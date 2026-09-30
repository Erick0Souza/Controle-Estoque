# Funcionalidades do Sistema

Este documento descreve as principais funcionalidades do projeto **Controle de Estoque**.

O sistema foi desenvolvido para gerenciar produtos, categorias, movimentações de estoque, usuários e informações administrativas, utilizando uma API REST em Spring Boot integrada a uma interface web.

---

# 1. Autenticação

## Cadastro de usuário

Endpoint:

```http
POST /auth/register
```

Permite criar uma nova conta quando o cadastro público está habilitado.

Exemplo:

```json
{
  "nomeUsuario": "Usuario Teste",
  "email": "usuario@teste.com",
  "senha": "Senha123"
}
```

O sistema:

- valida o nome de usuário;
- valida o formato do email;
- impede nomes de usuário duplicados;
- impede emails duplicados;
- valida o tamanho da senha;
- protege a senha com BCrypt;
- normaliza o email;
- cria o novo usuário inicialmente com perfil `CONSULTA`.

No ambiente de produção, o cadastro público pode ser desabilitado.

---

## Login

Endpoint:

```http
POST /auth/login
```

Exemplo:

```json
{
  "email": "usuario@teste.com",
  "senha": "Senha123"
}
```

Após autenticação bem-sucedida, a API retorna:

```json
{
  "token": "JWT_GERADO",
  "nomeUsuario": "Usuario Teste",
  "email": "usuario@teste.com",
  "perfil": "CONSULTA"
}
```

O JWT deve ser enviado nas requisições protegidas:

```text
Authorization: Bearer SEU_TOKEN
```

---

## Proteção contra tentativas de login

A aplicação controla falhas de autenticação considerando:

```text
Usuário + endereço IP
Endereço IP
```

Após muitas tentativas incorretas, novas tentativas podem ser temporariamente bloqueadas.

A API pode retornar:

```http
429 Too Many Requests
```

Essa proteção dificulta ataques de força bruta.

---

## Proteção contra enumeração de usuários

Quando o email informado no login não existe, a aplicação ainda realiza uma comparação BCrypt utilizando um hash fictício.

Isso ajuda a reduzir diferenças de tempo de resposta que poderiam indicar se determinado email está cadastrado ou não.

---

# 2. Produtos

Base dos endpoints:

```text
/produtos
```

---

## Listar produtos

```http
GET /produtos
```

A listagem possui paginação, filtros e ordenação.

Filtros disponíveis:

```text
nome
sku
categoriaId
precoMin
precoMax
quantidadeMin
quantidadeMax
```

Paginação:

```text
page
size
```

Ordenação:

```text
sort
direction
```

Exemplo:

```text
GET /produtos?nome=mouse&page=0&size=10&sort=nome&direction=asc
```

O tamanho máximo permitido por página é:

```text
100 registros
```

---

## Buscar produto

```http
GET /produtos/{id}
```

Retorna os dados de um produto específico.

Caso o produto não exista:

```http
404 Not Found
```

---

## Criar produto

```http
POST /produtos
```

O produto pode possuir informações como:

```text
SKU
Nome
Preço
Quantidade
Estoque mínimo
Categoria
Descrição
Imagem
```

O SKU é normalizado e deve ser único.

Caso outro produto já possua o mesmo SKU:

```http
409 Conflict
```

A criação também gera um registro de auditoria.

---

## Editar produto

```http
PUT /produtos/{id}
```

Permite alterar os dados de um produto existente.

O sistema também verifica se o novo SKU já pertence a outro produto.

A alteração gera registro de auditoria.

---

## Excluir produto

```http
DELETE /produtos/{id}
```

A exclusão é realizada pelo serviço responsável pela remoção do produto e das informações relacionadas necessárias.

Essa operação possui acesso restrito.

---

# 3. Imagens dos produtos

Cada produto pode possuir uma imagem associada.

---

## Adicionar imagem

```http
POST /produtos/{id}/imagem
```

