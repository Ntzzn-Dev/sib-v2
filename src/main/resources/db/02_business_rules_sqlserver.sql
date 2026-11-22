USE brecho;
GO

/* Função escalar: centraliza o texto apresentado para o estado da venda. */
CREATE OR ALTER FUNCTION dbo.fn_venda_status_texto(@status INT)
RETURNS VARCHAR(20)
AS
BEGIN
    RETURN CASE @status
        WHEN 0 THEN 'EM ABERTO'
        WHEN 1 THEN 'PAGA'
        WHEN 2 THEN 'CANCELADA'
        ELSE 'ERRO'
    END;
END;
GO

/* Função escalar usada para responder se um produto está no estoque. */
CREATE OR ALTER FUNCTION dbo.fn_produto_disponivel(@cod_prod INT)
RETURNS BIT
AS
BEGIN
    DECLARE @disponivel BIT = 0;
    IF EXISTS (SELECT 1 FROM dbo.produtos
               WHERE cod_prod=@cod_prod AND cod_venda IS NULL AND is_selled=0)
        SET @disponivel = 1;
    RETURN @disponivel;
END;
GO

/* Função com retorno tabular inline para consulta do estoque disponível. */
CREATE OR ALTER FUNCTION dbo.fn_produtos_disponiveis()
RETURNS TABLE
AS
RETURN
    SELECT p.cod_prod, p.descricao, p.tamanho, p.preco,
           p.cod_marca, m.nome AS marca_nome,
           p.cod_categoria, c.nome AS categoria_nome
    FROM dbo.produtos p
    LEFT JOIN dbo.marcas m ON m.cod_marca=p.cod_marca
    JOIN dbo.categorias c ON c.cod_categorias=p.cod_categoria
    WHERE p.cod_venda IS NULL AND p.is_selled=0;
GO

/* Função multi-statement: devolve indicadores de compra de um cliente. */
CREATE OR ALTER FUNCTION dbo.fn_cliente_resumo(@cpf_cliente VARCHAR(11))
RETURNS @resumo TABLE (
    cpf_cliente VARCHAR(11) NOT NULL,
    total_vendas INT NOT NULL,
    vendas_finalizadas INT NOT NULL,
    total_gasto DECIMAL(12,2) NOT NULL,
    ultima_venda DATETIME NULL
)
AS
BEGIN
    INSERT INTO @resumo(cpf_cliente,total_vendas,vendas_finalizadas,total_gasto,ultima_venda)
    SELECT c.pessoa_cpf,
           COUNT(v.cod_venda),
           COALESCE(SUM(CASE WHEN v.status=1 THEN 1 ELSE 0 END),0),
           COALESCE(SUM(CASE WHEN v.status=1 THEN v.valor_total ELSE 0 END),0),
           MAX(v.data_venda)
    FROM dbo.clientes c
    LEFT JOIN dbo.vendas v ON v.cpf_cliente=c.pessoa_cpf
    WHERE c.pessoa_cpf=@cpf_cliente
    GROUP BY c.pessoa_cpf;
    RETURN;
END;
GO

/* Visão 1: resumo transacional das vendas para a interface administrativa. */
CREATE OR ALTER VIEW dbo.vw_vendas_resumo
AS
SELECT v.cod_venda, v.data_venda, v.cpf_cliente, pc.nome AS cliente_nome,
       v.cpf_funcionario, pf.nome AS funcionario_nome,
       v.valor_total, v.status,
       dbo.fn_venda_status_texto(v.status) AS status_texto,
       COALESCE(pg.total_pago,0) AS total_pago,
       COALESCE(pr.quantidade_produtos,0) AS quantidade_produtos
FROM dbo.vendas v
JOIN dbo.pessoas pc ON pc.cpf=v.cpf_cliente
JOIN dbo.pessoas pf ON pf.cpf=v.cpf_funcionario
OUTER APPLY (SELECT SUM(vp.valor_pago) AS total_pago
             FROM dbo.venda_pagamento vp WHERE vp.cod_venda=v.cod_venda) pg
OUTER APPLY (SELECT COUNT(*) AS quantidade_produtos
             FROM dbo.produtos p WHERE p.cod_venda=v.cod_venda) pr;
GO

/* Visão 2: catálogo, categoria, marca, situação de estoque e venda vinculada. */
CREATE OR ALTER VIEW dbo.vw_produtos_estoque
AS
SELECT p.cod_prod, p.descricao, p.tamanho, p.preco, p.is_selled,
       p.cod_venda, m.cod_marca, m.nome AS marca_nome,
       c.cod_categorias, c.nome AS categoria_nome,
       CASE WHEN dbo.fn_produto_disponivel(p.cod_prod)=1 THEN 'DISPONÍVEL'
            WHEN p.cod_venda IS NOT NULL THEN 'EM VENDA'
            ELSE 'VENDIDO' END AS situacao
