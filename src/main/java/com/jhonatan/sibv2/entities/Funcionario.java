package com.jhonatan.sibv2.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "funcionarios")
@PrimaryKeyJoinColumn(name = "pessoa_cpf", referencedColumnName = "cpf")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Funcionario extends Pessoas{
    public static final int PERFIL_ADMIN = 0;
    public static final int PERFIL_ATENDENTE = 1;

    @Column(name = "salario")
    private BigDecimal salarioFunc = BigDecimal.ZERO;
    @Column(name = "perfil", nullable = false)
    private int perfil = PERFIL_ATENDENTE;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "senha", nullable = false)
    private String senha;

}
