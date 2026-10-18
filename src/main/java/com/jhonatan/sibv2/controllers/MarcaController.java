package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.MarcaRequest;
import com.jhonatan.sibv2.dto.responses.MarcaResponse;
import com.jhonatan.sibv2.services.MarcaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
@CrossOrigin
public class MarcaController {
    private final MarcaService marcas;

    public MarcaController(MarcaService marcas) {
        this.marcas = marcas;
    }

    @GetMapping
    public List<MarcaResponse> listar(@RequestParam(required = false) String q) {
        return marcas.listar(q);
    }

    @GetMapping("/{id}")
    public MarcaResponse buscar(@PathVariable int id) {
        return marcas.buscar(id);
    }

    @PostMapping
    public MarcaResponse criar(@RequestBody MarcaRequest request) {
        return marcas.criar(request);
    }

    @PutMapping("/{id}")
    public MarcaResponse editar(@PathVariable int id, @RequestBody MarcaRequest request) {
        return marcas.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        marcas.apagar(id);
    }
}
