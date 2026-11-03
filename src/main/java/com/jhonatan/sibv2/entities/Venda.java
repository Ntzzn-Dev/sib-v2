package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas")
@NoArgsConstructor
@Data
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_venda")
    private int codVenda;
    @Column(name = "data")
    private java.time.LocalDateTime dataVenda;
    @ManyToOne
    @JoinColumn(name = "cpf_cliente", referencedColumnName = "pessoa_cpf")
    private Cliente clienteVenda;
    @ManyToOne
    @JoinColumn(name = "cpf_funcionario", referencedColumnName = "pessoa_cpf")
    private Funcionario funcVenda;
    @Column(name = "valor_total")
    private BigDecimal valorTotal = BigDecimal.ZERO;
    @Column(check = { @CheckConstraint(name = "status_check", constraint = "status IN (0, 1, 2)") })
    private int status = 0;
    @OneToMany(mappedBy = "vendaProd")
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
}
