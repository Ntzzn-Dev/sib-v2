package com.jhonatan.sibv2.dto.responses;

import lombok.Data;

@Data
public class ConfiguracaoEmailResponse {
    private String emailRemetente;
    private boolean senhaConfigurada;

    public ConfiguracaoEmailResponse(String emailRemetente, boolean senhaConfigurada) {
        this.emailRemetente = emailRemetente;
        this.senhaConfigurada = senhaConfigurada;
    }
}
