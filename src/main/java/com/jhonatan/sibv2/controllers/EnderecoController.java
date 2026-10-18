package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.EnderecoRequest;
import com.jhonatan.sibv2.dto.responses.EnderecoResponse;
import com.jhonatan.sibv2.services.EnderecoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@CrossOrigin
public class EnderecoController {

    private final EnderecoService enderecos;

    public EnderecoController(EnderecoService enderecos) {
        this.enderecos = enderecos;
    }

    @GetMapping
    public List<EnderecoResponse> listar(@RequestParam(required = false) String q) {
        return enderecos.listar(q);
    }

    @GetMapping("/{id}")
    public EnderecoResponse buscar(@PathVariable int id) {
        return enderecos.buscar(id);
    }

    @PostMapping
    public EnderecoResponse criar(@RequestBody EnderecoRequest request) {
        return enderecos.criar(request);
    }

    @PutMapping("/{id}")
    public EnderecoResponse editar(@PathVariable int id, @RequestBody EnderecoRequest request) {
        return enderecos.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        enderecos.apagar(id);
    }
}
