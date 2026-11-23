package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FuncionarioResponse {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private String email;
    private BigDecimal salarioFunc;
    private int perfil;
    private EnderecoResponse endPessoa;
    private boolean ativo;
}
