package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "endereco")
@Data
@NoArgsConstructor
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_end")
    private int codEnd;
    @Column(name = "cpf_pessoa", length = 11)
    private String cpfPessoa;
    @Column(length = 2)
    private String uf;
    @Column(length = 9)
    private String cep;
    @Column(length = 50)
    private String cidade;
    @Column(length = 50)
    private String bairro;
    @Column(length = 50)
    private String logradouro;
    @Column(length = 50)
    private String numero;
    @Column(length = 50)
    private String complemento;
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

}
