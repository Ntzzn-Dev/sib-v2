package com.jhonatan.sibv2.dto.responses;

import lombok.Data;

@Data
public class ProfileResponse {
    private String cpfPessoa;
    private String email;
    private String nomePessoa;
    private String telPessoa;
    private int perfil;
    private EnderecoResponse endPessoa;
}
