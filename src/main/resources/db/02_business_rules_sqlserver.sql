-- SQL Server procedures, view and triggers. Each @@ separator is a JDBC batch boundary.
CREATE OR ALTER FUNCTION dbo.venda_status_text(@status INT)
    RETURNS VARCHAR(20)
    AS
BEGIN
RETURN CASE @status WHEN 0 THEN 'EM ABERTO' WHEN 1 THEN 'PAGA'
                    WHEN 2 THEN 'CANCELADA' ELSE 'ERRO' END;
END;
go


CREATE OR ALTER VIEW dbo.vw_vendas_resumo
AS
SELECT v.codigo_venda, v.data_venda, v.cliente_id, c.nome_pessoa AS cliente_nome,
       v.funcionario_id, f.nome_pessoa AS funcionario_nome,
       v.valor_total, v.status, dbo.venda_status_text(v.status) AS status_texto,
       COALESCE(pg.total_pago, 0) AS total_pago, COALESCE(pr.quantidade_produtos, 0) AS quantidade_produtos
FROM dbo.venda v
         JOIN dbo.cliente c ON c.cpf_pessoa = v.cliente_id
         JOIN dbo.funcionario f ON f.cpf_pessoa = v.funcionario_id
    OUTER APPLY (SELECT SUM(valor_pago) AS total_pago FROM dbo.venda_pagamento WHERE venda_id=v.codigo_venda) pg
OUTER APPLY (SELECT COUNT(*) AS quantidade_produtos FROM dbo.venda_produtos WHERE venda_codigo_venda=v.codigo_venda) pr;
go

CREATE OR ALTER TRIGGER dbo.tr_venda_produtos_validar ON dbo.venda_produtos
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.venda v ON v.codigo_venda=i.venda_codigo_venda WHERE v.status<>0)
        THROW 51001, 'Somente venda em aberto pode receber produtos.', 1;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.produto p ON p.codigo_produto=i.produtos_codigo_produto WHERE p.is_selled=1)
        THROW 51002, 'Produto já foi vendido.', 1;
UPDATE p SET is_selled=1 FROM dbo.produto p JOIN inserted i ON i.produtos_codigo_produto=p.codigo_produto;
END;
go


CREATE OR ALTER TRIGGER dbo.tr_venda_produtos_recalcular ON dbo.venda_produtos
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;
UPDATE p SET is_selled=0 FROM dbo.produto p JOIN deleted d ON d.produtos_codigo_produto=p.codigo_produto;
END;
go


CREATE OR ALTER TRIGGER dbo.tr_venda_pagamento_validar ON dbo.venda_pagamento
AFTER INSERT, UPDATE
                                  AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.venda v ON v.codigo_venda=i.venda_id WHERE v.status<>0)
        THROW 51003, 'Pagamento só pode ser incluído em venda aberta.', 1;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.metodo_pagamento m ON m.codigo_metodo_pagamento=i.metodo_pagamento_id WHERE m.is_active_metodo_pagamento=0)
        THROW 51004, 'Método de pagamento desabilitado.', 1;
    IF EXISTS (SELECT 1 FROM inserted WHERE valor_pago<=0)
        THROW 51005, 'Valor pago deve ser maior que zero.', 1;
END;
go

CREATE OR ALTER PROCEDURE dbo.finalizar_venda @venda_id INT
    AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
BEGIN TRANSACTION;
    DECLARE @total FLOAT, @status INT, @pago FLOAT;
SELECT @total=valor_total, @status=status FROM dbo.venda WITH (UPDLOCK, ROWLOCK)
WHERE codigo_venda=@venda_id;
IF @status IS NULL THROW 51010, 'Venda não encontrada.', 1;
    IF @status<>0 THROW 51011, 'Venda não está em aberto.', 1;
    IF @total<=0 THROW 51012, 'Adicione produtos antes de pagar.', 1;
    IF EXISTS (SELECT 1 FROM dbo.venda_pagamento vp
               JOIN dbo.metodo_pagamento mp ON mp.codigo_metodo_pagamento=vp.metodo_pagamento_id
               WHERE vp.venda_id=@venda_id AND mp.is_active_metodo_pagamento=0)
        THROW 51014, 'Método de pagamento desabilitado.', 1;
SELECT @pago=COALESCE(SUM(valor_pago),0) FROM dbo.venda_pagamento WHERE venda_id=@venda_id;
IF @pago<@total THROW 51013, 'Pagamento incompleto.', 1;
UPDATE dbo.venda SET status=1 WHERE codigo_venda=@venda_id;
COMMIT TRANSACTION;
END;
go
