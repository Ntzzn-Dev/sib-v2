package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.CriarVendaRequest;
import com.jhonatan.sibv2.dto.requests.ListaProdutosRequest;
import com.jhonatan.sibv2.dto.requests.PagamentoRequest;
import com.jhonatan.sibv2.dto.responses.VendaPagamentoResponse;
import com.jhonatan.sibv2.dto.responses.VendaResponse;
import com.jhonatan.sibv2.entities.*;
import com.jhonatan.sibv2.repositories.VendaRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendaService {
    private final VendaRepository vendas;
    private final ClienteService clientes;
    private final FuncionarioService funcionarios;
    private final ProdutoService produtos;
    private final MetodoPagamentoService metodos;
    private final EntityManager entityManager;

    public VendaService(VendaRepository vendas, ClienteService clientes, FuncionarioService funcionarios,
                        ProdutoService produtos, MetodoPagamentoService metodos, EntityManager entityManager) {
        this.vendas = vendas;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.produtos = produtos;
        this.metodos = metodos;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listar(Integer status, String q) {
        String termo = q == null || q.isBlank() ? null : q.trim();
        StoredProcedureQuery consulta = entityManager.createStoredProcedureQuery("dbo.sp_vendas_listar");
        consulta.registerStoredProcedureParameter("status", Integer.class, ParameterMode.IN);
        consulta.registerStoredProcedureParameter("q", String.class, ParameterMode.IN);
        consulta.setParameter("status", status);
        consulta.setParameter("q", termo);
        @SuppressWarnings("unchecked")
        List<Number> codigos = consulta.getResultList();
        return codigos.stream()
                .map(codigo -> buscarVendaObrigatoria(codigo.intValue()))
                .map(this::mapearResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscar(int id) {
        return mapearResposta(buscarVendaObrigatoria(id));
    }

    @Transactional
    public VendaResponse criar(CriarVendaRequest request) {
        Venda venda = new Venda();
        venda.setDataVenda(LocalDateTime.now());
        venda.setClienteVenda(clientes.buscarEntidadeObrigatoria(
                FormatadorDocumento.normalizarCpf(request.getClienteCpf())));
        venda.setFuncVenda(funcionarios.buscarEntidadeObrigatoria(
                FormatadorDocumento.normalizarCpf(request.getFuncionarioCpf())));
        return mapearResposta(vendas.save(venda));
    }

    @Transactional
    public VendaResponse adicionarProdutos(int id, ListaProdutosRequest request) {
        StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("dbo.sp_venda_adicionar_produtos");
        procedure.registerStoredProcedureParameter("venda_id", Integer.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter("produtos_json", String.class, ParameterMode.IN);
        procedure.setParameter("venda_id", id);
        procedure.setParameter("produtos_json", produtosJson(request.getCodigos()));
        procedure.execute();
        entityManager.clear();
        return mapearResposta(buscarVendaObrigatoria(id));
    }

    @Transactional
    public VendaResponse removerProduto(int id, int produtoId) {
        StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("dbo.sp_venda_remover_produto");
        procedure.registerStoredProcedureParameter("venda_id", Integer.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter("produto_id", Integer.class, ParameterMode.IN);
        procedure.setParameter("venda_id", id);
        procedure.setParameter("produto_id", produtoId);
        procedure.execute();
        entityManager.clear();
        return mapearResposta(buscarVendaObrigatoria(id));
    }

    @Transactional
    public VendaResponse pagar(int id, List<PagamentoRequest> request) {
        StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("dbo.sp_venda_pagar");
        procedure.registerStoredProcedureParameter("venda_id", Integer.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter("pagamentos_json", String.class, ParameterMode.IN);
        procedure.setParameter("venda_id", id);
        procedure.setParameter("pagamentos_json", pagamentosJson(request));
        procedure.execute();
        entityManager.clear();
        return mapearResposta(buscarVendaObrigatoria(id));
    }

    @Transactional
    public VendaResponse cancelar(int id) {
        StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("dbo.sp_venda_cancelar");
        procedure.registerStoredProcedureParameter("venda_id", Integer.class, ParameterMode.IN);
        procedure.setParameter("venda_id", id);
        procedure.execute();
        entityManager.clear();
        return mapearResposta(buscarVendaObrigatoria(id));
    }

    private String produtosJson(List<Integer> codigos) {
        if (codigos == null) return "null";
        return codigos.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    private String pagamentosJson(List<PagamentoRequest> pagamentos) {
        if (pagamentos == null) return "null";
        return pagamentos.stream().map(item -> item == null ? "null"
                : "{\"metodoPagamentoId\":" + item.getMetodoPagamentoId()
                    + ",\"valorPago\":" + (item.getValorPago() == null ? "null" : item.getValorPago().toPlainString()) + "}")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    private Venda buscarVendaObrigatoria(int id) {
        return vendas.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Venda não encontrada"));
    }

    private VendaResponse mapearResposta(Venda venda) {
        VendaResponse response = new VendaResponse();
        response.setCodVenda(venda.getCodVenda());
        response.setDataVenda(venda.getDataVenda());
        response.setClienteVenda(clientes.mapearResposta(venda.getClienteVenda()));
        response.setFuncVenda(funcionarios.mapearResposta(venda.getFuncVenda()));
        response.setValorTotal(venda.getValorTotal());
        response.setStatus(venda.getStatus());
        response.setStatusString(statusTextoBanco(venda.getStatus()));

        StoredProcedureQuery consultaResumo = entityManager.createStoredProcedureQuery("dbo.sp_venda_resumo");
        consultaResumo.registerStoredProcedureParameter("venda_id", Integer.class, ParameterMode.IN);
        consultaResumo.setParameter("venda_id", venda.getCodVenda());
        Object[] resumo = (Object[]) consultaResumo.getSingleResult();
        response.setTotalPago((BigDecimal) resumo[0]);
        response.setQuantidadeProdutos(((Number) resumo[1]).longValue());
        response.setProdutos(venda.getProdutos().stream().map(produtos::mapearResposta).toList());
        response.setPagamentos(venda.getPagamentos().stream().map(this::mapearPagamento).toList());
        return response;
    }

    private String statusTextoBanco(int status) {
        StoredProcedureQuery consulta = entityManager.createStoredProcedureQuery("dbo.sp_venda_status_texto");
        consulta.registerStoredProcedureParameter("status", Integer.class, ParameterMode.IN);
        consulta.setParameter("status", status);
        return (String) consulta.getSingleResult();
    }

    private VendaPagamentoResponse mapearPagamento(VendaPagamento pagamento) {
        VendaPagamentoResponse response = new VendaPagamentoResponse();
        response.setCodVendaPagamento((long) pagamento.getMetodoPagamento().getCodMetodoPag());
        response.setMetodoPagamento(metodos.mapearResposta(pagamento.getMetodoPagamento()));
        response.setValorPago(pagamento.getValorPago());
        return response;
    }
}
