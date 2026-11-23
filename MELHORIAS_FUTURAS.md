# Melhorias futuras

Este documento reúne ideias para evoluções do SIB. Os itens abaixo são propostas para versões futuras; não fazem parte das funcionalidades implementadas atualmente.

## Validações de dados

- **Validar CPF:** além de exigir 11 dígitos, calcular e conferir os dígitos verificadores. Para não atrapalhar testes e demonstrações, manter CPFs fictícios documentados para o ambiente de desenvolvimento ou habilitar a validação estrita apenas fora dele.
- **Validar telefone:** conferir DDD e quantidade de dígitos, aceitar formatos com ou sem pontuação e apresentar uma mensagem clara quando o número estiver incompleto.
- **Validar campos numéricos:** definir limites e formatos para salário, preço, valores de pagamento, quantidade de produtos e demais campos numéricos, com validação tanto na interface quanto na API.
- **Unificar regras de CEP e endereço:** validar CEP e consistência dos campos do endereço, mantendo o preenchimento por consulta de CEP como auxílio e não como substituto da conferência do usuário.

## Permissões de funcionários

- **Permissões granulares por funcionário:** substituir os dois perfis fixos por permissões configuráveis por funcionário e por ação. Por exemplo, permitir que um atendente específico cadastre métodos de pagamento sem conceder acesso à edição de funcionários ou às demais configurações.
- **Aplicar as permissões em todas as camadas:** refletir as permissões na navegação do frontend e, obrigatoriamente, validá-las também no backend para impedir chamadas diretas à API sem autorização.
- **Registrar alterações de permissão:** manter histórico do administrador que concedeu ou removeu cada permissão e quando a mudança ocorreu.

## Painel e privacidade

- **Métricas individualizadas para funcionários:** oferecer ao funcionário métricas filtradas pelas vendas em que ele atuou, sem expor os resultados consolidados da empresa. O administrador pode manter acesso às métricas gerais.
- **Definir o escopo das métricas:** documentar quais informações cada perfil pode consultar e aplicar o filtro no backend e nas consultas SQL, não somente na interface.

## Segurança e rastreabilidade

- **Proteger senhas de funcionários:** substituir o armazenamento em texto puro por hash apropriado para senhas, com salt e verificação segura no login e na recuperação de acesso.
- **Proteger a credencial SMTP:** cifrar a senha de app do remetente no banco e definir um procedimento seguro para troca e recuperação dessa configuração.
- **Auditar operações importantes:** registrar alterações em funcionários, permissões, produtos, pagamentos e vendas, além das ações de backup e restauração.

## Backup, restauração e qualidade

- **Validar compatibilidade antes da restauração:** conferir se o backup contém as tabelas e objetos SQL esperados pelo SIB antes de substituir o banco atual, e criar uma cópia de segurança do estado atual antes da operação.
- **Facilitar o acesso aos arquivos de backup:** permitir baixar o backup gerado e escolher um local de destino acessível, respeitando as permissões do SQL Server.
- **Gerenciar retenção de backups:** oferecer política de remoção de cópias antigas e exibir data, tamanho e resultado das operações realizadas.
- **Ampliar os testes automatizados:** cobrir autenticação, autorização por perfil, CRUD, regras de venda, validações e os fluxos de backup e restauração.
