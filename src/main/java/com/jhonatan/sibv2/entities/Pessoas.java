package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "pessoas")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
@AllArgsConstructor
@Data
public abstract class Pessoas {
    @Id
    @NonNull
    @Column(length = 14, nullable = false)
    private String cpfPessoa;
    @Column(length = 50, nullable = false)
    private String nomePessoa;
    @Column(length = 11, nullable = true)
    private String telPessoa;
    @OneToOne
    private Endereco endPessoa;

}
