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
public class MetodoPagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_metodo_pagamento")
    private int codMetodoPag;
    @Column(name = "descricao_metodo_pagamento")
    private String descMetodoPag;
    @Column(name = "is_active_metodo_pagamento")
    private boolean isActiveMetodoPag = true;

    public void showMetodoPag(){
        System.out.println(
            "CÓDIGO: " + this.getCodMetodoPag() + " > " + this.getDescMetodoPag() + ", " + (this.isActiveMetodoPag() ? "ativo" : "desativado"));
    }

    public void copyFrom(MetodoPagamento mtd){
        this.setCodMetodoPag(mtd.getCodMetodoPag());
        this.setDescMetodoPag(mtd.getDescMetodoPag());
        this.setActiveMetodoPag(mtd.isActiveMetodoPag());
    }

    public boolean searchByTerm(String termo) {
        return this.getDescMetodoPag().toUpperCase().contains(termo.toUpperCase()) ||
        (this.isActiveMetodoPag() ? "ativo" : "desativado").toUpperCase().contains(termo.toUpperCase());
    }
}