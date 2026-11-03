package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class VendaResponse {
    private int codVenda;
    private LocalDateTime dataVenda;
    private ClienteResponse clienteVenda;
    private FuncionarioResponse funcVenda;
    private BigDecimal valorTotal;
    private BigDecimal totalPago;
    private long quantidadeProdutos;
    private int status;
    private String statusString;
    private List<ProdutoResponse> produtos = new ArrayList<>();
    private List<VendaPagamentoResponse> pagamentos = new ArrayList<>();
}