FROM dbo.produtos p
LEFT JOIN dbo.marcas m ON m.cod_marca=p.cod_marca
JOIN dbo.categorias c ON c.cod_categorias=p.cod_categoria;
GO

/* Visão 3: carteira de clientes com métricas agregadas de vendas. */
CREATE OR ALTER VIEW dbo.vw_clientes_compras
AS
SELECT pe.cpf, pe.nome, pe.telefone, cr.total_vendas,
       cr.vendas_finalizadas, cr.total_gasto, cr.ultima_venda
FROM dbo.clientes c
JOIN dbo.pessoas pe ON pe.cpf=c.pessoa_cpf
CROSS APPLY dbo.fn_cliente_resumo(c.pessoa_cpf) cr;
GO

/* Endpoints de leitura: Java chama procedures nomeadas sem embutir SELECTs. */
CREATE OR ALTER PROCEDURE dbo.sp_vendas_listar
    @status INT = NULL,
    @q VARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT cod_venda
    FROM dbo.vw_vendas_resumo
    WHERE (@status IS NULL OR status=@status)
      AND (@q IS NULL OR cliente_nome LIKE '%' + @q + '%'
        OR funcionario_nome LIKE '%' + @q + '%'
        OR CONVERT(VARCHAR(30),data_venda,120) LIKE '%' + @q + '%'
        OR CONVERT(VARCHAR(40),valor_total) LIKE '%' + @q + '%'
        OR status_texto LIKE '%' + @q + '%')
    ORDER BY data_venda DESC,cod_venda DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_venda_resumo @venda_id INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT total_pago,quantidade_produtos
    FROM dbo.vw_vendas_resumo WHERE cod_venda=@venda_id;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_venda_status_texto @status INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT dbo.fn_venda_status_texto(@status) AS status_texto;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_produtos_listar
    @q VARCHAR(100) = NULL,
    @vendido BIT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF @vendido=0
    BEGIN
        SELECT cod_prod FROM dbo.fn_produtos_disponiveis()
        WHERE @q IS NULL OR descricao LIKE '%' + @q + '%'
          OR marca_nome LIKE '%' + @q + '%' OR categoria_nome LIKE '%' + @q + '%'
        ORDER BY descricao;
        RETURN;
    END;
    SELECT cod_prod FROM dbo.vw_produtos_estoque
    WHERE (@vendido IS NULL OR is_selled=@vendido)
      AND (@q IS NULL OR descricao LIKE '%' + @q + '%'
        OR marca_nome LIKE '%' + @q + '%' OR categoria_nome LIKE '%' + @q + '%')
    ORDER BY descricao;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_produto_disponibilidade @cod_prod INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT dbo.fn_produto_disponivel(@cod_prod) AS disponivel;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_clientes_listar @q VARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT cpf,total_vendas,vendas_finalizadas,total_gasto,ultima_venda
    FROM dbo.vw_clientes_compras
    WHERE @q IS NULL OR nome LIKE '%' + @q + '%'
      OR cpf LIKE '%' + @q + '%' OR telefone LIKE '%' + @q + '%'
    ORDER BY nome;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_cliente_resumo @cpf_cliente VARCHAR(11)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT total_vendas,vendas_finalizadas,total_gasto,ultima_venda
    FROM dbo.fn_cliente_resumo(@cpf_cliente);
END;
GO

/* Trigger 1: valida integridade comercial e impede excluir item vinculado. */
CREATE OR ALTER TRIGGER dbo.tr_produtos_validar
ON dbo.produtos
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted
               WHERE preco < 0 OR tamanho<>UPPER(LTRIM(RTRIM(tamanho)))
                  OR tamanho NOT IN ('XS','PP','P','M','G','GG','XG','XGG','XXG','XXXG',
                                     'XL','XXL','XXXL','33','34','35','36','37','38','39','40',
                                     '41','42','43','44','45','46','47','48','50','52','54','56'))
        THROW 51020, 'Preço ou tamanho de produto inválido.', 1;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.vendas v ON v.cod_venda=i.cod_venda
               WHERE v.status<>0)
        THROW 51001, 'Somente venda em aberto pode receber produtos.', 1;
    IF (UPDATE(descricao) OR UPDATE(tamanho) OR UPDATE(preco)
        OR UPDATE(cod_marca) OR UPDATE(cod_categoria))
       AND EXISTS (SELECT 1 FROM deleted WHERE cod_venda IS NOT NULL)
        THROW 51028, 'Produto vinculado a uma venda não pode ser alterado.', 1;
    IF EXISTS (SELECT 1 FROM deleted d LEFT JOIN inserted i ON i.cod_prod=d.cod_prod
               WHERE i.cod_prod IS NULL AND d.cod_venda IS NOT NULL)
        THROW 51021, 'Produto vinculado a uma venda não pode ser excluído.', 1;
