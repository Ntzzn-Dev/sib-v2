package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.LoginRequest;
import com.jhonatan.sibv2.dto.requests.ProfileRequest;
import com.jhonatan.sibv2.dto.responses.LoginResponse;
import com.jhonatan.sibv2.dto.responses.ProfileResponse;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {
    private final FuncionarioRepository funcionarios;
    private final FuncionarioService funcionarioService;
    private final EnderecoService enderecoService;

    public AuthService(FuncionarioRepository funcionarios, FuncionarioService funcionarioService,
                       EnderecoService enderecoService) {
        this.funcionarios = funcionarios;
        this.funcionarioService = funcionarioService;
        this.enderecoService = enderecoService;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase(Locale.ROOT);
        Funcionario funcionario = funcionarios.findByEmailIgnoreCaseAndSenhaAndAtivoTrue(email, request.getSenha())
                .orElseThrow(this::credenciaisInvalidas);

        LoginResponse response = new LoginResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()));
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setEmail(funcionario.getEmail());
        response.setPerfil(funcionario.getPerfil());
        response.setMessage("Login realizado");
        return response;
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha não encontrados");
    }

    public ProfileResponse meuPerfil(String cpf) {
        Funcionario funcionario = funcionarioService.buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        ProfileResponse response = new ProfileResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()));
        response.setEmail(funcionario.getEmail());
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setTelPessoa(funcionario.getTelPessoa());
        response.setPerfil(funcionario.getPerfil());
        var endereco = enderecoService.buscarPorPessoa(funcionario.getCpfPessoa());
        if (endereco == null) endereco = funcionario.getEndPessoa();
        if (endereco != null) response.setEndPessoa(enderecoService.mapearResposta(endereco));
        return response;
    }

    public ProfileResponse atualizarMeuPerfil(String cpf, ProfileRequest request) {
        Funcionario funcionario = funcionarioService.buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        funcionario.setEmail(normalizarEmail(request.getEmail()));
        if (request.getNomePessoa() == null || request.getNomePessoa().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório.");
        }
        funcionario.setNomePessoa(request.getNomePessoa().trim());
        funcionario.setTelPessoa(request.getTelPessoa());

        boolean trocarSenha = (request.getSenhaAtual() != null && !request.getSenhaAtual().isBlank())
                || (request.getNovaSenha() != null && !request.getNovaSenha().isBlank());
        if (trocarSenha) {
            if (!funcionario.getSenha().equals(request.getSenhaAtual())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha atual está incorreta.");
            }
            if (request.getNovaSenha() == null || request.getNovaSenha().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a nova senha.");
            }
            funcionario.setSenha(request.getNovaSenha());
        }

        validarEmailDisponivel(funcionario.getEmail(), funcionario.getCpfPessoa());
        if (request.getEndPessoa() != null && temEndereco(request.getEndPessoa())) {
            enderecoService.salvarParaPessoa(funcionario, request.getEndPessoa());
        }
        funcionarios.save(funcionario);
        return meuPerfil(funcionario.getCpfPessoa());
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail é obrigatório.");
        }
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido.");
        }
        return normalizado;
    }

    private boolean temEndereco(com.jhonatan.sibv2.dto.requests.EnderecoRequest endereco) {
        return endereco.getUf() != null && !endereco.getUf().isBlank()
                || endereco.getCep() != null && !endereco.getCep().isBlank()
                || endereco.getCidade() != null && !endereco.getCidade().isBlank()
                || endereco.getBairro() != null && !endereco.getBairro().isBlank()
                || endereco.getLogradouro() != null && !endereco.getLogradouro().isBlank()
                || endereco.getNumero() != null && !endereco.getNumero().isBlank();
    }

    public void validarEmailDisponivel(String email, String cpf) {
        funcionarios.findByEmailIgnoreCase(email).filter(outro -> !outro.getCpfPessoa().equals(cpf))
                .ifPresent(outro -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Este e-mail já está cadastrado para outro funcionário."); });
    }
}
