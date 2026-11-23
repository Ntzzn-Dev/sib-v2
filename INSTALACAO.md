# Instalação e execução

Este guia prepara a API do SIB v2 em ambiente local Windows.

## Pré-requisitos

- Java 21.
- SQL Server com suporte a autenticação integrada do Windows, TCP/IP habilitado e porta acessível.
- SQL Server Management Studio (SSMS) ou outra ferramenta que execute scripts T-SQL.
- Node.js e npm para iniciar também o frontend.

## Preparar o banco

1. No SQL Server, crie o banco `brecho`.
2. Abra e execute, nesta ordem, os arquivos em `src/main/resources/db`:
   1. `01_schema_sqlserver.sql` — tabelas e constraints;
   2. `02_business_rules_sqlserver.sql` — views, funções, procedures e triggers;
   3. `03_seed_database_sqlserver.sql` — dados estáticos de demonstração.
3. O script de seed é para uma base recém-criada e deve ser executado uma única vez. Ele contém IDs fixos e não é um mecanismo de atualização incremental.

Os scripts não são executados automaticamente pela aplicação. O Hibernate está configurado com `ddl-auto: validate`: ele valida o mapeamento e espera que as tabelas já existam.

## Configurar a conexão

Edite `src/main/resources/application.yaml`. O padrão atual aponta para `DESKTOP-KLGKUVQ:1433`, banco `brecho`, com autenticação integrada do Windows. Altere o host, a porta, o banco ou as opções JDBC para corresponder ao SQL Server local. A conta Windows que inicia a API precisa ter acesso ao banco.

O Maven baixa o driver JDBC e a biblioteca nativa de autenticação integrada. A configuração do projeto copia a DLL para `target/native` ao executar a aplicação com o plugin Spring Boot.

## Iniciar a API

Na pasta `sib-v2`:

```powershell
.\mvnw.cmd spring-boot:run
```

A API atende em `http://localhost:8080`, com rotas sob `/api`. 

## Iniciar o frontend

Em outro terminal, na pasta `sib-v2-frontend`:

```powershell
npm ci
npm run dev
```

O Vite disponibiliza a interface em `http://localhost:5173`. Por padrão, ela chama `http://localhost:8080/api`.

## E-mail e recuperação de senha

O envio usa SMTP do Gmail com porta 587 e STARTTLS. Depois de configurar a verificação em duas etapas na conta remetente, gere uma senha de app do Google. Entre no sistema com um administrador, abra **Configuração → E-mail remetente**, informe o Gmail e a senha de app e use o botão de teste. Essa senha é guardada na tabela `configuracao_email`; a tela não a devolve nas consultas.

Sem remetente configurado, a recuperação de senha não consegue enviar a mensagem. A resposta pública da recuperação é intencionalmente genérica, mesmo quando o e-mail não existe.

## Primeiro acesso

Com uma base vazia, crie o primeiro funcionário pelo fluxo de cadastro. Esse cadastro se torna administrador. Em uma base populada pelo seed, use um dos funcionários de demonstração definidos no próprio `03_seed_database_sqlserver.sql` e altere os dados conforme necessário.

## Backup e restauração

O backend e o serviço SQL Server precisam acessar a mesma pasta padrão de backup da instância para importar e restaurar arquivos. A conta que executa o backend precisa conseguir gravar nela, e a conta de serviço do SQL Server precisa conseguir ler os arquivos. A tela aceita arquivos de até 2 GB. Consulte [Backup e restauração](BACKUP_RESTORE.md) antes de restaurar, pois essa operação substitui o conteúdo atual de `brecho`.
