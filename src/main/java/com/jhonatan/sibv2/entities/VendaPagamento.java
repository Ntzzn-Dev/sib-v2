package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VendaPagamento {
    @Id //Mudar isso
    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "metodo_pagamento_id")
    private MetodoPagamento metodoPagamento;
    private double valorPago;

    public void showVendaPag(){
        System.out.println(
            "Método:     " + this.getMetodoPagamento().getDescMetodoPag() + 
            "\nParcela R$: " + String.format("%.2f", this.getValorPago()));
    }

    public void copyFrom(VendaPagamento vdPg){
        this.setMetodoPagamento(vdPg.getMetodoPagamento());
        this.setValorPago(vdPg.getValorPago());
    }

    public boolean searchByTerm(String termo) {
        return this.getMetodoPagamento().getDescMetodoPag().toUpperCase().contains(termo.toUpperCase());
    }
}