package com.jhonatan.sibv2.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoResponse {
    private String uf;
    private String nome;
}
