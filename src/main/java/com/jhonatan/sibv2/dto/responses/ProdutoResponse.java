package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProdutoResponse {
    private int codProd;
    private String descProd;
    private String tamanhoProd;
    private MarcaResponse marcaProd;
    private CategoriaResponse catProd;
    private boolean selled;
    private double precoProd;
}
