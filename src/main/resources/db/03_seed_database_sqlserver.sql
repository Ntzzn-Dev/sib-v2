USE brecho;
GO

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- Executar uma unica vez, logo apos recriar o banco com os scripts 01 e 02.

/* ================================================================
   1. PESSOAS
   ================================================================ */
INSERT INTO pessoas (cpf, nome, telefone) VALUES
    ('10000000001', 'Ana Beatriz Martins', '17990001001'),
    ('10000000002', 'Bruno Henrique Souza', '17990001002'),
    ('10000000003', 'Carla Oliveira Santos', NULL),
    ('10000000004', 'Diego Ferreira Lima', '17990001004'),
    ('10000000005', 'Eduarda Costa Ribeiro', '17990001005'),
    ('10000000006', 'Felipe Almeida Rocha', NULL),
    ('10000000007', 'Gabriela Mendes Silva', '17990001007'),
    ('10000000008', 'Heitor Barbosa Nunes', '17990001008'),
    ('10000000009', 'Isabela Carvalho Alves', NULL),
    ('10000000010', 'Joao Pedro Moreira', '17990001010'),
    ('20000000001', 'Mariana Lopes Andrade', '17991111001'),
    ('20000000002', 'Jhonatan Henryck de Souza Alves', '17991111002'),
    ('20000000003', 'Camila Rodrigues Freitas', NULL),
    ('20000000004', 'Thiago Cardoso Melo', '17991111004');

/* ================================================================
   2. CLIENTES
   ================================================================ */
INSERT INTO clientes (pessoa_cpf) VALUES
    ('10000000001'),
    ('10000000002'),
    ('10000000003'),
    ('10000000004'),
    ('10000000005'),
    ('10000000006'),
    ('10000000007'),
    ('10000000008'),
    ('10000000009'),
    ('10000000010');

/* ================================================================
   3. FUNCIONARIOS
   ================================================================ */
INSERT INTO funcionarios (pessoa_cpf, salario, perfil, email, senha) VALUES
    ('20000000002', 3300.50, 0, 'jhonatan.alves@brecho.com', 'JhonNath'),
    ('20000000001', 3500.00, 1, 'mariana@brecho.com',        'teste123'),
    ('20000000004', 4500.75, 0, 'thiago@brecho.com',         'teste123'),
    ('20000000003', 0.00,    1, 'camila@brecho.com',         'teste123');

/* ================================================================
   4. MARCAS
   ================================================================ */
SET IDENTITY_INSERT marcas ON;
INSERT INTO marcas (cod_marca, nome) VALUES
    (1, 'Nike'),
    (2, 'Adidas'),
    (3, 'Puma'),
    (4, 'Levi''s'),
    (5, 'Zara'),
    (6, 'Hering'),
    (7, 'Lacoste'),
    (8, 'Renner'),
    (9, 'Sem Marca');
SET IDENTITY_INSERT marcas OFF;

/* ================================================================
   5. CATEGORIAS
   ================================================================ */
SET IDENTITY_INSERT categorias ON;
INSERT INTO categorias (cod_categorias, nome) VALUES
    (1, 'Camisetas'),
    (2, 'Calcas'),
    (3, 'Vestidos'),
    (4, 'Saias'),
    (5, 'Jaquetas'),
    (6, 'Blusas'),
    (7, 'Tenis'),
    (8, 'Sapatos'),
    (9, 'Acessorios'),
    (10, 'Bolsas'),
    (11, 'Roupas Infantis');
SET IDENTITY_INSERT categorias OFF;

/* ================================================================
   6. METODOS DE PAGAMENTO
   ================================================================ */
SET IDENTITY_INSERT metodo_pagamento ON;
INSERT INTO metodo_pagamento (cod_pagamento, desc_pagamento, is_active) VALUES
    (1, 'Dinheiro', 1),
    (2, 'Pix', 1),
    (3, 'Cartao de Debito', 1),
    (4, 'Cartao de Credito', 1),
    (5, 'Transferencia', 1),
    (6, 'Cheque', 0),
    (7, 'Vale Compra', 0);
SET IDENTITY_INSERT metodo_pagamento OFF;

/* ================================================================
   7. ENDERECOS
   ================================================================ */
