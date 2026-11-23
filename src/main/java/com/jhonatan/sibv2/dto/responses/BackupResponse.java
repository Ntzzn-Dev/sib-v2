package com.jhonatan.sibv2.dto.responses;

import lombok.Data;

@Data
public class BackupResponse {
    private final String arquivo;
    private final String caminho;
    private final String criadoEm;

    public BackupResponse(String arquivo, String caminho, String criadoEm) {
        this.arquivo = arquivo;
        this.caminho = caminho;
        this.criadoEm = criadoEm;
    }
}
