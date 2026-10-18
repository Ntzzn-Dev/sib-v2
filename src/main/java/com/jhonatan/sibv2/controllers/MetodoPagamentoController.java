package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.MetodoPagamento;
import com.jhonatan.sibv2.repositories.MetodoPagamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/metodos-pagamento")
@CrossOrigin
public class MetodoPagamentoController {

    private final MetodoPagamentoRepository metodos;

    public MetodoPagamentoController(MetodoPagamentoRepository metodos) {
        this.metodos = metodos;
    }

    @GetMapping
    public List<MetodoPagamento> listar(@RequestParam(required = false) String q) {
        return filter(metodos.findAll(), q, m -> has(m.getDescMetodoPag(), q)
                || has(m.isActiveMetodoPag() ? "ativo" : "desativado", q));
    }

    @GetMapping("/{id}")
    public MetodoPagamento buscar(@PathVariable int id) {
        return required(metodos, id, "Método de pagamento");
    }

    @PostMapping
    public MetodoPagamento criar(@RequestBody MetodoPagamento metodo) {
        metodo.setCodMetodoPag(0);
        return metodos.save(metodo);
    }

    @PutMapping("/{id}")
    public MetodoPagamento editar(@PathVariable int id, @RequestBody MetodoPagamento dados) {
        MetodoPagamento metodo = required(metodos, id, "Método de pagamento");
        metodo.setDescMetodoPag(dados.getDescMetodoPag());
        metodo.setActiveMetodoPag(dados.isActiveMetodoPag());
        return metodos.save(metodo);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        metodos.delete(required(metodos, id, "Método de pagamento"));
    }
}
