# Segurança

## Sessão e acesso

- A autenticação usa sessão HTTP no servidor, mantida pelo cookie de sessão.
- O frontend deve enviar cookies nas chamadas (`credentials: include`). CORS permite a origem de desenvolvimento `http://localhost:5173` com credenciais.
- O interceptor valida o funcionário da sessão no banco e aplica as permissões por perfil.
- Leituras `GET` são públicas no backend atual. Proteja o serviço e a rede de acordo com o ambiente em que for publicado.
- Respostas de funcionários não incluem senha.

## E-mail e recuperação

- O Gmail remetente autentica no SMTP com uma senha de app Google e STARTTLS.
- A senha de app é armazenada no campo `configuracao_email.senha_app` no banco. No código atual, esse campo não é cifrado; limite o acesso ao SQL Server e use apenas credenciais de demonstração fora de um ambiente real.
- A API não devolve a senha de app nas consultas de configuração. Use o botão de teste para verificar SMTP.
- O fluxo de recuperação substitui a senha da conta por uma senha temporária e envia a nova senha ao e-mail cadastrado. O usuário deve alterá-la em seguida.
- A senha SMTP não deve ser incluída em documentação pública, imagens, commits ou dados de seed compartilhados. Se uma senha de app já foi exposta, revogue-a na conta Google e gere outra.