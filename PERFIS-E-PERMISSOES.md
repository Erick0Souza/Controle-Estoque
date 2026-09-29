# Perfis e Permissões

Este documento descreve o sistema de autorização do projeto **Controle de Estoque**.

A aplicação utiliza **Spring Security + JWT** e possui três perfis:

```text
ADMIN
OPERADOR
CONSULTA
```

Cada perfil possui permissões diferentes de acordo com a responsabilidade do usuário dentro do sistema.

---

# 1. Visão geral

A hierarquia funcional do sistema pode ser entendida como:

```text
ADMIN
  │
  ├── Administração
  ├── Operação de estoque
  └── Consulta

OPERADOR
  │
  ├── Operação de estoque
  └── Consulta

CONSULTA
  │
  └── Consulta
```

Isso não significa que os perfis herdam tecnicamente uns dos outros.

As permissões são configuradas explicitamente no Spring Security.

---

# 2. ADMIN

O perfil:

```text
ADMIN
```

possui o maior nível de acesso da aplicação.

Pode utilizar recursos operacionais e administrativos.

Principais responsabilidades:

- gerenciamento de produtos;
- gerenciamento de categorias;
- movimentação de estoque;
- gerenciamento de usuários;
- alteração de perfis;
- redefinição de senhas;
- auditoria;
- dashboard;
- relatórios;
- exportações.

---

# 3. OPERADOR

O perfil:

```text
OPERADOR
```

é destinado aos usuários responsáveis pelas atividades operacionais do estoque.

Pode:

- visualizar produtos;
- cadastrar produtos;
- editar produtos;
- adicionar imagens;
- remover imagens;
- consultar categorias;
- registrar entradas;
- registrar saídas;
- consultar movimentações.

Não possui acesso aos recursos administrativos.

---

# 4. CONSULTA

O perfil:

```text
CONSULTA
```

é destinado a usuários que precisam visualizar informações sem alterar os dados do sistema.

Pode:

- visualizar produtos;
- visualizar categorias;
- consultar movimentações.

Não pode:

```text
Cadastrar produtos
Editar produtos
Excluir produtos
Alterar imagens
Criar categorias
Editar categorias
Excluir categorias
Realizar movimentações
Acessar /admin/**
```

---

# 5. Matriz geral de permissões

| Funcionalidade | ADMIN | OPERADOR | CONSULTA |
|---|:---:|:---:|:---:|
| Login | ✅ | ✅ | ✅ |
| Consultar produtos | ✅ | ✅ | ✅ |
| Cadastrar produto | ✅ | ✅ | ❌ |
| Editar produto | ✅ | ✅ | ❌ |
| Excluir produto | ✅ | ❌ | ❌ |
| Adicionar imagem ao produto | ✅ | ✅ | ❌ |
| Remover imagem do produto | ✅ | ✅ | ❌ |
| Consultar categorias | ✅ | ✅ | ✅ |
| Criar categoria | ✅ | ❌ | ❌ |
| Editar categoria | ✅ | ❌ | ❌ |
| Excluir categoria | ✅ | ❌ | ❌ |
| Consultar movimentações | ✅ | ✅ | ✅ |
| Registrar entrada | ✅ | ✅ | ❌ |
| Registrar saída | ✅ | ✅ | ❌ |
| Gerenciar usuários | ✅ | ❌ | ❌ |
| Alterar perfil de usuário | ✅ | ❌ | ❌ |
| Redefinir senha de usuário | ✅ | ❌ | ❌ |
| Consultar auditoria | ✅ | ❌ | ❌ |
| Consultar dashboard | ✅ | ❌ | ❌ |
| Consultar relatórios | ✅ | ❌ | ❌ |
| Exportar CSV | ✅ | ❌ | ❌ |
| Exportar XLSX | ✅ | ❌ | ❌ |
| Exportar PDF | ✅ | ❌ | ❌ |

---

# 6. Produtos

Base:

```text
/produtos
```

## Consultar produtos

```http
GET /produtos
GET /produtos/{id}
```

Permitido para:

```text
ADMIN
OPERADOR
CONSULTA
```

---

## Criar produto

```http
POST /produtos
```

Permitido para:

```text
ADMIN
OPERADOR
```

