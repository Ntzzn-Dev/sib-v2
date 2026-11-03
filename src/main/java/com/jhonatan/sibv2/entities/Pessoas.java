package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "pessoas")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class Pessoas {
    @Id
    @NonNull
    @Column(name = "cpf", length = 11, nullable = false)
    private String cpfPessoa;
    @Column(name = "nome", length = 50, nullable = false)
    private String nomePessoa;
    @Column(name = "telefone", length = 11)
    private String telPessoa;
    @Transient
    private Endereco endPessoa;

}
