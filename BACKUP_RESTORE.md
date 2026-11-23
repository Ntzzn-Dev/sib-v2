# Backup e restauração

O SIB oferece em **Configuração → Backup do banco** backup manual e importação/restauração sob demanda. O arquivo gerado pelo botão de backup é escrito pelo SQL Server na pasta padrão da instância; a tela informa o caminho. O nome contém data e hora até o minuto; se outro backup for gerado no mesmo minuto, o arquivo anterior com esse nome será substituído. Para restaurar, selecione um arquivo `.bak` e digite `RESTAURAR` para confirmar. A API valida que o arquivo contém um backup completo do banco `brecho`, verifica a integridade e substitui os dados atuais. A sessão é encerrada ao concluir. Também é possível fazer o backup diretamente no SQL Server, por exemplo com SSMS ou comandos T-SQL.

Para importar um arquivo, a aplicação backend e o serviço SQL Server precisam conseguir acessar a pasta padrão da instância. A conta da API precisa poder gravar o arquivo temporário e executar a operação; a conta do serviço SQL Server precisa ler esse arquivo. No uso local esperado, ambos rodam no mesmo computador. A restauração remove as conexões ativas e substitui o conteúdo atual do banco; faça um backup recente antes de restaurar.

## Backup completo em T-SQL

Execute em uma instância SQL Server com permissão de backup e ajuste o caminho para uma pasta existente no servidor:

```sql
BACKUP DATABASE brecho
TO DISK = 'C:\SQLBackups\brecho_full.bak'
WITH INIT, CHECKSUM, STATS = 10;
GO
```

O caminho é interpretado pela máquina que executa o serviço SQL Server, não necessariamente pelo computador onde o SSMS está aberto.

## Verificar arquivo

```sql
RESTORE VERIFYONLY
FROM DISK = 'C:\SQLBackups\brecho_full.bak'
WITH CHECKSUM;
GO
```

## Restaurar

Restaure em um banco de destino apropriado. O exemplo abaixo substitui o banco `brecho`; confirme conexões e destino antes de executar:

```sql
USE master;
GO
ALTER DATABASE brecho SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
RESTORE DATABASE brecho
FROM DISK = 'C:\SQLBackups\brecho_full.bak'
WITH REPLACE, CHECKSUM, STATS = 10;
GO
ALTER DATABASE brecho SET MULTI_USER;
GO
```

Após restaurar, verifique se a API conecta e se os dados e objetos SQL estão presentes. Restauração substitui os dados atuais pelo conteúdo do backup.
