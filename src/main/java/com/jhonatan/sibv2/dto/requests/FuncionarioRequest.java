package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FuncionarioRequest {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private BigDecimal salarioFunc = BigDecimal.ZERO;
    private Integer perfil;
    private String senha;
    private EnderecoRequest endPessoa;
}
