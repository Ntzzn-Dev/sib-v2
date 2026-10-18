package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Cliente;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.repositories.ClienteRepository;
import com.jhonatan.sibv2.repositories.EnderecoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin
public class ClienteController {

    private final ClienteRepository clientes;
    private final EnderecoRepository enderecos;

    public ClienteController(ClienteRepository clientes, EnderecoRepository enderecos) {
        this.clientes = clientes;
        this.enderecos = enderecos;
    }

    @GetMapping public List<Cliente> listar(@RequestParam(required = false) String q) {
        return filter(clientes.findAll(), q, c -> has(c.getNomePessoa(), q)
                || has(c.getCpfPessoa(), q) || has(c.getTelPessoa(), q));
    }
    @GetMapping("/{cpf}") public Cliente buscar(@PathVariable String cpf) { return required(clientes, cpf(cpf), "Cliente"); }
    @PostMapping public Cliente criar(@RequestBody Cliente cliente) {
        cliente.setCpfPessoa(cpf(cliente.getCpfPessoa())); cliente.setEndPessoa(enderecoRef(cliente.getEndPessoa()));
        return clientes.save(cliente);
    }
    @PutMapping("/{cpf}") public Cliente editar(@PathVariable String cpf, @RequestBody Cliente dados) {
        Cliente cliente = required(clientes, cpf(cpf), "Cliente"); cliente.setNomePessoa(dados.getNomePessoa());
        cliente.setTelPessoa(dados.getTelPessoa()); cliente.setEndPessoa(enderecoRef(dados.getEndPessoa()));
        return clientes.save(cliente);
    }
    @DeleteMapping("/{cpf}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable String cpf) { clientes.delete(required(clientes, cpf(cpf), "Cliente")); }

    private Endereco enderecoRef(Endereco value) {
        return value == null || value.getCodEnd() <= 0 ? null : required(enderecos, value.getCodEnd(), "Endereço");
    }
}
