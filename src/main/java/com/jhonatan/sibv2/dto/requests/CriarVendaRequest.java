package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CriarVendaRequest {
    private String clienteCpf;
    private String funcionarioCpf;
}
