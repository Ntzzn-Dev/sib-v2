package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarcaRequest {
    private int codMarca;
    private String nomeMarca;
}
