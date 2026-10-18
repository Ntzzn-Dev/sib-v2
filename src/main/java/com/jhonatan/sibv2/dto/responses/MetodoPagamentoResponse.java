package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetodoPagamentoResponse {
    private int codMetodoPag;
    private String descMetodoPag;
    private boolean activeMetodoPag;
}
