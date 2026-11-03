package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VendaPagamentoResponse {
    private Long codVendaPagamento;
    private MetodoPagamentoResponse metodoPagamento;
    private BigDecimal valorPago;
}