Bloqueado para:

```text
CONSULTA
```

---

## Editar produto

```http
PUT /produtos/{id}
```

Permitido para:

```text
ADMIN
OPERADOR
```

Bloqueado para:

```text
CONSULTA
```

---

## Excluir produto

```http
DELETE /produtos/{id}
```

Permitido somente para:

```text
ADMIN
```

---

# 7. Imagens de produtos

## Adicionar imagem

```http
POST /produtos/{id}/imagem
```

Permitido para:

```text
ADMIN
OPERADOR
```

---

## Excluir imagem

```http
DELETE /produtos/{id}/imagem
```

Permitido para:

```text
ADMIN
OPERADOR
```

O perfil `CONSULTA` não pode alterar imagens.

---

# 8. Categorias

Base:

```text
/categorias
```

## Consultar

```http
GET /categorias
GET /categorias/{id}
```

Permitido para:

```text
ADMIN
OPERADOR
CONSULTA
```

---

## Criar

```http
POST /categorias
```

Permitido somente para:

```text
ADMIN
```

---

## Editar

```http
PUT /categorias/{id}
```

Permitido somente para:

```text
ADMIN
```

---

## Excluir

```http
DELETE /categorias/{id}
```

Permitido somente para:

```text
ADMIN
```

---

# 9. Movimentações

Base:

```text
/movimentacoes
```

## Consultar movimentações

```http
GET /movimentacoes
GET /movimentacoes/produto/{produtoId}
```

Permitido para:

```text
ADMIN
OPERADOR
CONSULTA
```

---

## Registrar movimentação

```http
POST /movimentacoes
```

Permitido para:

```text
ADMIN
OPERADOR
```

Bloqueado para:

```text
CONSULTA
```

Isso inclui movimentações:

```text
ENTRADA
SAIDA
```

---

# 10. Área administrativa

Todas as rotas iniciadas por:

```text
/admin/**
```

são protegidas pelo Spring Security.

A regra aplicada é:

```text
ROLE_ADMIN
```

Portanto:

```text
ADMIN       ✅
OPERADOR    ❌
CONSULTA    ❌
```

---

# 11. Administração de usuários

Base:

```text
/admin/usuarios
```

Endpoints:

```http
GET /admin/usuarios

GET /admin/usuarios/{id}

PUT /admin/usuarios/{id}/perfil

PUT /admin/usuarios/{id}/senha
```

Todos exigem:

```text
ADMIN
```

---

## Alteração de perfil

O administrador pode alterar o perfil de outro usuário.

Perfis possíveis:

```text
ADMIN
OPERADOR
CONSULTA
```

Existem proteções adicionais.

O administrador não pode remover o próprio perfil ADMIN.

Exemplo de operação bloqueada:

```text
Usuário autenticado:
ADMIN

Tentativa:
alterar o próprio perfil para OPERADOR

Resultado:
operação rejeitada
```

---

## Último administrador

O sistema também impede que o último administrador seja rebaixado.

Exemplo:

```text
Quantidade de ADMINs: 1

ADMIN → OPERADOR
```

Resultado:

```text
operação rejeitada
```

Dessa forma, o sistema deve continuar possuindo pelo menos um administrador.

---

# 12. Redefinição de senha

Endpoint:

```http
PUT /admin/usuarios/{id}/senha
```

Somente:

```text
ADMIN
```

pode utilizar essa funcionalidade.

A nova senha:

- deve ser confirmada;
- precisa respeitar o limite configurado;
- é protegida novamente com BCrypt;
- não é armazenada em texto puro.

A ação também é registrada na auditoria.

---

# 13. Auditoria

Base:

```text
/admin/auditorias
```

Endpoints:

```http
GET /admin/auditorias

GET /admin/auditorias/{id}
```

Permitido somente para:

```text
ADMIN
```

Isso evita que usuários operacionais ou de consulta tenham acesso aos registros administrativos do sistema.

---

# 14. Dashboard

Endpoint:

```http
GET /admin/dashboard
```

Permitido somente para:

```text
ADMIN
```

O dashboard contém informações consolidadas do sistema, incluindo:

```text
Produtos
Categorias
Usuários
Movimentações
Estoque
Valor financeiro
Auditoria recente
```

---