A requisição utiliza:

```text
multipart/form-data
```

Campo:

```text
imagem
```

Quando uma nova imagem substitui uma anterior, o sistema também realiza o tratamento do arquivo antigo.

---

## Remover imagem

```http
DELETE /produtos/{id}/imagem
```

Remove a associação com o produto e exclui o arquivo correspondente.

Caso o produto não possua imagem:

```http
404 Not Found
```

---

## Limites de upload

A aplicação possui os seguintes limites globais:

```text
Arquivo: 5 MB
Requisição: 6 MB
```

---

# 4. Estoque mínimo

Cada produto possui um campo:

```text
estoqueMinimo
```

Um produto é considerado com estoque baixo quando:

```text
quantidade <= estoque mínimo
```

Essa informação é utilizada pelo sistema em:

- dashboard;
- relatórios;
- exportações;
- acompanhamento do estoque.

---

# 5. Categorias

Base:

```text
/categorias
```

---

## Listar categorias

```http
GET /categorias
```

---

## Buscar categoria

```http
GET /categorias/{id}
```

Caso não exista:

```http
404 Not Found
```

---

## Criar categoria

```http
POST /categorias
```

Exemplo:

```json
{
  "nome": "Informática"
}
```

O nome é obrigatório.

---

## Editar categoria

```http
PUT /categorias/{id}
```

Permite alterar o nome de uma categoria existente.

---

## Excluir categoria

```http
DELETE /categorias/{id}
```

Operação administrativa.

---

# 6. Movimentações de estoque

Base:

```text
/movimentacoes
```

As movimentações podem ser:

```text
ENTRADA
SAIDA
```

---

## Registrar movimentação

```http
POST /movimentacoes
```

Uma movimentação registra informações relacionadas a:

```text
Produto
Tipo
Quantidade
Data e hora
Responsável
Observação
```

---

## Entrada

Uma movimentação:

```text
ENTRADA
```

aumenta a quantidade disponível do produto.

Exemplo conceitual:

```text
Estoque atual: 10
Entrada: 5

Novo estoque: 15
```

---

## Saída

Uma movimentação:

```text
SAIDA
```

reduz a quantidade disponível.

Exemplo:

```text
Estoque atual: 10
Saída: 4

Novo estoque: 6
```

---

## Proteção contra estoque negativo

O sistema não permite uma saída maior do que a quantidade disponível.

Exemplo:

```text
Estoque atual: 5
Saída solicitada: 10
```

A operação é rejeitada.

---

## Histórico completo

```http
GET /movimentacoes
```

Retorna o histórico das movimentações.

---

## Histórico por produto

```http
GET /movimentacoes/produto/{produtoId}
```

Permite consultar somente as movimentações relacionadas a determinado produto.

---

# 7. Usuários administrativos

Base:

```text
/admin/usuarios
```

Os recursos desta área são destinados ao perfil administrativo.

---

## Listar usuários

```http
GET /admin/usuarios
```

A listagem possui:

- filtros;
- paginação;
- ordenação.

---

## Buscar usuário

```http
GET /admin/usuarios/{id}
```

---

## Alterar perfil

```http
PUT /admin/usuarios/{id}/perfil
```

Exemplo:

```json
{
  "perfil": "OPERADOR"
}
```

Perfis disponíveis:

```text
ADMIN
OPERADOR
CONSULTA
```

Existem regras de segurança para evitar situações como:

- remover o próprio perfil ADMIN;
- deixar o sistema sem nenhum administrador.

A alteração também gera auditoria.

---

## Redefinir senha

```http
PUT /admin/usuarios/{id}/senha
```

Exemplo:

```json
{
  "novaSenha": "NovaSenha123",
  "confirmarSenha": "NovaSenha123"
}
```

O sistema:

- exige confirmação da senha;
- verifica se os dois valores são iguais;
- valida o tamanho;
- gera um novo hash BCrypt;
- registra a ação na auditoria.

---

