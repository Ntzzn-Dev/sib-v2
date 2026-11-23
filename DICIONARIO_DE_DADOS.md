# Dicionário de dados

Resumo das colunas definidas em `src/main/resources/db/01_schema_sqlserver.sql`. Cada linha da tabela abaixo representa uma tabela do banco; os campos são separados por quebras de linha para facilitar a leitura.

| Tabela | Colunas |
|---|---|
| `pessoas` | `cpf VARCHAR(11)` — PK, obrigatório;<br>`nome VARCHAR(50)` — obrigatório;<br>`telefone VARCHAR(11)` — opcional. |
| `clientes` | `pessoa_cpf VARCHAR(11)` — PK e FK para `pessoas.cpf`, obrigatório. |
| `funcionarios` | `pessoa_cpf VARCHAR(11)` — PK e FK para `pessoas.cpf`, obrigatório;<br>`salario DECIMAL(8,2)` — obrigatório, padrão `0`, não negativo;<br>`perfil INT` — obrigatório, padrão `1` (atendente); `0` = administrador e `1` = atendente;<br>`email VARCHAR(254)` — único; pode ser nulo no schema, mas é obrigatório no cadastro pela API;<br>`senha VARCHAR(255)` — obrigatória e não pode ser vazia. |
| `endereco` | `cod_end INT IDENTITY(1,1)` — PK;<br>`cpf_pessoa VARCHAR(11)` — FK opcional para `pessoas.cpf`;<br>`logradouro VARCHAR(50)` — opcional;<br>`complemento VARCHAR(50)` — opcional;<br>`numero VARCHAR(50)` — opcional;<br>`bairro VARCHAR(50)` — opcional;<br>`cidade VARCHAR(50)` — opcional;<br>`cep VARCHAR(9)` — opcional;<br>`uf VARCHAR(2)` — opcional. |
| `marcas` | `cod_marca INT IDENTITY(1,1)` — PK;<br>`nome VARCHAR(50)` — obrigatório e único. |
| `categorias` | `cod_categorias INT IDENTITY(1,1)` — PK;<br>`nome VARCHAR(50)` — obrigatório e único. |
| `metodo_pagamento` | `cod_pagamento INT IDENTITY(1,1)` — PK;<br>`desc_pagamento VARCHAR(50)` — obrigatório e único;<br>`is_active BIT` — obrigatório, padrão `1` (ativo). |
| `vendas` | `cod_venda INT IDENTITY(1,1)` — PK;<br>`data_venda DATETIME` — obrigatório, padrão data e hora atuais;<br>`cpf_cliente VARCHAR(11)` — FK obrigatória para `clientes.pessoa_cpf`;<br>`cpf_funcionario VARCHAR(11)` — FK obrigatória para `funcionarios.pessoa_cpf`;<br>`endereco_entrega INT` — FK opcional para `endereco.cod_end`;<br>`valor_total DECIMAL(10,2)` — pode ser nulo, padrão `0`, não negativo quando informado;<br>`status INT` — obrigatório, padrão `0` (aberta); `1` = paga e `2` = cancelada. |
| `produtos` | `cod_prod INT IDENTITY(1,1)` — PK;<br>`descricao VARCHAR(50)` — obrigatório;<br>`tamanho VARCHAR(3)` — obrigatório;<br>`cod_marca INT` — FK opcional para `marcas.cod_marca`;<br>`cod_categoria INT` — FK obrigatória para `categorias.cod_categorias`;<br>`cod_venda INT` — FK opcional para `vendas.cod_venda`;<br>`preco DECIMAL(10,2)` — obrigatório, padrão `0`, não negativo;<br>`is_selled BIT` — obrigatório, padrão `0` (não vendido). |
| `venda_pagamento` | `cod_pagamento INT` — parte da PK composta e FK para `metodo_pagamento.cod_pagamento`;<br>`cod_venda INT` — parte da PK composta e FK para `vendas.cod_venda`;<br>`valor_pago DECIMAL(10,2)` — obrigatório, padrão `0`, não negativo. |
| `configuracao_email` | `cod_configuracao INT` — PK; a aplicação utiliza o código `1`;<br>`email_remetente VARCHAR(254)` — obrigatório;<br>`senha_app VARCHAR(100)` — obrigatório. |

`IDENTITY(1,1)` significa que o SQL Server gera o identificador automaticamente. `PK` indica chave primária e `FK` indica chave estrangeira. As colunas não marcadas como opcionais são `NOT NULL` no schema.

## Regras de integridade

- As chaves estrangeiras impedem referências a pessoas, cadastros ou vendas inexistentes.
- Salário, preço e valor de pagamento não podem ser negativos; a senha de funcionário não pode ser vazia.
- Perfil aceita somente `0` ou `1`; status da venda aceita somente `0`, `1` ou `2`.
- Nome de marca, nome de categoria, descrição do método de pagamento e e-mail de funcionário têm constraints de unicidade.
- A senha do funcionário e a senha de app do remetente não são retornadas pela API. O schema atual não define criptografia em repouso para esses campos.

Para o desenho lógico, consulte [BANCO_DE_DADOS.md](BANCO_DE_DADOS.md).
