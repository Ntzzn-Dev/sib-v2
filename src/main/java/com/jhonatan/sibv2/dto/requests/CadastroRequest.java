package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CadastroRequest {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private double salarioFunc;
    private String senha;
    private EnderecoRequest endPessoa;
}
