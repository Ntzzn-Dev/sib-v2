package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Categoria;
import com.jhonatan.sibv2.repositories.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin
public class CategoriaController {

    private final CategoriaRepository categorias;

    public CategoriaController(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    public List<Categoria> listar(@RequestParam(required = false) String q) {
        return filter(categorias.findAll(), q, categoria -> has(categoria.getNomeCat(), q));
    }

    @GetMapping("/{id}")
    public Categoria buscar(@PathVariable int id) {
        return required(categorias, id, "Categoria");
    }

    @PostMapping
    public Categoria criar(@RequestBody Categoria categoria) {
        categoria.setCodCat(0);
        return categorias.save(categoria);
    }

    @PutMapping("/{id}")
    public Categoria editar(@PathVariable int id, @RequestBody Categoria dados) {
        Categoria categoria = required(categorias, id, "Categoria");
        categoria.setNomeCat(dados.getNomeCat());
        return categorias.save(categoria);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        categorias.delete(required(categorias, id, "Categoria"));
    }
}