# 15. Relatórios

Base:

```text
/admin/relatorios
```

Endpoints:

```http
GET /admin/relatorios/estoque

GET /admin/relatorios/movimentacoes
```

Permitido somente para:

```text
ADMIN
```

---

# 16. Exportações

Base:

```text
/admin/exportacoes
```

## CSV

```http
GET /admin/exportacoes/estoque/csv

GET /admin/exportacoes/movimentacoes/csv
```

## XLSX

```http
GET /admin/exportacoes/estoque/xlsx

GET /admin/exportacoes/movimentacoes/xlsx
```

## PDF

```http
GET /admin/exportacoes/estoque/pdf

GET /admin/exportacoes/movimentacoes/pdf
```

Todas exigem:

```text
ADMIN
```

---

# 17. Cadastro e login

As rotas:

```text
/auth/**
```

são liberadas pelo Spring Security para usuários não autenticados.

Isso é necessário para permitir login e, quando habilitado, cadastro.

---

## Login

```http
POST /auth/login
```

Não exige JWT.

---

## Cadastro

```http
POST /auth/register
```

Não exige JWT no filtro de segurança.

Entretanto, a aplicação também possui uma configuração:

```text
app.security.public-registration
```

No ambiente:

```text
dev
```

o cadastro público está habilitado.

No ambiente:

```text
prod
```

o cadastro público está desabilitado.

Nesse caso, mesmo a rota estando acessível pelo Spring Security, o controller rejeita a criação da conta.

---

# 18. Recursos públicos

Alguns recursos não exigem autenticação.

Entre eles:

```text
/
index.html
app.js
style.css
/css/**
/js/**
/uploads/**
/error
```

Os arquivos presentes em:

```text
/uploads/**
```

podem ser acessados sem autenticação.

Isso permite que as imagens dos produtos sejam exibidas pela interface web.

---

# 19. Swagger

As rotas:

```text
/swagger-ui/**
/swagger-ui.html
/v3/api-docs/**
```

são liberadas pela configuração do Spring Security.

Entretanto, no perfil:

```text
prod
```

o SpringDoc é desabilitado.

Portanto, em produção, Swagger e OpenAPI não ficam disponíveis mesmo que essas rotas estejam liberadas pela configuração de autorização.

---

# 20. Respostas de segurança

## Usuário não autenticado

Quando uma rota protegida é acessada sem autenticação válida:

```http
401 Unauthorized
```

Exemplo:

```json
{
  "status": 401,
  "mensagem": "Autenticação necessária"
}
```

---

## Usuário sem permissão

Quando o usuário está autenticado, mas seu perfil não permite aquela operação:

```http
403 Forbidden
```

Exemplo:

```json
{
  "status": 403,
  "mensagem": "Você não possui permissão para realizar esta ação"
}
```

---

# 21. JWT e sessões

A aplicação utiliza:

```text
SessionCreationPolicy.STATELESS
```

Isso significa que o servidor não mantém uma sessão HTTP tradicional para autenticação.

Cada requisição protegida deve possuir um JWT válido.

Fluxo:

```text
Usuário
   │
   │ email + senha
   ▼
POST /auth/login
   │
   ▼
JWT
   │
   │ Authorization: Bearer TOKEN
   ▼
API protegida
   │
   ▼
JwtFilter
   │
   ▼
Usuário autenticado
   │
   ▼
Verificação de perfil
   │
   ▼
Endpoint
```

---

# 22. Resumo rápido

```text
CONSULTA
└── Visualiza dados

OPERADOR
├── Visualiza dados
├── Cria e edita produtos
├── Gerencia imagens
└── Realiza movimentações

ADMIN
├── Tudo que OPERADOR pode fazer
├── Exclui produtos
├── Gerencia categorias
├── Gerencia usuários
├── Auditoria
├── Dashboard
├── Relatórios
└── Exportações
```

---

# 23. Princípio utilizado

A organização das permissões segue a ideia de:

```text
menor privilégio necessário
```

Um usuário deve possuir somente as permissões necessárias para executar sua função.

Isso reduz o risco de alterações acidentais ou acesso indevido às funcionalidades administrativas.

---

# Repositório

```text
https://github.com/Erick0Souza/Controle-Estoque
```