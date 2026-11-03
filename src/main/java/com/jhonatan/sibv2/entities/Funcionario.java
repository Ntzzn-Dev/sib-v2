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
    @Column(name = "salario")
    private BigDecimal salarioFunc = BigDecimal.ZERO;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "senha")
    private String senha;

}
