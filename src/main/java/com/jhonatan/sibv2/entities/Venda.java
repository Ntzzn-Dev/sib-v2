package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_venda")
    private int codVenda;
    private String dataVenda;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente clienteVenda;
    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcVenda;
    private double valorTotal;
    @Column(check = { @CheckConstraint(name = "status_check", constraint = "status IN (0, 1, 2)") })
    private int status = 0;
    @OneToMany
    private List<Produto> produtos = new ArrayList<>();
    @OneToMany(
        mappedBy = "venda",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<VendaPagamento> pagamentos = new ArrayList<>();

    // 0 - CRIADA
    // 1 - PAGA
    // 2 - CANCELADA

    public void addProdutos(Produto produto) {
        produto.setSelled(true);
        this.produtos.add(produto);
    }

    public void addPagamentos(VendaPagamento pagamento) {
        this.pagamentos.add(pagamento);
    }

    public String getStatusString() {
        return switch (status){
            case 0 -> "EM ABERTO";
            case 1 -> "PAGA";
            case 2 -> "CANCELADA";
            default -> "ERRO";
        };
    }

    public void copyFrom(Venda venda){
        this.setCodVenda(venda.getCodVenda());
        this.setDataVenda(venda.getDataVenda());
        this.setClienteVenda(venda.getClienteVenda());
        this.setFuncVenda(venda.getFuncVenda());
        this.setValorTotal(venda.getValorTotal());
        this.setProdutos(venda.getProdutos());
        this.setPagamentos(venda.getPagamentos());
    }

    public boolean searchByTerm(String termo) {
        return this.getClienteVenda().getNomePessoa().toUpperCase().contains(termo.toUpperCase()) ||
        this.getFuncVenda().getNomePessoa().toUpperCase().contains(termo.toUpperCase()) ||
        this.getDataVenda().toUpperCase().contains(termo.toUpperCase()) ||
        String.valueOf(this.getValorTotal()).toUpperCase().contains(termo.toUpperCase()) ||
        this.getStatusString().toUpperCase().contains(termo.toUpperCase());
    }
}
