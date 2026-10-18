package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClienteResponse {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private EnderecoResponse endPessoa;
}