END;
GO

/* Trigger 2: mantém a disponibilidade do produto a partir do vínculo da venda. */
CREATE OR ALTER TRIGGER dbo.tr_produtos_situacao
ON dbo.produtos
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(cod_venda)
        UPDATE p SET is_selled=CASE WHEN i.cod_venda IS NULL THEN 0 ELSE 1 END
        FROM dbo.produtos p JOIN inserted i ON i.cod_prod=p.cod_prod
        WHERE p.is_selled<>CASE WHEN i.cod_venda IS NULL THEN 0 ELSE 1 END;
END;
GO

/* Trigger 3: recalcula os totais afetados por inclusão, alteração ou remoção. */
CREATE OR ALTER TRIGGER dbo.tr_produtos_recalcular_vendas
ON dbo.produtos
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    ;WITH vendas_afetadas AS (
        SELECT cod_venda FROM inserted WHERE cod_venda IS NOT NULL
        UNION
        SELECT cod_venda FROM deleted WHERE cod_venda IS NOT NULL
    )
    UPDATE v SET valor_total=COALESCE(t.total,0)
    FROM dbo.vendas v
    JOIN vendas_afetadas a ON a.cod_venda=v.cod_venda
    OUTER APPLY (SELECT SUM(p.preco) AS total
                 FROM dbo.produtos p WHERE p.cod_venda=v.cod_venda) t;
END;
GO

/* Trigger 4: pagamentos só entram em venda aberta e por método ativo. */
CREATE OR ALTER TRIGGER dbo.tr_venda_pagamento_validar
ON dbo.venda_pagamento
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.vendas v ON v.cod_venda=i.cod_venda
               WHERE v.status<>0)
        THROW 51003, 'Pagamento só pode ser incluído em venda aberta.', 1;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.metodo_pagamento m ON m.cod_pagamento=i.cod_pagamento
               WHERE m.is_active=0)
        THROW 51004, 'Método de pagamento desabilitado.', 1;
    IF EXISTS (SELECT 1 FROM inserted WHERE valor_pago<=0)
        THROW 51005, 'Valor pago deve ser maior que zero.', 1;
END;
GO

/* Procedure 1: adiciona lote de produtos e deixa triggers manterem os totais. */
CREATE OR ALTER PROCEDURE dbo.sp_venda_adicionar_produtos
    @venda_id INT,
    @produtos_json NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    DECLARE @status INT;
    SELECT @status=status FROM dbo.vendas WITH (UPDLOCK,HOLDLOCK)
    WHERE cod_venda=@venda_id;
    IF @status IS NULL THROW 51010, 'Venda não encontrada.', 1;
    IF @status<>0 THROW 51011, 'Venda não está em aberto.', 1;
    IF ISJSON(@produtos_json)<>1 THROW 51022, 'Lista de produtos inválida.', 1;
    DECLARE @itens TABLE(cod_prod INT PRIMARY KEY);
    INSERT INTO @itens(cod_prod)
    SELECT DISTINCT TRY_CONVERT(INT,[value]) FROM OPENJSON(@produtos_json)
    WHERE TRY_CONVERT(INT,[value]) IS NOT NULL;
    IF (SELECT COUNT(*) FROM OPENJSON(@produtos_json))<>(SELECT COUNT(*) FROM @itens)
        THROW 51022, 'A lista contém códigos inválidos ou repetidos.', 1;
    IF NOT EXISTS (SELECT 1 FROM @itens) THROW 51023, 'Informe ao menos um produto.', 1;
    IF EXISTS (SELECT 1 FROM @itens i LEFT JOIN dbo.produtos p WITH (UPDLOCK,HOLDLOCK)
               ON p.cod_prod=i.cod_prod
               WHERE p.cod_prod IS NULL OR p.cod_venda IS NOT NULL OR p.is_selled=1)
        THROW 51024, 'Produto inexistente ou indisponível.', 1;
    UPDATE p SET cod_venda=@venda_id
    FROM dbo.produtos p JOIN @itens i ON i.cod_prod=p.cod_prod;
    COMMIT TRANSACTION;
END;
GO

