package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.MetodoPagamentoRequest;
import com.jhonatan.sibv2.dto.responses.MetodoPagamentoResponse;
import com.jhonatan.sibv2.services.MetodoPagamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/metodos-pagamento")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MetodoPagamentoController {

    private final MetodoPagamentoService metodos;

    public MetodoPagamentoController(MetodoPagamentoService metodos) {
        this.metodos = metodos;
    }

    @GetMapping
    public List<MetodoPagamentoResponse> listar(@RequestParam(required = false) String q) {
        return metodos.listar(q);
    }

    @GetMapping("/{id}")
    public MetodoPagamentoResponse buscar(@PathVariable int id) {
        return metodos.buscar(id);
    }

    @PostMapping
    public MetodoPagamentoResponse criar(@RequestBody MetodoPagamentoRequest request) {
        return metodos.criar(request);
    }

    @PutMapping("/{id}")
    public MetodoPagamentoResponse editar(@PathVariable int id, @RequestBody MetodoPagamentoRequest request) {
        return metodos.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        metodos.apagar(id);
    }
}
