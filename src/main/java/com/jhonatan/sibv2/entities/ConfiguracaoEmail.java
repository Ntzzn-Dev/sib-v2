package com.jhonatan.sibv2.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracao_email")
@Data
@NoArgsConstructor
public class ConfiguracaoEmail {
    @Id
    @Column(name = "cod_configuracao")
    private Integer codConfiguracao;

    @Column(name = "email_remetente", nullable = false, length = 254)
    private String emailRemetente;

    @Column(name = "senha_app", nullable = false, length = 100)
    private String senhaApp;
}
