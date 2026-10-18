package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetodoPagamentoRequest {
    private int codMetodoPag;
    private String descMetodoPag;
    private boolean activeMetodoPag = true;
}
