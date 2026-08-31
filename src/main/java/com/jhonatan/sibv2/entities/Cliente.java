package com.jhonatan.sibv2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Cliente extends Pessoas{

    public void showProp(){
        super.showPessoa();
    }

    public void copyFrom(Cliente client){
        this.setCpfPessoa(client.getCpfPessoa());
        this.setNomePessoa(client.getNomePessoa());
        this.setTelPessoa(client.getTelPessoa());
        this.setEndPessoa(client.getEndPessoa());
    }

    public boolean searchByTerm(String termo) {
        return this.getNomePessoa().toUpperCase().contains(termo.toUpperCase()) ||
           this.getCpfPessoa().toUpperCase().contains(termo.toUpperCase()) ||
           this.getTelPessoa().toUpperCase().contains(termo.toUpperCase());
    }
}