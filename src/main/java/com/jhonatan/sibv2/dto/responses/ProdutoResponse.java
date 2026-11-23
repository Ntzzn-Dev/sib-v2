package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProdutoResponse {
    private int codProd;
    private String descProd;
    private String tamanhoProd;
    private MarcaResponse marcaProd;
    private CategoriaResponse catProd;
    private boolean selled;
    private boolean disponivel;
    private BigDecimal precoProd;
    private boolean ativo;
}
