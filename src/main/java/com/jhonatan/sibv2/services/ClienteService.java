package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ClienteRequest;
import com.jhonatan.sibv2.dto.responses.ClienteResponse;
import com.jhonatan.sibv2.entities.Cliente;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.repositories.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final EnderecoService enderecoService;
    private final EntityManager entityManager;

    public ClienteService(ClienteRepository clienteRepository, EnderecoService enderecoService, EntityManager entityManager) {
        this.clienteRepository = clienteRepository;
        this.enderecoService = enderecoService;
        this.entityManager = entityManager;
    }

    public List<ClienteResponse> listar(String pesquisa) {
        String termo = pesquisa == null || pesquisa.isBlank() ? null : pesquisa.trim();

        StoredProcedureQuery consulta = entityManager.createStoredProcedureQuery("dbo.sp_clientes_listar");
        consulta.registerStoredProcedureParameter("q", String.class, ParameterMode.IN);
        consulta.setParameter("q", termo);
        @SuppressWarnings("unchecked")
        List<Object[]> linhas = consulta.getResultList();

        return linhas.stream().map(linha -> {
            ClienteResponse response = mapearResposta(buscarEntidadeObrigatoria((String) linha[0]));
            response.setTotalVendas(((Number) linha[1]).intValue());
            response.setVendasFinalizadas(((Number) linha[2]).intValue());
            response.setTotalGasto((BigDecimal) linha[3]);
            response.setUltimaVenda(dataHora(linha[4]));
            return response;
        }).toList();
    }

    public ClienteResponse buscar(String cpf) {
        return mapearResposta(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public ClienteResponse criar(ClienteRequest request) {
        String cpf = FormatadorDocumento.normalizarCpf(request.getCpfPessoa());
        Endereco end = enderecoService.buscarReferencia(request.getEndPessoa());

        Cliente cliente = new Cliente();
        cliente.setCpfPessoa(cpf);
        cliente.setNomePessoa(request.getNomePessoa());
        cliente.setTelPessoa(request.getTelPessoa());
        cliente.setEndPessoa(end);

        Cliente criado = clienteRepository.save(cliente);
        enderecoService.associarPessoa(criado.getEndPessoa(), criado.getCpfPessoa());

        return mapearResposta(criado);
    }

    public ClienteResponse editar(String cpf, ClienteRequest request) {
        Cliente cliente = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        Endereco end = enderecoService.buscarReferencia(request.getEndPessoa());

        cliente.setNomePessoa(request.getNomePessoa());
        cliente.setTelPessoa(request.getTelPessoa());
        cliente.setEndPessoa(end);

        Cliente atualizado = clienteRepository.save(cliente);
        enderecoService.associarPessoa(atualizado.getEndPessoa(), atualizado.getCpfPessoa());

        return mapearResposta(atualizado);
    }

    public void apagar(String cpf) {
        clienteRepository.delete(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public Cliente buscarEntidadeObrigatoria(String cpf) {
        return clienteRepository.findById(cpf).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    public ClienteResponse mapearResposta(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(cliente.getCpfPessoa()));
        response.setNomePessoa(cliente.getNomePessoa());
        response.setTelPessoa(cliente.getTelPessoa());

        StoredProcedureQuery consultaResumo = entityManager.createStoredProcedureQuery("dbo.sp_cliente_resumo");
        consultaResumo.registerStoredProcedureParameter("cpf_cliente", String.class, ParameterMode.IN);
        consultaResumo.setParameter("cpf_cliente", cliente.getCpfPessoa());
        @SuppressWarnings("unchecked")
        List<Object[]> resumoCliente = consultaResumo.getResultList();

        if (!resumoCliente.isEmpty()) {
            Object[] resumo = resumoCliente.getFirst();
            response.setTotalVendas(((Number) resumo[0]).intValue());
            response.setVendasFinalizadas(((Number) resumo[1]).intValue());
            response.setTotalGasto((BigDecimal) resumo[2]);
            response.setUltimaVenda(dataHora(resumo[3]));
        }

        var endereco = cliente.getEndPessoa() != null
                ? cliente.getEndPessoa()
                : enderecoService.buscarPorPessoa(cliente.getCpfPessoa());

        if (endereco != null) {
            response.setEndPessoa(enderecoService.mapearResposta(endereco));
        }

        return response;
    }

    private LocalDateTime dataHora(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalDateTime dataHora) return dataHora;
        if (valor instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        throw new IllegalStateException("Tipo de data e hora inesperado: " + valor.getClass().getName());
    }

}
