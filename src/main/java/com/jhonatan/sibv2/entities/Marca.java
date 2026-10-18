package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Data
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int codMarca;
    private String nomeMarca;
    
    public void copyFrom(Marca marca){
        this.setCodMarca(marca.getCodMarca());
        this.setNomeMarca(marca.getNomeMarca());
    }

    public boolean searchByTerm(String termo) {
        return this.getNomeMarca().toUpperCase().contains(termo.toUpperCase());
    }
}
