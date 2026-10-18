package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClienteRequest {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private EnderecoRequest endPessoa;
}
