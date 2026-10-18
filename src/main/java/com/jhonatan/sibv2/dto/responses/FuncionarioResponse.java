package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FuncionarioResponse {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private double salarioFunc;
    private EnderecoResponse endPessoa;
}
