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
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_produto")
    private int codProd;
    @Column(name = "descricao_produto")
    private String descProd;
    @Column(name = "tamanho_produto")
    private String tamanhoProd;
    @ManyToOne
    @JoinColumn(name = "marca_id")
    private Marca marcaProd;
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria catProd;
    private boolean isSelled;
    @Column(name = "preco_produto")
    private double precoProd;

    public void showProp(){
        System.out.println(
            "CÓDIGO: " + this.getCodProd() + " > " + this.getMarcaProd().getNomeMarca() + " - " + 
            this.getMarcaProd().getNomeMarca() + "\n[" + this.getTamanhoProd() + "] " + this.getCatProd().getNomeCat() + 
            ", R$: " + this.getPrecoProd() + "\nproduto " + (this.isSelled() ? "vendido" : "disponível"));
    }

    public void copyFrom(Produto prod){
        this.setCodProd(prod.getCodProd());
        this.setDescProd(prod.getDescProd());
        this.setTamanhoProd(prod.getTamanhoProd());
        this.setMarcaProd(prod.getMarcaProd());
        this.setCatProd(prod.getCatProd());
    }

    public boolean searchByTerm(String termo) {
        
        return this.getDescProd().toUpperCase().contains(termo.toUpperCase()) ||
        this.getTamanhoProd().toUpperCase().contains(termo.toUpperCase()) ||
        this.getMarcaProd().getNomeMarca().toUpperCase().contains(termo.toUpperCase()) ||
        this.getCatProd().getNomeCat().toUpperCase().contains(termo.toUpperCase());
    }
}