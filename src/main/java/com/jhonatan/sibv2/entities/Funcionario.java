package com.jhonatan.sibv2.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Scanner;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Funcionario extends Pessoas{
    @Column(name = "salario_funcionario")
    private double salarioFunc;
    private String senha;

    public boolean validarSenha(Scanner entrada){
        System.out.println("SENHA: ");
        String senha = entrada.nextLine();
        boolean validada = this.senha == senha;
        
        if(!validada) System.out.println("Senha incorreta!");
        return validada;
    }

    public void showFunc(){
        super.showPessoa();
        System.out.println("> SALARIO: " + this.getSalarioFunc());
    }

    public void copyFrom(Funcionario func){
        this.setCpfPessoa(func.getCpfPessoa());
        this.setNomePessoa(func.getNomePessoa());
        this.setTelPessoa(func.getTelPessoa());
        this.setEndPessoa(func.getEndPessoa());
        this.setSalarioFunc(func.getSalarioFunc());
        this.setSenha(func.getSenha());
    }

    public boolean searchByTerm(String termo) {
        return this.getNomePessoa().toUpperCase().contains(termo.toUpperCase()) ||
           this.getCpfPessoa().toUpperCase().contains(termo.toUpperCase()) ||
           this.getTelPessoa().toUpperCase().contains(termo.toUpperCase()) ||
           String.valueOf(this.getSalarioFunc()).toUpperCase().contains(termo.toUpperCase());
    }
}