# 8. Auditoria

Base:

```text
/admin/auditorias
```

A auditoria registra ações importantes realizadas dentro do sistema.

---

## Informações registradas

Um registro pode possuir:

```text
ID
Data e hora
Nome do usuário
Email
Perfil
Ação
Entidade
ID da entidade
Descrição
```

---

## Exemplos de ações

```text
PRODUTO_CRIADO
PRODUTO_EDITADO
PRODUTO_EXCLUIDO

MOVIMENTACAO_ENTRADA
MOVIMENTACAO_SAIDA

PERFIL_USUARIO_ALTERADO
SENHA_USUARIO_REDEFINIDA
```

---

## Listar auditorias

```http
GET /admin/auditorias
```

Filtros disponíveis incluem:

```text
usuario
perfil
acao
entidade
dataInicio
dataFim
```

Também são suportados:

```text
page
size
sort
direction
```

Por padrão, os registros mais recentes são apresentados primeiro.

---

## Consultar auditoria

```http
GET /admin/auditorias/{id}
```

---

# 9. Dashboard administrativo

Endpoint:

```http
GET /admin/dashboard
```

O dashboard reúne indicadores gerais do estoque.

Entre eles:

```text
Total de produtos
Total de categorias
Total de usuários
Total de movimentações
Total de entradas
Total de saídas
Produtos com estoque baixo
Total de unidades em estoque
Valor total do estoque
Atividades recentes
```

---

## Valor do estoque

O valor total considera:

```text
preço do produto × quantidade disponível
```

Exemplo:

```text
Produto A
Preço: R$ 50,00
Quantidade: 10

Valor em estoque: R$ 500,00
```

---

## Atividades recentes

O dashboard também apresenta ações recentes registradas pela auditoria.

A listagem é organizada das atividades mais recentes para as mais antigas.

---

# 10. Relatórios

Base:

```text
/admin/relatorios
```

---

## Relatório de estoque

```http
GET /admin/relatorios/estoque
```

O relatório possui informações como:

```text
Total de produtos
Total de unidades
Quantidade de produtos com estoque baixo
Valor total do estoque
Lista de produtos
```

Cada produto pode apresentar:

```text
ID
SKU
Nome
Categoria
Preço
Quantidade
Estoque mínimo
Status
Valor em estoque
```

---

## Apenas estoque baixo

Utilize:

```text
GET /admin/relatorios/estoque?somenteEstoqueBaixo=true
```

Nesse caso, a lista apresentada contém somente produtos identificados com estoque baixo.

---

## Relatório de movimentações

```http
GET /admin/relatorios/movimentacoes
```

Filtros:

```text
dataInicio
dataFim
tipo
```

Tipos:

```text
ENTRADA
SAIDA
```

Exemplo:

```text
GET /admin/relatorios/movimentacoes?tipo=ENTRADA
```

---

## Filtro por período

Exemplo:

```text
dataInicio=2026-09-01T00:00:00
dataFim=2026-09-30T23:59:59
```

Caso a data inicial seja posterior à data final, a requisição é rejeitada.

---

# 11. Exportações

Base:

```text
/admin/exportacoes
```

Os dados podem ser exportados em:

```text
CSV
XLSX
PDF
```

---

## CSV de estoque

```http
GET /admin/exportacoes/estoque/csv
```

Somente estoque baixo:

```text
GET /admin/exportacoes/estoque/csv?somenteEstoqueBaixo=true
```

---

## CSV de movimentações

```http
GET /admin/exportacoes/movimentacoes/csv
```

Aceita:

```text
dataInicio
dataFim
tipo
```

---

## Excel de estoque

```http
GET /admin/exportacoes/estoque/xlsx
```

---

## Excel de movimentações

```http
GET /admin/exportacoes/movimentacoes/xlsx
```

Aceita filtros por período e tipo.

---

## PDF de estoque

```http
GET /admin/exportacoes/estoque/pdf
```

---

## PDF de movimentações

