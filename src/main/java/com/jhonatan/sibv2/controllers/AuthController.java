package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.LoginRequest;
import com.jhonatan.sibv2.dto.requests.ConfiguracaoEmailRequest;
import com.jhonatan.sibv2.dto.requests.RecuperarSenhaRequest;
import com.jhonatan.sibv2.dto.responses.LoginResponse;
import com.jhonatan.sibv2.dto.responses.ConfiguracaoEmailResponse;
import com.jhonatan.sibv2.dto.responses.MensagemResponse;
import com.jhonatan.sibv2.dto.requests.ProfileRequest;
import com.jhonatan.sibv2.dto.responses.ProfileResponse;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.services.AuthService;
import com.jhonatan.sibv2.services.EmailService;
import com.jhonatan.sibv2.config.AuthSessionInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {
    private static final String MENSAGEM_RECUPERACAO =
            "Se o e-mail estiver cadastrado, você receberá uma nova senha temporária.";

    private final AuthService auth;
    private final EmailService emailService;
    private final FuncionarioRepository funcionarios;

    public AuthController(AuthService auth, EmailService emailService, FuncionarioRepository funcionarios) {
        this.auth = auth;
        this.emailService = emailService;
        this.funcionarios = funcionarios;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = auth.login(request);
        session.setAttribute(AuthSessionInterceptor.AUTHENTICATED_EMPLOYEE, response.getCpfPessoa());
        return response;
    }

    @GetMapping("/bootstrap")
    public Map<String, Boolean> verificarCadastroInicial() {
        return Map.of("cadastroInicial", funcionarios.count() == 0);
    }

    @PostMapping("/esqueci-senha")
    public MensagemResponse recuperarSenha(@RequestBody RecuperarSenhaRequest request) {
        try {
            emailService.recuperarSenha(request == null ? null : request.getEmail());
        } catch (MailException | DataAccessException | IllegalStateException erro) {
            System.out.println("Não foi possível enviar a senha temporária. Confira a configuração de e-mail.");
        }
        return new MensagemResponse(MENSAGEM_RECUPERACAO);
    }

    @GetMapping("/email-config")
    public ConfiguracaoEmailResponse consultarEmail(HttpSession session) {
        exigirAdministrador(session);
        return emailService.consultarConfiguracao();
    }

    @PutMapping("/email-config")
    public ConfiguracaoEmailResponse salvarEmail(@RequestBody ConfiguracaoEmailRequest request, HttpSession session) {
        exigirAdministrador(session);
        return emailService.salvarConfiguracao(request);
    }

    @PostMapping("/email-config/teste")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void testarEmail(HttpSession session) {
        exigirAdministrador(session);
        try {
            emailService.enviarTeste();
        } catch (MailException | IllegalStateException erro) {
            System.out.println("Falha ao enviar o e-mail de teste do SIB; confira a autenticação SMTP e a conexão." + erro);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O Gmail não aceitou o envio. Confira o e-mail e a senha de app.");
        }
    }

    @GetMapping("/me")
    public ProfileResponse myProfile(HttpSession session) {
        return auth.meuPerfil(cpfDaSessao(session));
    }

    @PutMapping("/me")
    public ProfileResponse updateMyProfile(@RequestBody ProfileRequest request, HttpSession session) {
        return auth.atualizarMeuPerfil(cpfDaSessao(session), request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    private String cpfDaSessao(HttpSession session) {
        Object cpf = session.getAttribute(AuthSessionInterceptor.AUTHENTICATED_EMPLOYEE);
        if (!(cpf instanceof String valor)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Faça login para realizar esta operação.");
        }
        return valor;
    }

    private void exigirAdministrador(HttpSession session) {
        String cpf = cpfDaSessao(session);
        boolean administrador = funcionarios.findById(com.jhonatan.sibv2.utils.FormatadorDocumento.normalizarCpf(cpf))
                .map(Funcionario::getPerfil)
                .filter(perfil -> perfil == Funcionario.PERFIL_ADMIN)
                .isPresent();
        if (!administrador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Esta configuração é permitida somente para administradores.");
        }
    }

}
