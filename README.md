# Controle de Estoque

Sistema de controle de estoque desenvolvido com Java e Spring Boot.

O projeto permite autenticação com JWT, gerenciamento de produtos e categorias,
registro de entradas e saídas de estoque e consulta do histórico de movimentações.

## Tecnologias

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Maven
- Swagger / OpenAPI
- Docker
- Docker Compose
- JUnit
- Mockito
- HTML
- CSS
- JavaScript

## Funcionalidades

- Autenticação com JWT
- Proteção de endpoints
- Cadastro de produtos
- Edição de produtos
- Exclusão de produtos
- Busca de produtos
- Cadastro de categorias
- Entrada de estoque
- Saída de estoque
- Validação de estoque insuficiente
- Histórico de movimentações
- Validação de dados
- Tratamento global de erros
- Interface web
- Documentação Swagger
- Testes automatizados
- Execução com Docker

## Estrutura

```text
src/main/java/com/erick/estoque

categoria/
movimentacao/
produto/
security/
exception/
config/
```

## Usuário Teste
```text
Email: avaliador@teste.com

Senha: 123456
```


## Executando com Docker

É necessário ter Docker e Docker Compose instalados.

Clone o repositório:

```bash
git clone URL_DO_REPOSITORIO
```

Entre na pasta:

```bash
cd 01-estoque-api
```

Execute:

```bash
docker compose up --build
```

Depois acesse:

Interface:

```text
http://localhost:8081
```

Swagger:

```text
http://localhost:8081/swagger-ui/index.html
```

## Executando sem Docker

É necessário possuir:

- Java
- Maven
- PostgreSQL

Crie um banco chamado:

```text
estoque
```

Configure as variáveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
APP_JWT_SECRET
```

Depois execute a aplicação pelo IntelliJ ou Maven.

## Principais endpoints

### Autenticação

```text
POST /auth/login
```

### Produtos

```text
GET    /produtos
GET    /produtos/{id}
POST   /produtos
PUT    /produtos/{id}
DELETE /produtos/{id}
```

### Categorias

```text
GET    /categorias
GET    /categorias/{id}
POST   /categorias
PUT    /categorias/{id}
DELETE /categorias/{id}
```

### Movimentações

```text
GET  /movimentacoes
GET  /movimentacoes/produto/{produtoId}
POST /movimentacoes
```

Exemplo de entrada:

```json
{
  "produtoId": 1,
  "tipo": "ENTRADA",
  "quantidade": 5,
  "observacao": "Compra do fornecedor"
}
```

Exemplo de saída:

```json
{
  "produtoId": 1,
  "tipo": "SAIDA",
  "quantidade": 2,
  "observacao": "Venda"
}
```

O sistema impede uma saída maior que a quantidade disponível.

## Testes

Para executar os testes:

```bash
mvn test
```

O projeto possui testes para regras de movimentação de estoque e testes da API.

## Segurança

A API utiliza autenticação JWT.

Após o login, o token deve ser enviado no header:

```text
Authorization: Bearer TOKEN
```

As senhas são armazenadas utilizando BCrypt.

## Autor

Erick