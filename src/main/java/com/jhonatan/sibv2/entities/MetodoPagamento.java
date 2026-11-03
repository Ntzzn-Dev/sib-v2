package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metodo_pagamento")
@NoArgsConstructor
@Data
public class MetodoPagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_pagamento")
    private int codMetodoPag;
    @Column(name = "desc_pagamento", length = 50, nullable = false)
    private String descMetodoPag;
    @Column(name = "is_active")
    private boolean isActiveMetodoPag = true;

}
