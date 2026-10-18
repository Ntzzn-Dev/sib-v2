package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VendaPagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codVendaPagamento;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    @JsonIgnore
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "metodo_pagamento_id")
    private MetodoPagamento metodoPagamento;
    private double valorPago;

    public void copyFrom(VendaPagamento vdPg){
        this.setMetodoPagamento(vdPg.getMetodoPagamento());
        this.setValorPago(vdPg.getValorPago());
    }

    public boolean searchByTerm(String termo) {
        return this.getMetodoPagamento().getDescMetodoPag().toUpperCase().contains(termo.toUpperCase());
    }
}
