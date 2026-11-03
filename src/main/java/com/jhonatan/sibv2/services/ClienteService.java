package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ClienteRequest;
import com.jhonatan.sibv2.dto.responses.ClienteResponse;
import com.jhonatan.sibv2.entities.Cliente;
import com.jhonatan.sibv2.repositories.ClienteRepository;
import jakarta.persistence.EntityManager;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final EnderecoService enderecos;
    private final EntityManager entityManager;

    public ClienteService(ClienteRepository clientes, EnderecoService enderecos, EntityManager entityManager) {
        this.clientes = clientes;
        this.enderecos = enderecos;
        this.entityManager = entityManager;
    }

    public List<ClienteResponse> listar(String q) {
        String termo = q == null || q.isBlank() ? null : q.trim();
        @SuppressWarnings("unchecked")
        List<Object[]> linhas = entityManager.createNativeQuery("""
                SELECT cpf,total_vendas,vendas_finalizadas,total_gasto,ultima_venda
                FROM dbo.vw_clientes_compras
                WHERE (:q IS NULL OR nome LIKE '%' + :q + '%' OR cpf LIKE '%' + :q + '%'
                    OR telefone LIKE '%' + :q + '%')
                ORDER BY nome
                """)
                .setParameter("q", termo)
                .getResultList();
        return linhas.stream().map(linha -> {
            ClienteResponse response = mapearResposta(buscarEntidadeObrigatoria((String) linha[0]));
            response.setTotalVendas(((Number) linha[1]).intValue());
            response.setVendasFinalizadas(((Number) linha[2]).intValue());
            response.setTotalGasto((BigDecimal) linha[3]);
            response.setUltimaVenda(linha[4] == null ? null : ((Timestamp) linha[4]).toLocalDateTime());
            return response;
        }).toList();
    }

    public ClienteResponse buscar(String cpf) {
        return resposta(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public ClienteResponse criar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setCpfPessoa(FormatadorDocumento.normalizarCpf(request.getCpfPessoa()));
        aplicarDados(cliente, request);
        Cliente criado = clientes.save(cliente);
        enderecos.associarPessoa(criado.getEndPessoa(), criado.getCpfPessoa());
        return resposta(criado);
    }

    public ClienteResponse editar(String cpf, ClienteRequest request) {
        Cliente cliente = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        aplicarDados(cliente, request);
        Cliente atualizado = clientes.save(cliente);
        enderecos.associarPessoa(atualizado.getEndPessoa(), atualizado.getCpfPessoa());
        return resposta(atualizado);
    }

    public void apagar(String cpf) {
        clientes.delete(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
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

    public ClienteResponse mapearResposta(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(cliente.getCpfPessoa()));
        response.setNomePessoa(cliente.getNomePessoa());
        response.setTelPessoa(cliente.getTelPessoa());
        @SuppressWarnings("unchecked")
        List<Object[]> resumoCliente = entityManager.createNativeQuery("""
                SELECT total_vendas,vendas_finalizadas,total_gasto,ultima_venda
                FROM dbo.fn_cliente_resumo(:cpf)
                """)
                .setParameter("cpf", cliente.getCpfPessoa())
                .getResultList();
        if (!resumoCliente.isEmpty()) {
            Object[] resumo = resumoCliente.getFirst();
            response.setTotalVendas(((Number) resumo[0]).intValue());
            response.setVendasFinalizadas(((Number) resumo[1]).intValue());
            response.setTotalGasto((BigDecimal) resumo[2]);
            response.setUltimaVenda(resumo[3] == null ? null : ((Timestamp) resumo[3]).toLocalDateTime());
        }
        var endereco = cliente.getEndPessoa() != null ? cliente.getEndPessoa()
                : enderecos.buscarPorPessoa(cliente.getCpfPessoa());
        if (endereco != null) {
            response.setEndPessoa(enderecos.mapearResposta(endereco));
        }
        return response;
    }

    private ClienteResponse resposta(Cliente cliente) {
        return mapearResposta(cliente);
    }
}
