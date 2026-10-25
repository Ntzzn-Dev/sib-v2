package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.ClienteRequest;
import com.jhonatan.sibv2.dto.responses.ClienteResponse;
import com.jhonatan.sibv2.services.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ClienteController {

    private final ClienteService clientes;

    public ClienteController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String q) {
        return clientes.listar(q);
    }

    @GetMapping("/{cpf}")
    public ClienteResponse buscar(@PathVariable String cpf) {
        return clientes.buscar(cpf);
    }

    @PostMapping
    public ClienteResponse criar(@RequestBody ClienteRequest request) {
        return clientes.criar(request);
    }

    @PutMapping("/{cpf}")
    public ClienteResponse editar(@PathVariable String cpf, @RequestBody ClienteRequest request) {
        return clientes.editar(cpf, request);
    }

    @DeleteMapping("/{cpf}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable String cpf) {
        clientes.apagar(cpf);
    }
}
