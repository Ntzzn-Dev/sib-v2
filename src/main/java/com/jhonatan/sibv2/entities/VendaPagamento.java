package com.jhonatan.sibv2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "venda_pagamento")
@IdClass(VendaPagamentoId.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VendaPagamento {
    @Id
    @ManyToOne
    @JoinColumn(name = "cod_venda")
    @JsonIgnore
    private Venda venda;

    @Id
    @ManyToOne
    @JoinColumn(name = "cod_pagamento")
    private MetodoPagamento metodoPagamento;

    @Column(name = "valor_pago")
    private BigDecimal valorPago = BigDecimal.ZERO;
}
