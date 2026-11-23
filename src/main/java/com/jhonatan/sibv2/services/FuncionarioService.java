package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.FuncionarioRequest;
import com.jhonatan.sibv2.dto.responses.FuncionarioResponse;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;
    private final EnderecoService enderecoService;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, EnderecoService enderecoService) {
        this.funcionarioRepository = funcionarioRepository;
        this.enderecoService = enderecoService;
    }

    public List<FuncionarioResponse> listar(String pesquisa, boolean incluirInativos) {
        String termo = pesquisa == null || pesquisa.isBlank() ? null : pesquisa.trim();
        List<Funcionario> funcionarios = funcionarioRepository.findAll().stream()
                .filter(funcionario -> incluirInativos || funcionario.isAtivo()).toList();
        return funcionarios.stream()
                .filter(funcionario -> termo == null || corresponde(funcionario, termo))
                .map(this::mapearResposta)
                .toList();
    }

    public FuncionarioResponse buscar(String cpf) {
        return mapearResposta(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public FuncionarioResponse criar(FuncionarioRequest request) {
        String cpf = FormatadorDocumento.normalizarCpf(request.getCpfPessoa());
        String email = validarEmail(request.getEmail());
        validarEmailDisponivel(email, cpf);
        Endereco end = enderecoService.buscarReferencia(request.getEndPessoa());
        String senha = senhaObrigatoria(request.getSenha());
        boolean primeiroFuncionario = funcionarioRepository.countByAtivoTrue() == 0;
        int perfil = primeiroFuncionario
                ? Funcionario.PERFIL_ADMIN
                : perfilValidoOuPadrao(request.getPerfil());

        Funcionario funcionario = new Funcionario();
        funcionario.setCpfPessoa(cpf);
        funcionario.setNomePessoa(request.getNomePessoa());
        funcionario.setTelPessoa(request.getTelPessoa());
        funcionario.setEmail(email);
        funcionario.setSalarioFunc(request.getSalarioFunc());
        funcionario.setEndPessoa(end);
        funcionario.setSenha(senha);
        funcionario.setPerfil(perfil);

        Funcionario criado = funcionarioRepository.save(funcionario);
        enderecoService.associarPessoa(end, criado.getCpfPessoa());

        return mapearResposta(criado);
    }

    @Transactional
    public FuncionarioResponse editar(String cpf, FuncionarioRequest request) {
        Funcionario funcionario = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        String email = validarEmail(request.getEmail());
        validarEmailDisponivel(email, funcionario.getCpfPessoa());

        Endereco enderecoAnterior = enderecoService.buscarPorPessoa(funcionario.getCpfPessoa());
        Endereco end = enderecoService.buscarReferencia(request.getEndPessoa());
        int perfilAnterior = funcionario.getPerfil();

        funcionario.setNomePessoa(request.getNomePessoa());
        funcionario.setTelPessoa(request.getTelPessoa());
        funcionario.setEmail(email);
        funcionario.setSalarioFunc(request.getSalarioFunc());
        funcionario.setEndPessoa(end);
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            funcionario.setSenha(request.getSenha());
        }

        if (request.getPerfil() != null) {
            funcionario.setPerfil(perfilValidoOuPadrao(request.getPerfil()));
        } else {
            funcionario.setPerfil(perfilAnterior);
        }

        if(perfilAnterior == Funcionario.PERFIL_ADMIN) {
            validarMinimoAdministradorAoEditar(funcionario);
        }

        Funcionario atualizado = funcionarioRepository.save(funcionario);
        if (enderecoAnterior != null && (end == null || enderecoAnterior.getCodEnd() != end.getCodEnd())) {
            enderecoService.desassociarPessoa(enderecoAnterior, atualizado.getCpfPessoa());
        }
        enderecoService.associarPessoa(end, atualizado.getCpfPessoa());

        return mapearResposta(atualizado);
    }

    public void apagar(String cpf) {
        Funcionario funcionario = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        if (funcionario.getPerfil() == Funcionario.PERFIL_ADMIN && !existeOutroAdministrador(funcionario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O sistema precisa manter ao menos um administrador.");
        }
        funcionario.setAtivo(false);
        funcionarioRepository.save(funcionario);
    }

    public FuncionarioResponse reativar(String cpf) {
        Funcionario funcionario = funcionarioRepository.findById(FormatadorDocumento.normalizarCpf(cpf))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
        funcionario.setAtivo(true);
        return mapearResposta(funcionarioRepository.save(funcionario));
    }

    public Funcionario buscarEntidadeObrigatoria(String cpf) {
        Funcionario funcionario = funcionarioRepository.findById(cpf).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
        if (!funcionario.isAtivo()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado");
        return funcionario;
    }

    private boolean corresponde(Funcionario funcionario, String pesquisa) {
        String termo = pesquisa.toLowerCase(Locale.ROOT);
        return contem(funcionario.getNomePessoa(), termo) || contem(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()), termo)
                || contem(funcionario.getTelPessoa(), termo)
                || funcionario.getSalarioFunc().toPlainString().contains(termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    public FuncionarioResponse mapearResposta(Funcionario funcionario) {
        FuncionarioResponse response = new FuncionarioResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()));
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setTelPessoa(funcionario.getTelPessoa());
        response.setEmail(funcionario.getEmail());
        response.setSalarioFunc(funcionario.getSalarioFunc());
        response.setPerfil(funcionario.getPerfil());
        response.setAtivo(funcionario.isAtivo());
        var endereco = enderecoService.buscarPorPessoa(funcionario.getCpfPessoa());
        if (endereco == null) endereco = funcionario.getEndPessoa();
        if (endereco != null) response.setEndPessoa(enderecoService.mapearResposta(endereco));
        return response;
    }

    private int perfilValidoOuPadrao(Integer perfil) {
        if (perfil == null) return Funcionario.PERFIL_ATENDENTE;
        if (perfil != Funcionario.PERFIL_ADMIN && perfil != Funcionario.PERFIL_ATENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil de funcionário inválido.");
        }
        return perfil;
    }

    private void validarMinimoAdministradorAoEditar(Funcionario funcionario) {
        if (funcionario.getPerfil() != Funcionario.PERFIL_ADMIN && !existeOutroAdministrador(funcionario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O sistema precisa manter ao menos um administrador.");
        }
    }

    private boolean existeOutroAdministrador(Funcionario funcionario) {
        return funcionarioRepository.findAll().stream().anyMatch(outro ->
                outro.isAtivo()
                        && !outro.getCpfPessoa().equals(funcionario.getCpfPessoa())
                        && outro.getPerfil() == Funcionario.PERFIL_ADMIN);
    }

    private String senhaObrigatoria(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha do funcionário é obrigatória.");
        }
        return senha;
    }

    private String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail do funcionário é obrigatório.");
        }
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido.");
        }
        return normalizado;
    }

    private void validarEmailDisponivel(String email, String cpf) {
        funcionarioRepository.findByEmailIgnoreCase(email)
                .filter(outro -> !outro.getCpfPessoa().equals(cpf))
                .ifPresent(outro -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Este e-mail já está cadastrado para outro funcionário."); });
    }
}
