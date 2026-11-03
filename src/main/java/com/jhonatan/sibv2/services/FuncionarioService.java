package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.FuncionarioRequest;
import com.jhonatan.sibv2.dto.responses.FuncionarioResponse;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class FuncionarioService {
    private final FuncionarioRepository funcionarios;
    private final EnderecoService enderecos;

    public FuncionarioService(FuncionarioRepository funcionarios, EnderecoService enderecos) {
        this.funcionarios = funcionarios;
        this.enderecos = enderecos;
    }

    public List<FuncionarioResponse> listar(String q) {
        return funcionarios.findAll().stream()
                .filter(funcionario -> q == null || q.isBlank() || corresponde(funcionario, q))
                .map(this::resposta)
                .toList();
    }

    public FuncionarioResponse buscar(String cpf) {
        return resposta(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public FuncionarioResponse criar(FuncionarioRequest request) {
        Funcionario funcionario = new Funcionario();
        funcionario.setCpfPessoa(FormatadorDocumento.normalizarCpf(request.getCpfPessoa()));
        aplicarDados(funcionario, request);
        Funcionario criado = funcionarios.save(funcionario);
        enderecos.associarPessoa(criado.getEndPessoa(), criado.getCpfPessoa());
        return resposta(criado);
    }

    public FuncionarioResponse editar(String cpf, FuncionarioRequest request) {
        Funcionario funcionario = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        aplicarDados(funcionario, request);
        return resposta(funcionarios.save(funcionario));
    }

    public void apagar(String cpf) {
        funcionarios.delete(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public Funcionario buscarEntidadeObrigatoria(String cpf) {
        return funcionarios.findById(cpf).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private void aplicarDados(Funcionario funcionario, FuncionarioRequest request) {
        funcionario.setNomePessoa(request.getNomePessoa());
        funcionario.setTelPessoa(request.getTelPessoa());
        funcionario.setSalarioFunc(request.getSalarioFunc());
        funcionario.setEndPessoa(enderecos.buscarReferencia(request.getEndPessoa()));
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            funcionario.setSenha(request.getSenha());
        }
    }

    private boolean corresponde(Funcionario funcionario, String q) {
        String termo = q.toLowerCase(Locale.ROOT);
        return contem(funcionario.getNomePessoa(), termo) || contem(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()), termo)
                || contem(funcionario.getTelPessoa(), termo)
                || funcionario.getSalarioFunc().toPlainString().contains(termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    public FuncionarioResponse mapearResposta(Funcionario funcionario) {
        FuncionarioResponse response = new FuncionarioResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()));
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setTelPessoa(funcionario.getTelPessoa());
        response.setSalarioFunc(funcionario.getSalarioFunc());
        var endereco = funcionario.getEndPessoa() != null ? funcionario.getEndPessoa()
                : enderecos.buscarPorPessoa(funcionario.getCpfPessoa());
        if (endereco != null) {
            response.setEndPessoa(enderecos.mapearResposta(endereco));
        }
        return response;
    }

    private FuncionarioResponse resposta(Funcionario funcionario) {
        return mapearResposta(funcionario);
    }
}
