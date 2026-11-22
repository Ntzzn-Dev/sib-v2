package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ConfiguracaoEmailRequest;
import com.jhonatan.sibv2.dto.responses.ConfiguracaoEmailResponse;
import com.jhonatan.sibv2.entities.ConfiguracaoEmail;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.ConfiguracaoEmailRepository;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class EmailService {
    private static final int COD_CONFIGURACAO = 1;
    private static final int TAMANHO_SENHA = 8;
    private static final Duration INTERVALO_SOLICITACAO = Duration.ofMinutes(2);
    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConfiguracaoEmailRepository configuracaoEmailRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ConcurrentMap<String, Instant> solicitacoes = new ConcurrentHashMap<>();

    public EmailService(ConfiguracaoEmailRepository configuracaoEmailRepository,
                        FuncionarioRepository funcionarioRepository) {
        this.configuracaoEmailRepository = configuracaoEmailRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoEmailResponse consultarConfiguracao() {
        return configuracaoEmailRepository.findById(COD_CONFIGURACAO)
                .map(configuracao -> new ConfiguracaoEmailResponse(configuracao.getEmailRemetente(), true))
                .orElseGet(() -> new ConfiguracaoEmailResponse(null, false));
    }

    @Transactional
    public ConfiguracaoEmailResponse salvarConfiguracao(ConfiguracaoEmailRequest request) {
        String email = normalizarEmail(request == null ? null : request.getEmailRemetente());
        ConfiguracaoEmail configuracao = configuracaoEmailRepository.findById(COD_CONFIGURACAO)
                .orElseGet(() -> {
                    ConfiguracaoEmail nova = new ConfiguracaoEmail();
                    nova.setCodConfiguracao(COD_CONFIGURACAO);
                    return nova;
                });

        String senhaApp = request.getSenhaApp() == null
                ? "" : request.getSenhaApp().replaceAll("\\s", "");
        if (!senhaApp.isBlank()) {
            configuracao.setSenhaApp(senhaApp);
        } else if (configuracao.getSenhaApp() == null || configuracao.getSenhaApp().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a senha de app do Gmail.");
        }

        configuracao.setEmailRemetente(email);
        configuracaoEmailRepository.save(configuracao);
        return new ConfiguracaoEmailResponse(email, true);
    }

    public void enviarTeste() {
        ConfiguracaoEmail configuracao = configuracaoObrigatoria();
        enviar(configuracao, configuracao.getEmailRemetente(),
                "SIB · Teste de configuração de e-mail",
                "O envio de e-mails do SIB está configurado corretamente.");
    }

    @Transactional
    public void recuperarSenha(String emailInformado) {
        String email = emailInformado == null ? "" : emailInformado.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return;
        if (limiteAtingido(email)) return;

        Funcionario funcionario = funcionarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (funcionario == null) return;

        String senhaTemporaria = gerarSenhaTemporaria();
        funcionario.setSenha(senhaTemporaria);
        funcionarioRepository.saveAndFlush(funcionario);
        ConfiguracaoEmail configuracao = configuracaoObrigatoria();
        enviar(configuracao, funcionario.getEmail(), "SIB · Recuperação de senha",
                "Olá, " + funcionario.getNomePessoa() + ".\n\n"
                        + "Sua nova senha temporária do SIB é:\n\n" + senhaTemporaria + "\n\n"
                        + "Entre no sistema com esta senha e altere-a pelo seu perfil.\n"
                        + "Se você não solicitou esta recuperação, avise o administrador do brechó.");
    }

    private void enviar(ConfiguracaoEmail configuracao, String destinatario, String assunto, String texto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(configuracao.getEmailRemetente());
        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(configuracao.getEmailRemetente());
        sender.setPassword(configuracao.getSenhaApp());
        sender.setDefaultEncoding("UTF-8");
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");
        sender.send(mensagem);
    }

    private ConfiguracaoEmail configuracaoObrigatoria() {
        return configuracaoEmailRepository.findById(COD_CONFIGURACAO).orElseThrow(() ->
                new IllegalStateException("O administrador ainda não configurou o e-mail remetente."));
    }

    private String normalizarEmail(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o e-mail remetente.");
        }
        String email = valor.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail remetente válido.");
        }
        return email;
    }

    private boolean limiteAtingido(String email) {
        Instant agora = Instant.now();
        Instant anterior = solicitacoes.put(email, agora);
        if (solicitacoes.size() > 10_000) {
            solicitacoes.entrySet().removeIf(entrada -> entrada.getValue().isBefore(agora.minus(INTERVALO_SOLICITACAO)));
        }
        return anterior != null && Duration.between(anterior, agora).compareTo(INTERVALO_SOLICITACAO) < 0;
    }

    private String gerarSenhaTemporaria() {
        StringBuilder senha = new StringBuilder(TAMANHO_SENHA);
        for (int i = 0; i < TAMANHO_SENHA; i++) {
            senha.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return senha.toString();
    }

}
