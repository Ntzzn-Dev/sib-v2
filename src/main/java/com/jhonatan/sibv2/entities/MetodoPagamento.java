package com.jhonatan.sibv2.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Data
public class MetodoPagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_metodo_pagamento")
    private int codMetodoPag;
    @Column(name = "descricao_metodo_pagamento")
    private String descMetodoPag;
    @Column(name = "is_active_metodo_pagamento")
    private boolean isActiveMetodoPag = true;

}
