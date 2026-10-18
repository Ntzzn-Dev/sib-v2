package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ClienteRequest;
import com.jhonatan.sibv2.dto.responses.ClienteResponse;
import com.jhonatan.sibv2.entities.Cliente;
import com.jhonatan.sibv2.repositories.ClienteRepository;
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
        return resposta(buscarEntidadeObrigatoria(formatarCpf(cpf)));
    }

    public ClienteResponse criar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setCpfPessoa(formatarCpf(request.getCpfPessoa()));
        aplicarDados(cliente, request);
        return resposta(clientes.save(cliente));
    }

    public ClienteResponse editar(String cpf, ClienteRequest request) {
        Cliente cliente = buscarEntidadeObrigatoria(formatarCpf(cpf));
        aplicarDados(cliente, request);
        return resposta(clientes.save(cliente));
    }

    public void apagar(String cpf) {
        clientes.delete(buscarEntidadeObrigatoria(formatarCpf(cpf)));
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

    private String formatarCpf(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido: informe 11 dígitos");
        }
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "."
                + digits.substring(6, 9) + "-" + digits.substring(9);
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
