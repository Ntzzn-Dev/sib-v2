package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.EnderecoRepository;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin
public class FuncionarioController {

    private final FuncionarioRepository funcionarios;
    private final EnderecoRepository enderecos;

    public FuncionarioController(FuncionarioRepository funcionarios, EnderecoRepository enderecos) {
        this.funcionarios = funcionarios;
        this.enderecos = enderecos;
    }

    @GetMapping
    public List<Funcionario> listar(@RequestParam(required = false) String q) {
        return filter(funcionarios.findAll(), q, f -> has(f.getNomePessoa(), q)
                || has(f.getCpfPessoa(), q) || has(f.getTelPessoa(), q)
                || has(String.valueOf(f.getSalarioFunc()), q));
    }

    @GetMapping("/{cpf}")
    public Funcionario buscar(@PathVariable String cpf) {
        return required(funcionarios, cpf(cpf), "Funcionário");
    }

    @PostMapping
    public Funcionario criar(@RequestBody Funcionario funcionario) {
        funcionario.setCpfPessoa(cpf(funcionario.getCpfPessoa()));
        funcionario.setEndPessoa(enderecoRef(funcionario.getEndPessoa()));
        return funcionarios.save(funcionario);
    }

    @PutMapping("/{cpf}")
    public Funcionario editar(@PathVariable String cpf, @RequestBody Funcionario dados) {
        Funcionario funcionario = required(funcionarios, cpf(cpf), "Funcionário");
        funcionario.setNomePessoa(dados.getNomePessoa());
        funcionario.setTelPessoa(dados.getTelPessoa());
        funcionario.setEndPessoa(enderecoRef(dados.getEndPessoa()));
        funcionario.setSalarioFunc(dados.getSalarioFunc());

        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            funcionario.setSenha(dados.getSenha());
        }

        return funcionarios.save(funcionario);
    }
    @DeleteMapping("/{cpf}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable String cpf) {
        funcionarios.delete(required(funcionarios, cpf(cpf), "Funcionário"));
    }

    private Endereco enderecoRef(Endereco value) {
        return value == null || value.getCodEnd() <= 0 ? null : required(enderecos, value.getCodEnd(), "Endereço");
    }
}
