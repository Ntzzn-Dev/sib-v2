package com.jhonatan.sibv2.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileRequest {
    private String email;
    private String nomePessoa;
    private String telPessoa;
    private String senhaAtual;
    private String novaSenha;
    private EnderecoRequest endPessoa;
}
