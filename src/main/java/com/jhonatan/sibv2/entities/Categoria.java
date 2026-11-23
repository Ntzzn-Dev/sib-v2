package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "categorias")
@NoArgsConstructor
@Data
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_categorias")
    private int codCat;

    @NonNull
    @Column (name="nome", length=50, nullable=false)
    private String nomeCat;
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

}
