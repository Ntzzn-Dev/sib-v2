# Recursos SQL

Os objetos abaixo são criados por `src/main/resources/db/02_business_rules_sqlserver.sql`. As procedures de consulta são usadas pelos serviços para listas e resumos; procedures transacionais e triggers protegem operações de venda no próprio SQL Server.

## Views

| View | Uso |
|---|---|
| `vw_vendas_resumo` | Lista vendas com status textual e informações agregadas |
| `vw_produtos_estoque` | Visão de produtos e disponibilidade no estoque |
| `vw_clientes_compras` | Clientes com dados resumidos de compras |

## Funções

| Função | Uso |
|---|---|
| `fn_venda_status_texto(@status)` | Converte o código de status da venda em texto |
| `fn_produto_disponivel(@cod_prod)` | Consulta disponibilidade de um produto |
| `fn_produtos_disponiveis()` | Retorna produtos disponíveis |
| `fn_cliente_resumo(@cpf_cliente)` | Calcula contagens e valores de compras do cliente |

## Procedures

| Procedure | Uso |
|---|---|
| `sp_vendas_listar` | Busca e filtra vendas |
| `sp_venda_resumo` | Consulta resumo de uma venda |
| `sp_venda_status_texto` | Retorna status textual |
| `sp_produtos_listar` | Busca produtos e informações de disponibilidade |
| `sp_produto_disponibilidade` | Consulta disponibilidade de um item |
| `sp_clientes_listar` | Lista clientes com resumo de compras e filtro opcional |
| `sp_cliente_resumo` | Resume compras de um cliente |
| `sp_venda_adicionar_produtos` | Valida e vincula produtos a uma venda aberta |
| `sp_venda_remover_produto` | Remove item de uma venda aberta |
| `sp_venda_pagar` | Valida métodos, valores e quitação da venda |
| `sp_venda_cancelar` | Cancela venda aberta e libera produtos |

## Triggers

| Trigger | Responsabilidade |
|---|---|
| `tr_produtos_validar` | Valida preço/tamanho e impede alterações inválidas em itens vinculados |
| `tr_produtos_situacao` | Mantém `is_selled` coerente com o vínculo `cod_venda` |
| `tr_produtos_recalcular_vendas` | Recalcula o total da venda a partir dos produtos associados |
| `tr_venda_pagamento_validar` | Impede pagamentos inválidos ou em venda que não esteja aberta |

Para as mensagens de validação e implementação completa, consulte diretamente o script SQL versionado.
