package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(nullable = true, length = 5)
    private String numero;
    @Column(nullable = true, length = 10)
    private String complemento;

    public void copyFrom(Endereco end){
        this.setCodEnd(end.getCodEnd());
        this.setUf(end.getUf());
        this.setCep(end.getCep());
        this.setCidade(end.getCidade());
        this.setBairro(end.getBairro());
        this.setLogradouro(end.getLogradouro());
        this.setNumero(end.getNumero());
        this.setComplemento(end.getComplemento());
    }

    public boolean searchByTerm(String termo) {
        return this.getBairro().toUpperCase().contains(termo.toUpperCase()) ||
           this.getCep().toUpperCase().contains(termo.toUpperCase()) ||
           this.getCidade().toUpperCase().contains(termo.toUpperCase()) ||
           this.getLogradouro().toUpperCase().contains(termo.toUpperCase()) ||
           this.getComplemento().toUpperCase().contains(termo.toUpperCase()) ||
           this.getNumero().toUpperCase().contains(termo.toUpperCase()) ||
           this.getUf().toUpperCase().contains(termo.toUpperCase());
    }

    public void showProp(){
        System.out.println(
            "CÓDIGO: " + this.getCodEnd() + " > " + this.getBairro() + ", " + this.getLogradouro() + ", " + this.getNumero() + "-" + this.getComplemento() + "\n[" + this.getUf() + "] " + this.getCidade() + ", " + this.getCep());
    }
}