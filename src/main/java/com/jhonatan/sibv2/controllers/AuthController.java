package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.jhonatan.sibv2.controllers.ApiSupport.cpf;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final FuncionarioRepository funcionarios;

    public AuthController(FuncionarioRepository funcionarios) { this.funcionarios = funcionarios; }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String id = cpf(request.cpf());
        Funcionario funcionario = funcionarios.findByCpfPessoaAndSenha(id, request.senha())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário ou senha não encontrados"));
        return Map.of("cpfPessoa", funcionario.getCpfPessoa(), "nomePessoa", funcionario.getNomePessoa(),
                "message", "Login realizado");
    }

    public record LoginRequest(String cpf, String senha) {}
}
