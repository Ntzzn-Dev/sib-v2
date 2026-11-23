# API REST

Base local: `http://localhost:8080/api`. A API recebe e responde JSON, exceto rotas sem corpo que retornam `204 No Content`. A sessão é mantida por cookie HTTP; clientes web devem enviar credenciais (`credentials: include`).

## Autenticação e perfil

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/auth/login` | Público | Inicia sessão com `{ "email": "...", "senha": "..." }` |
| `POST` | `/auth/logout` | Público | Invalida a sessão atual |
| `POST` | `/auth/esqueci-senha` | Público | Solicita senha temporária com `{ "email": "..." }` |
| `GET` | `/auth/me` | Autenticado | Consulta perfil do usuário logado |
| `PUT` | `/auth/me` | Autenticado | Atualiza o próprio perfil |
| `GET` | `/auth/email-config` | Administrador | Consulta remetente e se a senha está configurada |
| `PUT` | `/auth/email-config` | Administrador | Salva `{ "emailRemetente": "...", "senhaApp": "..." }` |
| `POST` | `/auth/email-config/teste` | Administrador | Envia teste; sem corpo |

## Cadastros

As rotas de lista aceitam `?q=termo` quando indicado. Funcionários são identificados por CPF; os demais IDs numéricos aparecem nos próprios recursos.

| Recurso | Rotas |
|---|---|
| Funcionários | `GET/POST /funcionarios`, `GET/PUT/DELETE /funcionarios/{cpf}` |
| Clientes | `GET/POST /clientes`, `GET/PUT/DELETE /clientes/{cpf}` |
| Endereços | `GET/POST /enderecos`, `GET/PUT/DELETE /enderecos/{id}`, `GET /enderecos/ufs`, `GET /enderecos/cep/{cep}` |
| Produtos | `GET/POST /produtos`, `GET/PUT/DELETE /produtos/{id}` |
| Marcas | `GET/POST /marcas`, `GET/PUT/DELETE /marcas/{id}` |
| Categorias | `GET/POST /categorias`, `GET/PUT/DELETE /categorias/{id}` |
| Métodos de pagamento | `GET/POST /metodos-pagamento`, `GET/PUT/DELETE /metodos-pagamento/{id}` |

`GET /produtos` aceita também `?vendido=true|false`. Os identificadores numéricos de produto, marca, categoria, endereço e forma de pagamento são inteiros.

## Vendas

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/vendas?status=0&q=termo` | Lista vendas com filtros opcionais |
| `GET` | `/vendas/{id}` | Consulta uma venda |
| `POST` | `/vendas` | Cria venda em aberto |
| `POST` | `/vendas/{id}/produtos` | Adiciona produtos à venda |
| `DELETE` | `/vendas/{id}/produtos/{produtoId}` | Remove produto da venda aberta |
| `POST` | `/vendas/{id}/pagamentos` | Registra pagamento(s) e conclui venda quando quitada |
| `POST` | `/vendas/{id}/cancelamento` | Cancela uma venda aberta |

Os corpos completos variam por DTO. Use os nomes `clienteCpf`, `funcionarioCpf`, `codigos`, `metodoPagamentoId` e `valorPago`; os exemplos de campos e vínculos podem ser consultados em `src/main/java/com/jhonatan/sibv2/dto/requests`.

## Sessão e autorização

- Consultas `GET` e rotas públicas de autenticação podem ser chamadas sem login.
- Sem sessão, operações protegidas respondem `401`.
- O atendente pode criar clientes e endereços e operar vendas. Alterações administrativas exigem administrador e respondem `403` para o atendente.
- Enquanto não houver funcionário cadastrado, o interceptor libera o primeiro cadastro de funcionário e endereço para bootstrap.

## Respostas de erro

Erros de validação e autorização incluem um campo `detail` com a explicação.  
Códigos comuns: 
- `400` para dados inválidos ou regra de negócio.
- `401` para sessão ausente ou login inválido.
- `403` para ação administrativa sem privilégio.
- `404` para registro não encontrado.
- `409` para conflito de unicidade.
- `500` para falha não tratada.
