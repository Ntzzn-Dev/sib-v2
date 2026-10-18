package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Marca;
import com.jhonatan.sibv2.repositories.MarcaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/marcas")
@CrossOrigin
public class MarcaController {

    private final MarcaRepository marcas;

    public MarcaController(MarcaRepository marcas) {
        this.marcas = marcas;
    }

    @GetMapping
    public List<Marca> listar(@RequestParam(required = false) String q) {
        return filter(marcas.findAll(), q, marca -> has(marca.getNomeMarca(), q));
    }

    @GetMapping("/{id}")
    public Marca buscar(@PathVariable int id) {
        return required(marcas, id, "Marca");
    }

    @PostMapping
    public Marca criar(@RequestBody Marca marca) {
        marca.setCodMarca(0);
        return marcas.save(marca);
    }

    @PutMapping("/{id}")
    public Marca editar(@PathVariable int id, @RequestBody Marca dados) {
        Marca marca = required(marcas, id, "Marca");
        marca.setNomeMarca(dados.getNomeMarca());
        return marcas.save(marca);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        marcas.delete(required(marcas, id, "Marca"));
    }
}
