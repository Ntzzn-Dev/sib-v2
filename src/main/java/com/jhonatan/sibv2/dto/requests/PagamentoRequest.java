package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PagamentoRequest {
    private Integer metodoPagamentoId;
    private BigDecimal valorPago = BigDecimal.ZERO;
}
