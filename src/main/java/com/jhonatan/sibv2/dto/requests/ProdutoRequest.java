package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProdutoRequest {
    private int codProd;
    private String descProd;
    private String tamanhoProd;
    private MarcaRequest marcaProd;
    private CategoriaRequest catProd;
    private BigDecimal precoProd = BigDecimal.ZERO;
}
