package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String cpfPessoa;
    private String nomePessoa;
    private int perfil;
    private String message;
}
