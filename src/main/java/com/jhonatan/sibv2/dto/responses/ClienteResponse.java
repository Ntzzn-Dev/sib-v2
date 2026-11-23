package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ClienteResponse {
    private String cpfPessoa;
    private String nomePessoa;
    private String telPessoa;
    private EnderecoResponse endPessoa;
    private int totalVendas;
    private int vendasFinalizadas;
    private BigDecimal totalGasto;
    private LocalDateTime ultimaVenda;
    private boolean ativo;
}
