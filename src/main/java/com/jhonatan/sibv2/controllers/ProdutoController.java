package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.ProdutoRequest;
import com.jhonatan.sibv2.dto.responses.ProdutoResponse;
import com.jhonatan.sibv2.services.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProdutoController {

    private final ProdutoService produtos;

    public ProdutoController(ProdutoService produtos) {
        this.produtos = produtos;
    }

    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) String q, @RequestParam(required = false) Boolean vendido) {
        return produtos.listar(q, vendido);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable int id) {
        return produtos.buscar(id);
    }

    @PostMapping
    public ProdutoResponse criar(@RequestBody ProdutoRequest request) {
        return produtos.criar(request);
    }

    @PutMapping("/{id}")
    public ProdutoResponse editar(@PathVariable int id, @RequestBody ProdutoRequest request) {
        return produtos.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        produtos.apagar(id);
    }
}
