package com.jhonatan.sibv2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "pessoa_cpf", referencedColumnName = "cpf")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class Cliente extends Pessoas {}
