package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.CategoriaRequest;
import com.jhonatan.sibv2.dto.responses.CategoriaResponse;
import com.jhonatan.sibv2.services.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categorias")
@CrossOrigin
public class CategoriaController {

    private final CategoriaService categorias;

    public CategoriaController(CategoriaService categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    public List<CategoriaResponse> listar(@RequestParam(required = false) String q) {
        return categorias.listar(q);
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable int id) {
        return categorias.buscar(id);
    }

    @PostMapping
    public CategoriaResponse criar(@RequestBody CategoriaRequest request) {
        return categorias.criar(request);
    }

    @PutMapping("/{id}")
    public CategoriaResponse editar(@PathVariable int id, @RequestBody CategoriaRequest request) {
        return categorias.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        categorias.apagar(id);
    }
}
