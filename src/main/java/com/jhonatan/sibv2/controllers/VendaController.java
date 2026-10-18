package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.*;
import com.jhonatan.sibv2.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin
public class VendaController {

    private final VendaRepository vendas;
    private final ClienteRepository clientes;
    private final FuncionarioRepository funcionarios;
    private final ProdutoRepository produtos;
    private final MetodoPagamentoRepository metodos;

    public VendaController(VendaRepository vendas, ClienteRepository clientes,
                           FuncionarioRepository funcionarios, ProdutoRepository produtos,
                           MetodoPagamentoRepository metodos) {
        this.vendas = vendas;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.produtos = produtos;
        this.metodos = metodos;
    }

    @GetMapping
    public List<Venda> listar(@RequestParam(required = false) Integer status, @RequestParam(required = false) String q) {
        if (status != null && (status < 0 || status > 2)) {
            invalid("Status deve ser 0 (aberta), 1 (paga) ou 2 (cancelada)");
        }

        return vendas.findAll().stream().filter(v -> status == null || v.getStatus() == status)
                .filter(v -> q == null || q.isBlank() || has(v.getClienteVenda().getNomePessoa(), q)
                        || has(v.getFuncVenda().getNomePessoa(), q) || has(v.getDataVenda(), q)
                        || has(String.valueOf(v.getValorTotal()), q) || has(v.getStatusString(), q)).toList();
    }

    @GetMapping("/{id}")
    public Venda buscar(@PathVariable int id) {
        return required(vendas, id, "Venda");
    }

    @PostMapping
    public Venda criar(@RequestBody CriarVendaRequest request) {
        Venda venda = new Venda();
        venda.setDataVenda(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")));
        venda.setClienteVenda(required(clientes, cpf(request.clienteCpf()), "Cliente"));
        venda.setFuncVenda(required(funcionarios, cpf(request.funcionarioCpf()), "Funcionário"));
        return vendas.save(venda);
    }

    @PostMapping("/{id}/produtos")
    @Transactional
    public Venda adicionarProdutos(@PathVariable int id, @RequestBody ListaProdutos request) {
        Venda venda = required(vendas, id, "Venda");
        aberta(venda);

        if (request.codigos() == null || request.codigos().isEmpty()) {
            invalid("Informe ao menos um produto");
        }

        for (Integer codigo : request.codigos()) {
            Produto produto = required(produtos, codigo, "Produto");

            if (produto.isSelled()) {
                invalid("Produto " + codigo + " já foi vendido");
            }

            produto.setSelled(true);
            venda.getProdutos().add(produto);
            venda.setValorTotal(venda.getValorTotal() + produto.getPrecoProd());
        }

        return vendas.save(venda);
    }

    @PostMapping("/{id}/pagamentos")
    @Transactional
    public Venda pagar(@PathVariable int id, @RequestBody List<PagamentoRequest> request) {
        Venda venda = required(vendas, id, "Venda"); aberta(venda);

        if (venda.getValorTotal() <= 0) {
            invalid("Adicione produtos antes de pagar");
        }

        if (request == null || request.isEmpty()) {
            invalid("Informe as formas de pagamento");
        }

        double restante = venda.getValorTotal();
        List<VendaPagamento> pagamentos = new ArrayList<>();

        for (PagamentoRequest item : request) {
            MetodoPagamento metodo = required(metodos, item.metodoPagamentoId(), "Método de pagamento");

            if (!metodo.isActiveMetodoPag()) {
                invalid("Método desabilitado: " + metodo.getDescMetodoPag());
            }

            if (item.valorPago() <= 0) {
                invalid("O valor pago deve ser maior que zero");
            }

            double valor = Math.min(item.valorPago(), restante);
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

        if (restante > 0.0000001) {
            invalid("Pagamento incompleto: faltam R$ " + String.format(Locale.ROOT, "%.2f", restante));
        }

        venda.getPagamentos().clear();
        venda.getPagamentos().addAll(pagamentos);
        venda.setStatus(1);
        return vendas.save(venda);
    }

    @PostMapping("/{id}/cancelamento")
    @Transactional
    public Venda cancelar(@PathVariable int id) {
        Venda venda = required(vendas, id, "Venda");
        aberta(venda);
        venda.setStatus(2);
        return vendas.save(venda);
    }

    private static void aberta(Venda venda) {
        if (venda.getStatus() == 1) invalid("Venda já está finalizada");
        if (venda.getStatus() == 2) invalid("Venda já está cancelada");
    }

    public record CriarVendaRequest(String clienteCpf, String funcionarioCpf) {}
    public record ListaProdutos(List<Integer> codigos) {}
    public record PagamentoRequest(Integer metodoPagamentoId, double valorPago) {}
}
