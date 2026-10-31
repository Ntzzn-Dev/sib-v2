package com.jhonatan.sibv2.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Funcionario extends Pessoas{
    @Column(name = "salario_funcionario")
    private double salarioFunc;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

}
