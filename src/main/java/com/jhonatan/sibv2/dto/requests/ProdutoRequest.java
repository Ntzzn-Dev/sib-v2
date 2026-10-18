package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProdutoRequest {
    private int codProd;
    private String descProd;
    private String tamanhoProd;
    private MarcaRequest marcaProd;
    private CategoriaRequest catProd;
    private double precoProd;
}