```http
GET /admin/exportacoes/movimentacoes/pdf
```

Aceita filtros por período e tipo.

---

# 12. Tratamento global de erros

A aplicação possui tratamento padronizado para diferentes problemas.

Entre eles:

```text
Dados inválidos
JSON inválido
Parâmetro obrigatório ausente
Tipo de parâmetro incorreto
Conflito de integridade
Upload muito grande
Recurso inexistente
Acesso não autorizado
Permissão insuficiente
Erros internos
```

Exemplo de resposta:

```json
{
  "status": 400,
  "mensagem": "Descrição do problema",
  "timestamp": "2026-09-29T20:00:00"
}
```

Em produção, detalhes internos e stack traces não são expostos ao cliente.

---

# 13. Interface web

A aplicação possui uma interface construída com:

```text
HTML
CSS
JavaScript
Fetch API
```

Ela consome a API Spring Boot diretamente.

A interface permite acessar funcionalidades como:

- login;
- cadastro de usuário;
- produtos;
- categorias;
- imagens;
- movimentações;
- histórico;
- usuários administrativos;
- dashboard;
- auditoria;
- relatórios;
- exportações.

As opções exibidas dependem do perfil do usuário autenticado.

---

# 14. Documentação da API

Em desenvolvimento, o sistema utiliza:

```text
Swagger
OpenAPI
```

Interface:

```text
http://localhost:8081/swagger-ui/index.html
```

Especificação:

```text
http://localhost:8081/v3/api-docs
```

No perfil de produção, essas interfaces são desabilitadas.

---

# 15. Dados iniciais

O projeto possui atualmente um:

```text
DataInitializer
```

para facilitar a demonstração e desenvolvimento.

Ele pode inserir automaticamente:

```text
Usuário administrador de demonstração
Categorias iniciais
Produtos iniciais
```

Essa inicialização deve ser limitada ou desabilitada antes de uma implantação real em produção.

---

# 16. Backup

O sistema possui scripts para:

```text
Backup do PostgreSQL
Backup dos uploads
Backup completo
Restauração do banco
Restauração dos uploads
Restauração completa
```

Arquivos:

```text
scripts/
├── backup-database.ps1
├── backup-uploads.ps1
├── backup-completo.ps1
├── restore-database.ps1
├── restore-uploads.ps1
└── restore-completo.ps1
```

---

# 17. Testes automatizados

A aplicação possui atualmente:

```text
113 testes automatizados
Failures: 0
Errors: 0
```

Os testes cobrem funcionalidades como:

```text
Autenticação
Segurança
Perfis
Categorias
Produtos
Filtros
Paginação
Movimentações
Usuários
Auditoria
Dashboard
Relatórios
CSV
Excel
PDF
Regras de negócio
Integração
```

O ambiente de testes utiliza:

```text
H2
```

---

# 18. Integração contínua

O projeto utiliza GitHub Actions.

A pipeline executa:

```text
Push / Pull Request
        ↓
Java 17
        ↓
mvn clean test
        ↓
Testes automatizados
        ↓
Validação da imagem Docker
```

Alterações que quebrem os testes ou impeçam a construção da imagem Docker podem ser identificadas automaticamente.

---

# Resumo dos módulos

```text
Autenticação        → Login, cadastro e JWT
Security            → Perfis, autorização e proteção de login
Produtos            → Cadastro e controle dos itens
Categorias          → Organização dos produtos
Movimentações       → Entradas e saídas
Usuários            → Administração de contas
Auditoria           → Registro de ações
Dashboard           → Indicadores do sistema
Relatórios          → Informações consolidadas
Exportações         → CSV, XLSX e PDF
Backup              → Proteção e recuperação dos dados
Front-end           → Interface para utilização da API
Testes              → Validação automatizada
Docker              → Padronização do ambiente
GitHub Actions      → Integração contínua
```

---

# Repositório

```text
https://github.com/Erick0Souza/Controle-Estoque
```