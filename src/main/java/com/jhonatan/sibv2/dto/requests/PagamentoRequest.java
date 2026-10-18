package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PagamentoRequest {
    private Integer metodoPagamentoId;
    private double valorPago;
}
