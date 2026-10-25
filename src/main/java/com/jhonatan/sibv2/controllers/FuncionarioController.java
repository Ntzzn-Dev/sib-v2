package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.FuncionarioRequest;
import com.jhonatan.sibv2.dto.responses.FuncionarioResponse;
import com.jhonatan.sibv2.services.FuncionarioService;
import com.jhonatan.sibv2.config.AuthSessionInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FuncionarioController {

    private final FuncionarioService funcionarios;

    public FuncionarioController(FuncionarioService funcionarios) {
        this.funcionarios = funcionarios;
    }

    @GetMapping
    public List<FuncionarioResponse> listar(@RequestParam(required = false) String q) {
        return funcionarios.listar(q);
    }

    @GetMapping("/{cpf}")
    public FuncionarioResponse buscar(@PathVariable String cpf) {
        return funcionarios.buscar(cpf);
    }

    @PostMapping
    public FuncionarioResponse criar(@RequestBody FuncionarioRequest request, HttpSession session) {
        FuncionarioResponse response = funcionarios.criar(request);
        session.setAttribute(AuthSessionInterceptor.AUTHENTICATED_EMPLOYEE, response.getCpfPessoa());
        return response;
    }

    @PutMapping("/{cpf}")
    public FuncionarioResponse editar(@PathVariable String cpf, @RequestBody FuncionarioRequest request) {
        return funcionarios.editar(cpf, request);
    }

    @DeleteMapping("/{cpf}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable String cpf) {
        funcionarios.apagar(cpf);
    }
}
