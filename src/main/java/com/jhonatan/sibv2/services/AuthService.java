package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.CadastroRequest;
import com.jhonatan.sibv2.dto.requests.FuncionarioRequest;
import com.jhonatan.sibv2.dto.requests.LoginRequest;
import com.jhonatan.sibv2.dto.responses.FuncionarioResponse;
import com.jhonatan.sibv2.dto.responses.LoginResponse;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final FuncionarioRepository funcionarios;
    private final FuncionarioService funcionarioService;

    public AuthService(FuncionarioRepository funcionarios, FuncionarioService funcionarioService) {
        this.funcionarios = funcionarios;
        this.funcionarioService = funcionarioService;
    }

    public LoginResponse login(LoginRequest request) {
        String cpf = formatarCpf(request.getCpf());
        Funcionario funcionario = funcionarios.findByCpfPessoaAndSenha(cpf, request.getSenha())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário ou senha não encontrados"));

        LoginResponse response = new LoginResponse();
        response.setCpfPessoa(funcionario.getCpfPessoa());
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setMessage("Login realizado");
        return response;
    }

    public FuncionarioResponse cadastrar(CadastroRequest request) {
        FuncionarioRequest funcionario = new FuncionarioRequest();
        funcionario.setCpfPessoa(request.getCpfPessoa());
        funcionario.setNomePessoa(request.getNomePessoa());
        funcionario.setTelPessoa(request.getTelPessoa());
        funcionario.setSalarioFunc(request.getSalarioFunc());
        funcionario.setSenha(request.getSenha());
        funcionario.setEndPessoa(request.getEndPessoa());
        return funcionarioService.criar(funcionario);
    }

    private String formatarCpf(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido: informe 11 dígitos");
        }
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "."
                + digits.substring(6, 9) + "-" + digits.substring(9);
    }
}