SET IDENTITY_INSERT endereco ON;
INSERT INTO endereco (cod_end, cpf_pessoa, logradouro, complemento, numero, bairro, cidade, cep, uf) VALUES
    (1, '10000000001', 'Rua das Flores', 'Casa', '120', 'Centro', 'Sao Jose do Rio Preto', '15010-000', 'SP'),
    (2, '10000000001', 'Avenida Bady Bassitt', NULL, '850', 'Boa Vista', 'Sao Jose do Rio Preto', '15025-000', 'SP'),
    (3, '10000000002', 'Rua Sao Paulo', 'Apto 32', '450', 'Vila Ercilia', 'Sao Jose do Rio Preto', '15013-000', 'SP'),
    (4, '10000000003', 'Rua do Comercio', NULL, '15', 'Centro', 'Mirassol', '15130-000', 'SP'),
    (5, '10000000004', 'Avenida Brasil', 'Fundos', '900', 'Jardim America', 'Sao Jose do Rio Preto', '15045-000', 'SP'),
    (6, '10000000005', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
    (7, '10000000006', 'Rua Um', NULL, 'S/N', 'Residencial', 'Bady Bassitt', '15115-000', 'SP'),
    (8, NULL, 'Rua sem cadastro', NULL, '10', 'Centro', 'Sao Jose do Rio Preto', '15000-000', 'SP'),
    (9, '10000000008', 'Rua das Acacias', 'Bloco B', '77', 'Jardim Europa', 'Catanduva', '15800-000', 'SP');
SET IDENTITY_INSERT endereco OFF;

/* ================================================================
   8. VENDAS - status: 0 (aberta), 1 (paga), 2 (cancelada).
   ================================================================ */
SET IDENTITY_INSERT vendas ON;
INSERT INTO vendas(cod_venda, data_venda, cpf_cliente, cpf_funcionario, endereco_entrega, valor_total, status) VALUES
    (1,  '2026-10-05T10:15:00', '10000000001', '20000000001', 1,  89.90, 1),
    (2,  '2026-10-06T14:30:00', '10000000002', '20000000002', 3, 159.80, 1),
    (3,  '2026-10-08T09:05:00', '10000000003', '20000000001', 4,  45.00, 0),
    (4,  '2026-10-10T16:45:00', '10000000004', '20000000003', 5,   0.00, 1),
    (5,  '2026-10-12T11:20:00', '10000000005', '20000000004', 6, 220.00, 2),
    (6,  '2026-10-15T13:00:00', '10000000006', '20000000002', 7, 310.50, 1),
    (7,  '2026-11-01T10:00:00', '10000000007', '20000000001', NULL, 75.00, 0),
    (8,  '2026-11-03T17:10:00', '10000000008', '20000000003', 9, 129.99, 1),
    (9,  '2026-11-05T12:40:00', '10000000009', '20000000004', NULL, 75.00, 0),
    (10, '2026-11-07T15:25:00', '10000000010', '20000000002', NULL, 499.90, 1),
    (11, '2026-11-10T08:50:00', '10000000001', '20000000003', 1,  35.00, 2),
    (12, '2026-11-12T18:00:00', '10000000002', '20000000001', 3, 180.00, 0);
SET IDENTITY_INSERT vendas OFF;

/* ================================================================
   9. PRODUTOS
   ================================================================ */
SET IDENTITY_INSERT produtos ON;
INSERT INTO produtos (cod_prod, descricao, tamanho, cod_marca, cod_categoria, cod_venda, preco, is_selled) VALUES
    (1,  'Camiseta basica branca',       'M',  1,  1, NULL,  39.90, 0),
    (2,  'Calca jeans reta',             '42', 4,  2, NULL,  50.00, 0),
    (3,  'Vestido floral',               'P',  5,  3, NULL,  89.90, 0),
    (4,  'Saia jeans',                   'M',  4,  4, NULL,  69.90, 0),
    (5,  'Jaqueta esportiva',            'G',  2,  5, NULL, 120.00, 0),
    (6,  'Blusa de frio',                'GG', 6,  6,  3,   45.00, 1),
    (7,  'Tenis casual',                 '40', 1,  7,  7,  180.50, 1),
    (8,  'Sapato social',                '41', NULL, 8, NULL, 95.00, 0),
    (9,  'Colar artesanal',              'P', NULL, 9, NULL,   0.00, 0),
    (10, 'Bolsa transversal',            'P',  8, 10, NULL, 129.99, 0),
    (11, 'Camiseta infantil',            'P',  9, 11, NULL,  25.00, 0),
    (12, 'Calca legging',                'G',  3,  2, NULL,  79.90, 0),
    (13, 'Camiseta estampada',           'G',  2,  1, NULL,  70.00, 0),
    (14, 'Vestido longo',                'GG', 5,  3, NULL, 150.00, 0),
    (15, 'Cinto de couro',               'P', NULL, 9,  12,  35.00, 1),
    (16, 'Jaqueta jeans',                'M',  4,  5,  12, 145.00, 1),
    (17, 'Blusa sem manga',              'P',  6,  6, NULL,   0.00, 0),
    (18, 'Tenis de corrida',             '39', 3,  7, NULL, 210.00, 0),
    (19, 'Bolsa pequena',                'P',  8, 10, NULL, 220.00, 0),
    (20, 'Saia plissada',                'M',  5,  4, NULL,  35.00, 0),
    (21, 'Camiseta oversized',           'GG', 1,  1, NULL,  59.90, 0),
    (22, 'Sapato casual',                '42', NULL, 8, NULL, 110.00, 0);
SET IDENTITY_INSERT produtos OFF;

/* ================================================================
   10. VENDA_PAGAMENTO
   ================================================================ */
INSERT INTO venda_pagamento (cod_pagamento, cod_venda, valor_pago) VALUES
    (2,  3,  45.00), -- Pix em venda aberta
    (4,  7, 100.00), -- Cartao de credito em venda aberta
    (2,  7,  80.50),
    (1,  9,  25.00), -- Pagamento parcial em venda aberta
    (2, 12, 100.00), -- Pagamento parcial em venda aberta
    (5, 12,  80.00); -- Transferencia, metodo ativo

COMMIT TRANSACTION;
