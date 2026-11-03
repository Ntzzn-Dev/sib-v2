package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "marcas")
@NoArgsConstructor
@Data
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_marca")
    private int codMarca;
    @Column(name = "nome", length = 50, nullable = false)
    private String nomeMarca;
}
