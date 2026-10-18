package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.repositories.EnderecoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/enderecos")
@CrossOrigin
public class EnderecoController {

    private static final String UFS = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO";

    private final EnderecoRepository enderecos;

    public EnderecoController(EnderecoRepository enderecos) {
        this.enderecos = enderecos;
    }

    @GetMapping
    public List<Endereco> listar(@RequestParam(required = false) String q) {
        return filter(enderecos.findAll(), q, e -> has(e.getUf(), q) || has(e.getCep(), q)
                || has(e.getCidade(), q) || has(e.getBairro(), q) || has(e.getLogradouro(), q)
                || has(e.getNumero(), q) || has(e.getComplemento(), q));
    }

    @GetMapping("/{id}")
    public Endereco buscar(@PathVariable int id) {
        return required(enderecos, id, "Endereço");
    }

    @PostMapping
    public Endereco criar(@RequestBody Endereco endereco) {
        endereco.setCodEnd(0);
        endereco.setUf(endereco.getUf().trim().toUpperCase(Locale.ROOT));
        endereco.setCep(cep(endereco.getCep()));
        validarUf(endereco.getUf());
        return enderecos.save(endereco);
    }

    @PutMapping("/{id}")
    public Endereco editar(@PathVariable int id, @RequestBody Endereco dados) {
        Endereco atual = required(enderecos, id, "Endereço");
        atual.setUf(dados.getUf().trim().toUpperCase(Locale.ROOT));
        atual.setCep(cep(dados.getCep()));
        validarUf(atual.getUf());
        atual.setCidade(dados.getCidade());
        atual.setBairro(dados.getBairro());
        atual.setLogradouro(dados.getLogradouro());
        atual.setNumero(dados.getNumero());
        atual.setComplemento(dados.getComplemento());
        return enderecos.save(atual);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        enderecos.delete(required(enderecos, id, "Endereço"));
    }

    private static void validarUf(String uf) {
        if (!uf.matches(UFS))
            invalid("UF inválida");
    }
}
