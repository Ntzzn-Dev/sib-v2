package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_endereco")
    private int codEnd;
    @Column(nullable = false, length = 2)
    private String uf;
    @Column(nullable = false, length = 9)
    private String cep;
    @Column(nullable = false, length = 50)
    private String cidade;
    @Column(nullable = false, length = 50)
    private String bairro;
    @Column(nullable = false, length = 50)
    private String logradouro;
    @Column(length = 5)
    private String numero;
    @Column(length = 10)
    private String complemento;

}
