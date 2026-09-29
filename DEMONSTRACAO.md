# Demonstração do Sistema

Este documento apresenta um fluxo rápido para conhecer as principais funcionalidades do projeto Controle de Estoque.

## Fluxo sugerido

### 1. Login

Acesse:

```text
http://localhost:8081
```

Realize login com um usuário de demonstração.

Após a autenticação, a aplicação recebe um JWT utilizado nas requisições protegidas.

---

### 2. Dashboard

Com um usuário ADMIN, observe os indicadores:

```text
Produtos
Unidades
Estoque baixo
Valor do estoque
Categorias
Usuários
Movimentações
Entradas
Saídas
Atividades recentes
```

---

### 3. Produtos

Acesse a listagem e teste:

```text
Busca
Filtros
Ordenação
Paginação
Cadastro
Edição
Imagem
Estoque mínimo
```

---

### 4. Movimentação

Escolha um produto e realize uma:

```text
ENTRADA
```

Depois realize uma:

```text
SAIDA
```

Observe a alteração automática da quantidade disponível.

O sistema bloqueia saídas superiores ao estoque existente.

---

### 5. Usuários e permissões

Na área administrativa é possível:

```text
Consultar usuários
Filtrar usuários
Alterar perfil
Redefinir senha
```

Perfis disponíveis:

```text
ADMIN
OPERADOR
CONSULTA
```

---

### 6. Auditoria

Acesse a auditoria para visualizar ações realizadas anteriormente.

Exemplos:

```text
Produto criado
Produto alterado
Movimentação realizada
Perfil alterado
Senha redefinida
```

---

### 7. Relatórios

Gere um relatório de estoque.

Teste também:

```text
Somente estoque baixo
Relatório de movimentações
Filtro por período
Filtro por tipo
```

---

### 8. Exportações

Os relatórios podem ser exportados em:

```text
CSV
XLSX
PDF
```

---

### 9. Swagger

Durante o desenvolvimento, acesse:

```text
http://localhost:8081/swagger-ui/index.html
```

O Swagger permite visualizar e testar a API REST.

---

### 10. Testes automatizados

Execute:

```bash
mvn clean test
```

A versão atual possui:

```text
113 testes
Failures: 0
Errors: 0
```

---

# Resumo

Em poucos minutos é possível demonstrar:

```text
Autenticação
JWT
Controle de acesso
CRUD
Filtros e paginação
Movimentações
Regras de estoque
Dashboard
Usuários
Auditoria
Relatórios
Exportações
Testes
Docker
CI
```