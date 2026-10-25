package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.EnderecoRequest;
import com.jhonatan.sibv2.dto.responses.EnderecoResponse;
import com.jhonatan.sibv2.dto.responses.CepConsultaResponse;
import com.jhonatan.sibv2.dto.responses.EstadoResponse;
import com.jhonatan.sibv2.services.ConsultaCepService;
import com.jhonatan.sibv2.services.EnderecoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EnderecoController {

    private final EnderecoService enderecos;
    private final ConsultaCepService consultaCep;

    public EnderecoController(EnderecoService enderecos, ConsultaCepService consultaCep) {
        this.enderecos = enderecos;
        this.consultaCep = consultaCep;
    }

    @GetMapping
    public List<EnderecoResponse> listar(@RequestParam(required = false) String q) {
        return enderecos.listar(q);
    }

    @GetMapping("/ufs")
    public List<EstadoResponse> listarUfs() {
        return enderecos.listarUfs();
    }

    @GetMapping("/{id}")
    public EnderecoResponse buscar(@PathVariable int id) {
        return enderecos.buscar(id);
    }

    @GetMapping("/cep/{cep}")
    public CepConsultaResponse consultarCep(@PathVariable String cep) {
        return consultaCep.buscar(cep);
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
