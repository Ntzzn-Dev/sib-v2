package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.CriarVendaRequest;
import com.jhonatan.sibv2.dto.requests.ListaProdutosRequest;
import com.jhonatan.sibv2.dto.requests.PagamentoRequest;
import com.jhonatan.sibv2.dto.responses.VendaResponse;
import com.jhonatan.sibv2.services.VendaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin
public class VendaController {
    private final VendaService vendas;

    public VendaController(VendaService vendas) {
        this.vendas = vendas;
    }

    @GetMapping
    public List<VendaResponse> listar(@RequestParam(required = false) Integer status, @RequestParam(required = false) String q) {
        return vendas.listar(status, q);
    }

    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable int id) {
        return vendas.buscar(id);
    }

    @PostMapping
    public VendaResponse criar(@RequestBody CriarVendaRequest request) {
        return vendas.criar(request);
    }

    @PostMapping("/{id}/produtos")
    public VendaResponse adicionarProdutos(@PathVariable int id, @RequestBody ListaProdutosRequest request) {
        return vendas.adicionarProdutos(id, request);
    }

    @PostMapping("/{id}/pagamentos")
    public VendaResponse pagar(@PathVariable int id, @RequestBody List<PagamentoRequest> request) {
        return vendas.pagar(id, request);
    }

    @PostMapping("/{id}/cancelamento")
    @ResponseStatus(HttpStatus.OK)
    public VendaResponse cancelar(@PathVariable int id) {
        return vendas.cancelar(id);
    }
}
