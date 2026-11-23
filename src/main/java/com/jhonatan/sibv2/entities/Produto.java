package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@NoArgsConstructor
@Data
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_prod")
    private int codProd;
    @Column(name = "descricao", length = 50, nullable = false)
    private String descProd;
    @Column(name = "tamanho", length = 3, nullable = false)
    private String tamanhoProd;
    @ManyToOne
    @JoinColumn(name = "cod_marca")
    private Marca marcaProd;
    @ManyToOne
    @JoinColumn(name = "cod_categoria")
    private Categoria catProd;
    @Column(name = "is_selled")
    private boolean isSelled;
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
    @Column(name = "preco")
    private BigDecimal precoProd = BigDecimal.ZERO;
    @ManyToOne
    @JoinColumn(name = "cod_venda")
    private Venda vendaProd;

}
