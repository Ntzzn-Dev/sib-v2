# SIB v2

API REST em Spring Boot que traz os cadastros e os fluxos de venda do projeto CLI `brechoSIB`. A API usa Microsoft SQL Server no servidor `DESKTOP-KLGKUVQ` por padrão. O banco padrão é `sibdb`.

## Executar

```powershell
.\mvnw.cmd spring-boot:run
```

O SQL Server deve estar com TCP/IP habilitado e o banco `sibdb` criado. A aplicação usa a autenticação integrada do Windows. O padrão é o host `DESKTOP-KLGKUVQ` na porta 1433; altere `DB_HOST`, `DB_PORT` e `DB_NAME` se necessário. O driver JDBC exige a biblioteca nativa de autenticação Windows, copiada pelo Maven para `target/native`. A inicialização executa `src/main/resources/db/01_schema_sqlserver.sql` e `02_business_rules_sqlserver.sql`; o Hibernate valida o mapeamento existente sem recriar ou apagar dados. O perfil de testes usa H2 isolado, para o build não depender do SQL Server.

A API fica disponível em `http://localhost:8080/api`. Requisições e respostas usam JSON. Códigos inexistentes retornam 404, dados inválidos retornam 400 e credenciais inválidas retornam 401. CORS está habilitado para permitir integração com o frontend durante o desenvolvimento.

## Recursos

Todos os cadastros abaixo aceitam `GET` para listar, `GET /{id}` para consultar, `POST` para criar, `PUT /{id}` para editar e `DELETE /{id}` para remover. As listas aceitam busca textual opcional com `?q=termo`.

| Recurso | Rota | Identificador |
|---|---|---|
| Endereços | `/api/enderecos` | `codEnd` |
| Funcionários | `/api/funcionarios` | CPF |
| Clientes | `/api/clientes` | CPF |
| Métodos de pagamento | `/api/metodos-pagamento` | `codMetodoPag` |
| Marcas | `/api/marcas` | `codMarca` |
| Categorias | `/api/categorias` | `codCat` |
| Produtos | `/api/produtos` | `codProd` |

CPF pode ser enviado com ou sem pontuação e é armazenado no formato `000.000.000-00`. CEP aceita oito dígitos com ou sem hífen. O tamanho do produto segue as opções aceitas no CLI. Em payloads de produto, `marcaProd` e `catProd` recebem objetos com seus identificadores; em cliente/funcionário, `endPessoa` recebe `{ "codEnd": 1 }` (ou `null`). A senha de funcionário é aceita na escrita e nunca aparece nas respostas.

Exemplos de payloads:

```json
POST /api/funcionarios
{
  "cpfPessoa": "12345678901",
  "nomePessoa": "João Silva",
  "telPessoa": "11999999999",
  "endPessoa": { "codEnd": 1 },
  "salarioFunc": 2500.0,
  "senha": "senha"
}
```

```json
POST /api/produtos
{
  "descProd": "Camiseta básica branca",
  "tamanhoProd": "M",
  "marcaProd": { "codMarca": 1 },
  "catProd": { "codCat": 1 },
  "precoProd": 100.0
}
```

## Autenticação e vendas

`POST /api/auth/login` recebe `{ "cpf": "12345678901", "senha": "senha" }` e inicia uma sessão HTTP. `POST /api/auth/logout` encerra a sessão. Para criar uma conta, a tela usa `POST /api/funcionarios`; a resposta inicia uma sessão, sem uma segunda chamada de login. O cadastro de funcionários é público; as demais operações que alteram dados exigem sessão autenticada. Consultas permanecem públicas. O frontend envia o CPF do funcionário ao criar a venda.

| Operação | Rota | Corpo |
|---|---|---|
| Criar venda em aberto | `POST /api/vendas` | `{ "clienteCpf": "...", "funcionarioCpf": "..." }` |
| Adicionar produtos | `POST /api/vendas/{id}/produtos` | `{ "codigos": [1, 2] }` |
| Pagar venda | `POST /api/vendas/{id}/pagamentos` | `[{ "metodoPagamentoId": 1, "valorPago": 50.0 }]` |
| Cancelar venda aberta | `POST /api/vendas/{id}/cancelamento` | sem corpo |
| Listar/filtrar vendas | `GET /api/vendas?status=0&q=termo` | — |
| Consultar venda e detalhes | `GET /api/vendas/{id}` | — |

Status da venda: `0` em aberto, `1` paga, `2` cancelada. O pagamento aceita parcelas em métodos ativos, limita automaticamente uma parcela ao saldo restante e só conclui quando o total foi coberto. Produtos adicionados ficam marcados como vendidos, como no CLI. A venda paga ou cancelada não pode ser alterada.