/* Procedure 2: desvincula produto e recalcula total via trigger. */
CREATE OR ALTER PROCEDURE dbo.sp_venda_remover_produto
    @venda_id INT,
    @produto_id INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    DECLARE @status INT;
    SELECT @status=status FROM dbo.vendas WITH (UPDLOCK,HOLDLOCK)
    WHERE cod_venda=@venda_id;
    IF @status IS NULL THROW 51010, 'Venda não encontrada.', 1;
    IF @status<>0 THROW 51011, 'Venda não está em aberto.', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.produtos WITH (UPDLOCK,HOLDLOCK)
                   WHERE cod_prod=@produto_id AND cod_venda=@venda_id)
        THROW 51025, 'Produto não pertence a esta venda.', 1;
    UPDATE dbo.produtos SET cod_venda=NULL WHERE cod_prod=@produto_id;
    COMMIT TRANSACTION;
END;
GO

/* Procedure 3: registra pagamentos, limita excedente e finaliza atomicamente. */
CREATE OR ALTER PROCEDURE dbo.sp_venda_pagar
    @venda_id INT,
    @pagamentos_json NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    DECLARE @total DECIMAL(10,2), @status INT;
    SELECT @total=valor_total,@status=status FROM dbo.vendas WITH (UPDLOCK,HOLDLOCK)
    WHERE cod_venda=@venda_id;
    IF @status IS NULL THROW 51010, 'Venda não encontrada.', 1;
    IF @status<>0 THROW 51011, 'Venda não está em aberto.', 1;
    IF COALESCE(@total,0)<=0 THROW 51012, 'Adicione produtos antes de pagar.', 1;
    IF ISJSON(@pagamentos_json)<>1 THROW 51026, 'Lista de pagamentos inválida.', 1;

    DECLARE @entrada TABLE(seq INT IDENTITY(1,1),cod_pagamento INT,valor DECIMAL(10,2));
    INSERT INTO @entrada(cod_pagamento,valor)
    SELECT cod_pagamento,valor
    FROM OPENJSON(@pagamentos_json)
         WITH (cod_pagamento INT '$.metodoPagamentoId', valor DECIMAL(10,2) '$.valorPago');
    IF NOT EXISTS (SELECT 1 FROM @entrada) THROW 51027, 'Informe as formas de pagamento.', 1;
    IF EXISTS (SELECT 1 FROM @entrada WHERE cod_pagamento IS NULL OR valor IS NULL OR valor<=0)
        THROW 51005, 'Método ou valor de pagamento inválido.', 1;
    IF EXISTS (SELECT 1 FROM @entrada e LEFT JOIN dbo.metodo_pagamento m
               ON m.cod_pagamento=e.cod_pagamento WHERE m.cod_pagamento IS NULL OR m.is_active=0)
        THROW 51004, 'Método de pagamento inexistente ou desabilitado.', 1;

    DECLARE @alocado TABLE(cod_pagamento INT PRIMARY KEY,valor DECIMAL(10,2));
    ;WITH acumulado AS (
        SELECT seq,cod_pagamento,valor,
               SUM(valor) OVER(ORDER BY seq ROWS UNBOUNDED PRECEDING) AS total_corrente
        FROM @entrada
    ), limitado AS (
        SELECT cod_pagamento,
               CASE WHEN total_corrente<=@total THEN valor
                    WHEN total_corrente-valor<@total THEN @total-(total_corrente-valor)
                    ELSE 0 END AS valor_alocado
        FROM acumulado
    )
    INSERT INTO @alocado(cod_pagamento,valor)
    SELECT cod_pagamento,SUM(valor_alocado) FROM limitado
    WHERE valor_alocado>0 GROUP BY cod_pagamento;
    IF COALESCE((SELECT SUM(valor) FROM @alocado),0)<@total
        THROW 51013, 'Pagamento incompleto.', 1;

    DELETE FROM dbo.venda_pagamento WHERE cod_venda=@venda_id;
    INSERT INTO dbo.venda_pagamento(cod_pagamento,cod_venda,valor_pago)
    SELECT cod_pagamento,@venda_id,valor FROM @alocado;
    UPDATE dbo.vendas SET status=1 WHERE cod_venda=@venda_id;
    COMMIT TRANSACTION;
END;
GO

/* Procedure 4: cancela venda e libera itens numa única transação. */
CREATE OR ALTER PROCEDURE dbo.sp_venda_cancelar @venda_id INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    DECLARE @status INT;
    SELECT @status=status FROM dbo.vendas WITH (UPDLOCK,HOLDLOCK)
    WHERE cod_venda=@venda_id;
    IF @status IS NULL THROW 51010, 'Venda não encontrada.', 1;
    IF @status<>0 THROW 51011, 'Somente venda em aberto pode ser cancelada.', 1;
    UPDATE dbo.produtos SET cod_venda=NULL WHERE cod_venda=@venda_id;
    DELETE FROM dbo.venda_pagamento WHERE cod_venda=@venda_id;
    UPDATE dbo.vendas SET status=2 WHERE cod_venda=@venda_id;
    COMMIT TRANSACTION;
END;
GO
