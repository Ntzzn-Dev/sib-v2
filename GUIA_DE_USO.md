# Guia de uso

## Primeiro acesso

Em uma base vazia, crie o primeiro funcionário na tela de cadastro. Ele recebe automaticamente perfil **Administrador**. Os próximos funcionários são criados pelo administrador e podem receber perfil administrador ou atendente. O login é feito com e-mail e senha.

O script de seed cria contas de demonstração. Consulte os dados atuais em `src/main/resources/db/03_seed_database_sqlserver.sql`; não presuma que os usuários de exemplo correspondem aos dados de produção.

## Perfis

| Ação | Administrador | Atendente |
|---|:---:|:---:|
| Consultar dados e navegar pelas telas | Sim | Sim |
| Criar e operar vendas | Sim | Sim |
| Cadastrar clientes e endereços | Sim | Sim |
| Alterar o próprio perfil | Sim | Sim |
| Cadastrar, editar ou excluir funcionários | Sim | Não |
| Administrar produtos, marcas, categorias e formas de pagamento | Sim | Não |
| Configurar o e-mail remetente | Sim | Não |

O backend representa administrador com `perfil = 0` e atendente com `perfil = 1`. A aba de configuração é voltada ao administrador.

## Operação de vendas

1. Selecione o cliente e inicie uma venda. A venda é criada em aberto.
2. Adicione produtos disponíveis ao carrinho. O estoque e o total da venda são atualizados.
3. Registre uma ou mais formas de pagamento. A soma deve cobrir o total para concluir a venda.
4. Se necessário, remova produtos ou cancele a venda enquanto ela estiver aberta.

Uma venda paga ou cancelada não pode receber novas alterações. Os status usados no banco são `0` (aberta), `1` (paga) e `2` (cancelada).

## Dados de operação e configuração

Na operação, o atendente pode cadastrar clientes e endereços. O administrador mantém os cadastros de funcionários, produtos, marcas, categorias e métodos de pagamento. Uma venda referencia cliente e funcionário; os produtos vendidos ficam vinculados à venda.

## Perfil e senha

Em **Meu perfil**, o funcionário pode atualizar seus dados, telefone, endereço e senha. Para manter a senha atual, deixe o campo de senha vazio.

Na tela de login, **Esqueci minha senha** solicita uma nova senha temporária. Se o endereço estiver cadastrado e o e-mail remetente estiver configurado, o sistema envia a senha para o funcionário. A senha deve ser alterada depois do acesso.

## Configurar envio de e-mail

Um administrador informa o Gmail remetente e a senha de app em **Configuração → E-mail remetente**, salva e envia uma mensagem de teste. A senha de app é diferente da senha normal da conta Google. Consulte [Instalação](INSTALACAO.md) para preparar a conta.
