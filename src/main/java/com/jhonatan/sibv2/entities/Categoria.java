package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "categoria")
@NoArgsConstructor
@Data
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int codCat;

    @NonNull
    @Column (name="nome_categoria", length=30, nullable=false)
    private String nomeCat;

}
