package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.CriarVendaRequest;
import com.jhonatan.sibv2.dto.requests.ListaProdutosRequest;
import com.jhonatan.sibv2.dto.requests.PagamentoRequest;
import com.jhonatan.sibv2.dto.responses.VendaPagamentoResponse;
import com.jhonatan.sibv2.dto.responses.VendaResponse;
import com.jhonatan.sibv2.entities.*;
import com.jhonatan.sibv2.repositories.VendaRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
        if (status != null && (status < 0 || status > 2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status deve ser 0 (aberta), 1 (paga) ou 2 (cancelada)");
        }
        return vendas.findAll().stream()
                .filter(venda -> status == null || venda.getStatus() == status)
                .filter(venda -> q == null || q.isBlank() || corresponde(venda, q))
                .map(this::mapearResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscar(int id) {
        return mapearResposta(buscarVendaObrigatoria(id));
    }

    public VendaResponse criar(CriarVendaRequest request) {
        Venda venda = new Venda();
        venda.setDataVenda(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")));
        venda.setClienteVenda(clientes.buscarEntidadeObrigatoria(
                FormatadorDocumento.formatarCpf(request.getClienteCpf())));
        venda.setFuncVenda(funcionarios.buscarEntidadeObrigatoria(
                FormatadorDocumento.formatarCpf(request.getFuncionarioCpf())));
        return mapearResposta(vendas.save(venda));
    }

    @Transactional
    public VendaResponse adicionarProdutos(int id, ListaProdutosRequest request) {
        Venda venda = buscarVendaObrigatoria(id);
        validarAberta(venda);
        if (request.getCodigos() == null || request.getCodigos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um produto");
        }

        for (Integer codigo : request.getCodigos()) {
            Produto produto = produtos.buscarEntidadeObrigatoria(codigo);
            venda.addProdutos(produto);
            venda.setValorTotal(venda.getValorTotal() + produto.getPrecoProd());
        }
        vendas.saveAndFlush(venda);
        venda.getProdutos().forEach(entityManager::refresh);
        return mapearResposta(venda);
    }

    @Transactional
    public VendaResponse pagar(int id, List<PagamentoRequest> request) {
        Venda venda = buscarVendaObrigatoria(id);
        validarAberta(venda);
        if (request == null || request.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe as formas de pagamento");
        }

        double restante = venda.getValorTotal();
        List<VendaPagamento> pagamentos = new java.util.ArrayList<>();
        for (PagamentoRequest item : request) {
            MetodoPagamento metodo = metodos.buscarEntidadeObrigatoria(item.getMetodoPagamentoId());
            double valor = Math.min(item.getValorPago(), restante);
            VendaPagamento pagamento = new VendaPagamento();
            pagamento.setVenda(venda);
            pagamento.setMetodoPagamento(metodo);
            pagamento.setValorPago(valor);
            pagamentos.add(pagamento);
            restante -= valor;
            if (restante <= 0) {
                break;
            }
        }

        venda.getPagamentos().clear();
        for (VendaPagamento pagamento : pagamentos) {
            venda.addPagamentos(pagamento);
        }
        vendas.saveAndFlush(venda);
        entityManager.createNativeQuery("EXEC dbo.finalizar_venda :vendaId")
                .setParameter("vendaId", id)
                .executeUpdate();
        entityManager.refresh(venda);
        venda.getProdutos().forEach(entityManager::refresh);
        return mapearResposta(venda);
    }

    @Transactional
    public VendaResponse cancelar(int id) {
        Venda venda = buscarVendaObrigatoria(id);
        validarAberta(venda);
        venda.setStatus(2);
        return mapearResposta(vendas.save(venda));
    }

    private Venda buscarVendaObrigatoria(int id) {
        return vendas.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Venda não encontrada"));
    }

    private void validarAberta(Venda venda) {
        if (venda.getStatus() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venda já está finalizada");
        }
        if (venda.getStatus() == 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venda já está cancelada");
        }
    }

    private boolean corresponde(Venda venda, String q) {
        String termo = q.toLowerCase(Locale.ROOT);
        return contem(venda.getClienteVenda().getNomePessoa(), termo)
                || contem(venda.getFuncVenda().getNomePessoa(), termo)
                || contem(venda.getDataVenda(), termo)
                || String.valueOf(venda.getValorTotal()).contains(termo)
                || venda.getStatusString().toLowerCase(Locale.ROOT).contains(termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    private VendaResponse mapearResposta(Venda venda) {
        VendaResponse response = new VendaResponse();
        response.setCodVenda(venda.getCodVenda());
        response.setDataVenda(venda.getDataVenda());
        response.setClienteVenda(clientes.mapearResposta(venda.getClienteVenda()));
        response.setFuncVenda(funcionarios.mapearResposta(venda.getFuncVenda()));
        response.setValorTotal(venda.getValorTotal());
        response.setStatus(venda.getStatus());
        response.setStatusString(venda.getStatusString());
        response.setProdutos(venda.getProdutos().stream().map(produtos::mapearResposta).toList());
        response.setPagamentos(venda.getPagamentos().stream().map(this::mapearPagamento).toList());
        return response;
    }

    private VendaPagamentoResponse mapearPagamento(VendaPagamento pagamento) {
        VendaPagamentoResponse response = new VendaPagamentoResponse();
        response.setCodVendaPagamento(pagamento.getCodVendaPagamento());
        response.setMetodoPagamento(metodos.mapearResposta(pagamento.getMetodoPagamento()));
        response.setValorPago(pagamento.getValorPago());
        return response;
    }
}
