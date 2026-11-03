package com.jhonatan.sibv2.entities;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VendaPagamentoId implements Serializable {
    private int metodoPagamento;
    private int venda;
}
