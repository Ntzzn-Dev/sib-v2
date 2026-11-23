# SIB v2 — Backend

API REST do **Sistema Integrado de Brechó**, construída com Java 21, Spring Boot e SQL Server. Ela atende ao frontend React e gerencia funcionários, clientes, endereços, estoque, vendas, pagamentos e recuperação de senha por e-mail.

## Começar

1. Configure o SQL Server e execute os scripts na ordem descrita em [INSTALACAO.md](INSTALACAO.md).
2. Ajuste a conexão do banco em `src/main/resources/application.yaml` para o seu ambiente.
3. Inicie a API:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Por padrão a API usa [http://localhost:8080](http://localhost:8080) e o banco `brecho`. O frontend usa [http://localhost:5173](http://localhost:5173/) como URL padrão.

## O que o sistema oferece

- Login com e-mail e senha, sessão HTTP e perfis de administrador e atendente.
- Cadastro e manutenção de clientes, endereços e vendas na operação.
- Administração de funcionários, produtos, marcas, categorias, métodos de pagamento e remetente de e-mail.
- Venda em aberto, seleção de produtos, pagamentos, cancelamento.
- Consulta de endereço por CEP e recuperação de senha enviada por e-mail.
- Relatórios apoiados por views, funções, procedures e triggers do SQL Server.

O primeiro funcionário cadastrado recebe perfil de administrador. O atendente pode operar vendas e cadastrar clientes e endereços; operações administrativas ficam restritas ao administrador. As leituras da API são públicas no estado atual do interceptor.

## Documentação

| Documento | Conteúdo |
|---|---|
| [Instalação](INSTALACAO.md) | Pré-requisitos, banco e inicialização local |
| [Guia de uso](GUIA_DE_USO.md) | Primeiro acesso, perfis e fluxos da aplicação |
| [API](API.md) | Rotas, parâmetros, sessão e exemplos |
| [Banco de dados](BANCO_DE_DADOS.md) | Tabelas e relacionamentos |
| [Dicionário de dados](DICIONARIO_DE_DADOS.md) | Colunas e domínios principais |
| [Recursos SQL](RECURSOS_SQL.md) | Views, funções, procedures e triggers |
| [Regras de negócio](REGRAS_DE_NEGOCIO.md) | Restrições dos cadastros e das vendas |
| [Segurança](SEGURANCA.md) | Autenticação, autorização e credenciais |

O escopo e os scripts SQL estão em `src/main/resources/db`.
