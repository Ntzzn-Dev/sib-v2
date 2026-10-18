package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnderecoResponse {
    private int codEnd;
    private String uf;
    private String cep;
    private String cidade;
    private String bairro;
    private String logradouro;
    private String numero;
    private String complemento;
}
