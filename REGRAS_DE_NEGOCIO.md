# Regras de negócio

Este documento resume as regras presentes nos serviços Java e nos scripts SQL atuais. As verificações no banco complementam as validações da API.

## Funcionários e autorização

- O login é pelo e-mail único do funcionário e senha.
- O primeiro funcionário cadastrado em uma base sem funcionários é automaticamente administrador (`perfil = 0`). O padrão dos demais é atendente (`perfil = 1`).
- O sistema deve manter pelo menos um administrador.
- Administradores mantêm funcionários, produtos, marcas, categorias e formas de pagamento, além da configuração do remetente.
- Atendentes operam vendas e podem cadastrar clientes e endereços. Cada funcionário pode editar o próprio perfil.
- A senha é obrigatória ao cadastrar funcionário; na edição, campo vazio conserva a senha existente.

## Cadastros

- CPF é normalizado para os onze dígitos; o frontend pode exibi-lo pontuado.
- O e-mail de funcionário é obrigatório e único.
- Salários e preços não podem ser negativos.
- Produto deve pertencer a uma categoria. A marca pode ser omitida.
- Endereço pode ser desvinculado de uma pessoa; referências a endereços usados em vendas são protegidas por chave estrangeira.
- Formas de pagamento desativadas não podem ser usadas em novas transações.

## Vendas e estoque

- Venda inicia aberta (`status = 0`) e referencia cliente e funcionário.
- Somente produto disponível pode ser adicionado a uma venda aberta; um produto não pode estar em mais de uma venda.
- Alterar ou remover produtos recalcula o total. Ao vincular produto à venda, o estado de estoque acompanha o vínculo.
- Pagamento só é aceito em venda aberta com produtos e total positivo. Pode usar mais de uma forma de pagamento, desde que ativa e com valor positivo.
- Os pagamentos devem cobrir o valor total. Quando quitada, a venda muda para paga (`status = 1`).
- Apenas venda aberta pode ser cancelada (`status = 2`); seus produtos são liberados.
- Vendas pagas ou canceladas não aceitam novas alterações.

## Recuperação de senha

- A resposta da solicitação é genérica para não revelar se um e-mail está cadastrado.
- Um e-mail válido associado a funcionário recebe uma senha temporária aleatória, desde que o remetente SMTP esteja configurado.
- Há intervalo mínimo de dois minutos entre solicitações por e-mail na instância da API.
