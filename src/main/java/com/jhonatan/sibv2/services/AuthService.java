package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.LoginRequest;
import com.jhonatan.sibv2.dto.responses.LoginResponse;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final FuncionarioRepository funcionarios;

    public AuthService(FuncionarioRepository funcionarios) {
        this.funcionarios = funcionarios;
    }

    public LoginResponse login(LoginRequest request) {
        String cpf = FormatadorDocumento.formatarCpf(request.getCpf());
        Funcionario funcionario = funcionarios.findByCpfPessoaAndSenha(cpf, request.getSenha())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário ou senha não encontrados"));

        LoginResponse response = new LoginResponse();
        response.setCpfPessoa(funcionario.getCpfPessoa());
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setMessage("Login realizado");
        return response;
    }
}
