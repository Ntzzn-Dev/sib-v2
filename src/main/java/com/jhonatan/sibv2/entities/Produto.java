package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
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

}
