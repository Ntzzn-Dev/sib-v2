package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ClienteRequest;
import com.jhonatan.sibv2.dto.responses.ClienteResponse;
import com.jhonatan.sibv2.entities.Cliente;
import com.jhonatan.sibv2.repositories.ClienteRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final EnderecoService enderecos;

    public ClienteService(ClienteRepository clientes, EnderecoService enderecos) {
        this.clientes = clientes;
        this.enderecos = enderecos;
    }

    public List<ClienteResponse> listar(String q) {
        return clientes.findAll().stream()
                .filter(cliente -> q == null || q.isBlank() || corresponde(cliente, q))
                .map(this::resposta)
                .toList();
    }

    public ClienteResponse buscar(String cpf) {
        return resposta(buscarEntidadeObrigatoria(FormatadorDocumento.formatarCpf(cpf)));
    }

    public ClienteResponse criar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setCpfPessoa(FormatadorDocumento.formatarCpf(request.getCpfPessoa()));
        aplicarDados(cliente, request);
        return resposta(clientes.save(cliente));
    }

    public ClienteResponse editar(String cpf, ClienteRequest request) {
        Cliente cliente = buscarEntidadeObrigatoria(FormatadorDocumento.formatarCpf(cpf));
        aplicarDados(cliente, request);
        return resposta(clientes.save(cliente));
    }

    public void apagar(String cpf) {
        clientes.delete(buscarEntidadeObrigatoria(FormatadorDocumento.formatarCpf(cpf)));
    }

    public Cliente buscarEntidadeObrigatoria(String cpf) {
        return clientes.findById(cpf).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private void aplicarDados(Cliente cliente, ClienteRequest request) {
        cliente.setNomePessoa(request.getNomePessoa());
        cliente.setTelPessoa(request.getTelPessoa());
        cliente.setEndPessoa(enderecos.buscarReferencia(request.getEndPessoa()));
    }

    private boolean corresponde(Cliente cliente, String q) {
        String termo = q.toLowerCase(Locale.ROOT);
        return contem(cliente.getNomePessoa(), termo) || contem(cliente.getCpfPessoa(), termo)
                || contem(cliente.getTelPessoa(), termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    public ClienteResponse mapearResposta(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setCpfPessoa(cliente.getCpfPessoa());
        response.setNomePessoa(cliente.getNomePessoa());
        response.setTelPessoa(cliente.getTelPessoa());
        if (cliente.getEndPessoa() != null) {
            response.setEndPessoa(enderecos.mapearResposta(cliente.getEndPessoa()));
        }
        return response;
    }

    private ClienteResponse resposta(Cliente cliente) {
        return mapearResposta(cliente);
    }
}
