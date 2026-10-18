package com.jhonatan.sibv2.dto.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class VendaResponse {
    private int codVenda;
    private String dataVenda;
    private ClienteResponse clienteVenda;
    private FuncionarioResponse funcVenda;
    private double valorTotal;
    private int status;
    private String statusString;
    private List<ProdutoResponse> produtos = new ArrayList<>();
    private List<VendaPagamentoResponse> pagamentos = new ArrayList<>();
}
