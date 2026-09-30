# Guia de Testes

Este documento apresenta a estratégia de testes automatizados utilizada no projeto **Controle de Estoque**.

A aplicação possui uma suíte responsável por validar regras de negócio, autenticação, autorização, endpoints REST, persistência, movimentações de estoque, relatórios, exportações e funcionalidades administrativas.

---

# Visão geral

A versão atual possui:

```text
113 testes automatizados
Failures: 0
Errors: 0
```

Os testes utilizam principalmente:

| Tecnologia | Utilização |
|---|---|
| JUnit 5 | Estrutura e execução dos testes |
| Spring Boot Test | Testes utilizando o contexto Spring |
| MockMvc | Testes dos endpoints HTTP |
| Mockito | Simulação de dependências |
| Spring Security Test | Simulação de autenticação e perfis |
| H2 | Banco de dados em memória |
| Apache POI | Validação das exportações XLSX |
| PDFBox | Validação das exportações PDF |

---

# Estrutura atual da suíte

A suíte está localizada em:

```text
src/test/java/com/erick/estoque
```

Estrutura:

```text
src/test/java/com/erick/estoque
│
├── AdminDashboardRelatoriosExportacoesTest.java
├── ApiIntegrationTest.java
├── ApplicationTest.java
├── MovimentacaoServiceTest.java
│
├── auditoria
│   └── AuditoriaControllerTest.java
│
├── categoria
│   └── CategoriaControllerTest.java
│
├── movimentacao
│   └── MovimentacaoControllerTest.java
│
├── produto
│   └── ProdutoControllerTest.java
│
└── security
    ├── AuthControllerTest.java
    ├── SecurityAuthorizationTest.java
    └── UsuarioAdminControllerTest.java
```

---

# Quantidade de testes

| Classe | Testes |
|---|---:|
| `AdminDashboardRelatoriosExportacoesTest` | 13 |
| `ApiIntegrationTest` | 5 |
| `ApplicationTest` | 1 |
| `MovimentacaoServiceTest` | 3 |
| `AuditoriaControllerTest` | 8 |
| `CategoriaControllerTest` | 10 |
| `MovimentacaoControllerTest` | 13 |
| `ProdutoControllerTest` | 22 |
| `AuthControllerTest` | 10 |
| `SecurityAuthorizationTest` | 18 |
| `UsuarioAdminControllerTest` | 10 |
| **Total** | **113** |

---

# Ambiente de testes

As configurações específicas ficam em:

```text
src/test/resources/application-test.yml
```

O banco utilizado é:

```text
H2
```

configurado em memória e em modo de compatibilidade com PostgreSQL.

Isso permite testar a camada de persistência sem precisar iniciar um PostgreSQL real.

A configuração utiliza:

```text
jdbc:h2:mem:estoquetest
MODE=PostgreSQL
```

O schema é criado para os testes e descartado posteriormente utilizando:

```text
ddl-auto=create-drop
```

---

# Perfil de testes

Os testes que utilizam o contexto Spring são executados com o perfil:

```text
test
```

Nesse ambiente:

```text
Banco: H2
Swagger: desabilitado
OpenAPI: desabilitado
Cadastro público: habilitado
JWT: chave exclusiva de teste
```

Nenhuma chave real de produção deve ser utilizada nos testes.

---

# ApplicationTest

Arquivo:

```text
ApplicationTest.java
```

Quantidade:

```text
1 teste
```

Esse teste verifica se o contexto principal do Spring Boot consegue ser carregado.

Exemplo de problema detectável:

```text
Bean inválido
Dependência ausente
Erro de configuração
Falha durante inicialização do Spring
```

Se esse teste falhar, normalmente significa que a aplicação não conseguiu iniciar corretamente.

---

# ApiIntegrationTest

Arquivo:

```text
ApiIntegrationTest.java
```

Quantidade:

```text
5 testes
```

Essa classe realiza verificações integradas da API utilizando o contexto real da aplicação em ambiente de teste.

Ela ajuda a validar a comunicação entre diferentes camadas, como:

```text
Controller
Security
Repository
Banco H2
Serialização JSON
```

---

# AuthControllerTest

Arquivo:

```text
security/AuthControllerTest.java
```

Quantidade:

```text
10 testes
```

Responsável por validar funcionalidades relacionadas à autenticação.

São verificadas situações como:

```text
Cadastro de usuário
Login correto
Credenciais incorretas
Validação de senha
Usuário inexistente
Cadastro duplicado
Proteção contra tentativas repetidas
Respostas HTTP de autenticação
```

Esses testes são especialmente importantes porque a autenticação é a porta de entrada para os recursos protegidos da aplicação.

---

# SecurityAuthorizationTest

Arquivo:

```text
security/SecurityAuthorizationTest.java
```

Quantidade:

```text
18 testes
```

Essa é uma das principais classes de segurança do projeto.

Ela verifica se cada perfil realmente possui somente as permissões configuradas.

Perfis testados:

```text
ADMIN
OPERADOR
CONSULTA
```

Exemplos de cenários:

```text
CONSULTA pode visualizar produtos
CONSULTA não pode cadastrar produtos

OPERADOR pode cadastrar produto
OPERADOR pode editar produto
OPERADOR não pode excluir produto

ADMIN pode utilizar recursos administrativos

OPERADOR não pode acessar /admin/**
CONSULTA não pode acessar /admin/**
```

Esses testes ajudam a impedir regressões de autorização.

---

# CategoriaControllerTest

Arquivo:

```text
categoria/CategoriaControllerTest.java
```

Quantidade:

```text
10 testes
```

Valida operações relacionadas às categorias.

Entre os cenários cobertos estão:

```text
Listagem
Busca por ID
Criação
Edição
Exclusão
Categoria inexistente
Validação dos dados
Categoria duplicada
Autorização
```

Durante testes de duplicidade, podem aparecer mensagens de violação de chave única no log.

Isso pode ser esperado quando o próprio objetivo do teste é verificar se o banco bloqueia dados duplicados.

O importante é o resultado final da suíte:

```text
Failures: 0
Errors: 0
```

---

# ProdutoControllerTest

Arquivo:

```text
produto/ProdutoControllerTest.java
```

Quantidade:

```text
22 testes
```

É atualmente a classe com maior quantidade de testes.

Ela cobre funcionalidades como:

```text
Criação
Consulta
Edição
Exclusão
SKU
SKU duplicado
Filtros
Paginação
Ordenação
Estoque mínimo
Validações
Imagens
Categorias
Permissões
```

Os testes também verificam condições inválidas de filtros e paginação.

Exemplo:

```text
page < 0
size < 1
size > 100
preço mínimo > preço máximo
quantidade mínima > quantidade máxima
campo de ordenação inválido
```

---

# MovimentacaoServiceTest

Arquivo:

```text
MovimentacaoServiceTest.java
```

Quantidade:

```text
3 testes
```

Essa classe foca diretamente nas regras de negócio da movimentação.

Exemplos:

```text
ENTRADA aumenta estoque
SAIDA reduz estoque
SAIDA maior que o estoque disponível é rejeitada
```

Esse tipo de teste valida a regra sem depender exclusivamente da camada HTTP.

---

# MovimentacaoControllerTest

Arquivo:

```text
movimentacao/MovimentacaoControllerTest.java
```

Quantidade:

```text
13 testes
```

Valida os endpoints de movimentações.

Entre os comportamentos testados estão:

```text
Criação de entrada
Criação de saída
Consulta do histórico
Consulta por produto
Produto inexistente
Quantidade inválida
Estoque insuficiente
Usuário responsável
Perfis e permissões
```

---

# UsuarioAdminControllerTest

Arquivo:

```text
security/UsuarioAdminControllerTest.java
```

Quantidade:

```text
10 testes
```

Testa a área administrativa de usuários.

Inclui cenários relacionados a:

```text
Listagem
Consulta
Filtros
Paginação
Alteração de perfil
Redefinição de senha
Usuário inexistente
Proteção do próprio ADMIN
Proteção do último administrador
```

Esses testes ajudam a impedir que uma alteração administrativa deixe a aplicação sem usuários com acesso de administrador.

---

# AuditoriaControllerTest

Arquivo:

```text
auditoria/AuditoriaControllerTest.java
```

Quantidade:

```text
8 testes
```

Valida a consulta dos registros de auditoria.

São verificadas funcionalidades como:

```text
Listagem
Consulta por ID
Filtros
Período
Paginação
Ordenação
Registro inexistente
Controle de acesso
```

---

# Dashboard, relatórios e exportações

Arquivo:

```text
AdminDashboardRelatoriosExportacoesTest.java
```

Quantidade:

```text
13 testes
```

Essa classe cobre diversos recursos administrativos.

---

## Dashboard

São validados indicadores como:

```text
Total de produtos
Total de categorias
Total de usuários
Total de movimentações
Entradas
Saídas
Estoque baixo
Total de unidades
Valor total do estoque
Atividades recentes
```

Também é validado que usuários sem perfil ADMIN não conseguem acessar o dashboard.

---

## Relatório de estoque

Os testes verificam:

```text
Totais
Produtos
Ordenação
Valor do estoque
Produtos com estoque baixo
Filtro somenteEstoqueBaixo
```

---

## Relatório de movimentações

São testados filtros como:

```text
dataInicio
dataFim
tipo
```

Também existe teste para:

```text
dataInicio > dataFim
```

que deve resultar em requisição inválida.

---

## CSV

Os testes verificam:

```text
Geração do arquivo
Content-Type
Content-Disposition
Conteúdo
Filtros
Resumo dos dados
```

---

## XLSX

As exportações Excel são abertas novamente pelos testes utilizando Apache POI.

Isso permite verificar se o conteúdo gerado é realmente um arquivo XLSX válido.

São validados pontos como:

```text
Nome da planilha
Cabeçalhos
Quantidade de linhas
Conteúdo gerado
```

---

## PDF

Os PDFs são novamente carregados utilizando PDFBox.

Isso verifica se o arquivo produzido é um PDF válido.

Também existe cobertura para geração de relatório mesmo quando não existem registros.

---

# MockMvc

Diversos testes utilizam:

```text
MockMvc
```

Ele permite simular requisições HTTP sem precisar iniciar um servidor web externo.

Exemplo conceitual:

```java
mockMvc.perform(
        get("/produtos")
)
.andExpect(
        status().isOk()
);
```

Isso permite testar:

```text
URL
Método HTTP
Status
JSON
Headers
Autorização
Validação
```

---

# Usuários simulados

Os testes de segurança podem utilizar:

```text
@WithMockUser
```

Exemplo conceitual:

```java
@WithMockUser(
        username = "admin@teste.com",
        roles = "ADMIN"
)
```

Isso permite testar uma requisição como se tivesse sido feita por determinado perfil.

---

# Banco isolado

Antes de determinados testes, os dados são preparados especificamente para o cenário que será validado.

A ideia é evitar que:

```text
um teste dependa de outro
```

Cada teste deve conseguir determinar o próprio estado necessário.

---

# Executando todos os testes

Na raiz do projeto:

```bash
mvn clean test
```

Resultado esperado:

```text
Tests run: 113
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

O número de testes poderá aumentar conforme novas funcionalidades forem adicionadas.

---

# IntelliJ IDEA

Também é possível utilizar:

```text
Maven
└── Lifecycle
    └── test
```

Outra opção é clicar com o botão direito em:

```text
src/test/java
```

e selecionar:

```text
Run 'All Tests'
```

---

# Executar somente uma classe

Exemplo:

```bash
mvn -Dtest=ProdutoControllerTest test
```

Outro exemplo:

```bash
mvn -Dtest=SecurityAuthorizationTest test
```

---

# Executar somente um teste

É possível utilizar:

```bash
mvn -Dtest=ProdutoControllerTest#nomeDoMetodo test
```

Isso é útil ao investigar uma falha específica.

---

# Clean Test

A execução recomendada antes de commits importantes é:

```bash
mvn clean test
```

O comando:

```text
clean
```

remove arquivos de builds anteriores.

O comando:

```text
test
```

compila e executa a suíte automatizada.

---

# Teste antes do build

Antes de considerar uma versão estável, recomenda-se executar:

```bash
mvn clean test
```

e depois:

```bash
mvn clean package
```

Resultado esperado:

```text
BUILD SUCCESS
```

---

# Relatórios do Maven Surefire

Depois dos testes, o Maven gera relatórios em:

```text
target/surefire-reports/
```

Essa pasta pode conter arquivos como:

```text
TEST-com.erick.estoque.ApplicationTest.xml
com.erick.estoque.ApplicationTest.txt
```

Eles são úteis para investigar falhas.

---

# Como interpretar o resultado

## Tests run

```text
Tests run: 113
```

Quantidade de testes executados.

---

## Failures

```text
Failures: 0
```

Uma `Failure` normalmente significa que o teste executou, mas uma expectativa não foi atendida.

Exemplo:

```text
Esperado: HTTP 200
Recebido: HTTP 403
```

---

## Errors

```text
Errors: 0
```

Um `Error` significa que ocorreu uma exceção inesperada durante o teste.

Exemplos:

```text
NullPointerException
Erro ao inicializar o Spring
Erro de banco
Bean inexistente
```

---

## Skipped

```text
Skipped: 0
```

Representa testes ignorados ou desabilitados.

---

## BUILD SUCCESS

```text
BUILD SUCCESS
```

Significa que o processo Maven foi concluído com sucesso.

---

## BUILD FAILURE

```text
BUILD FAILURE
```

Significa que alguma etapa falhou.

Nesse caso, deve-se procurar primeiro por:

```text
[ERROR]
```

e depois pelo primeiro teste que apresentou:

```text
FAILURE
ERROR
```

Normalmente o primeiro erro real é mais importante do que os erros seguintes, pois alguns deles podem ser apenas consequência do primeiro problema.

---

# Warnings

Um warning não significa necessariamente que o teste falhou.

Exemplo:

```text
[WARNING]
```

A execução pode possuir warnings e ainda terminar em:

```text
BUILD SUCCESS
```

Warnings devem ser analisados, mas não devem ser confundidos automaticamente com erros.

---

# GitHub Actions

O projeto também executa testes automaticamente através do GitHub Actions.

Workflow:

```text
.github/workflows/ci.yml
```

Quando ocorre:

```text
push na main
pull request para main
execução manual
```

o GitHub executa a pipeline.

Fluxo:

```text
Checkout
   ↓
Java 17
   ↓
Maven
   ↓
mvn clean test
   ↓
113 testes
   ↓
Validação Docker
```

O job de Docker só é executado depois que os testes automatizados terminam com sucesso.

---

# Por que utilizar H2 nos testes?

Utilizar H2 permite:

```text
Execução rápida
Banco isolado
Não depender do PostgreSQL local
Limpeza automática
Facilidade no CI
```

O modo de compatibilidade com PostgreSQL ajuda a aproximar o comportamento do banco utilizado em produção.

Ainda assim, testes com H2 não substituem completamente uma validação final com PostgreSQL real antes de uma implantação em produção.

---

# Estratégia de testes do projeto

A suíte atual combina diferentes níveis de validação:

```text
Testes de regras de negócio
        ↓
Testes de controllers
        ↓
Testes de segurança
        ↓
Testes de integração
        ↓
Testes de relatórios
        ↓
Testes de exportações
```

Dessa forma, uma alteração em uma parte do sistema pode ser detectada antes de chegar ao usuário final.

---

# Checklist antes de um commit importante

```text
[ ] Código compila
[ ] mvn clean test executado
[ ] 113 testes ou quantidade atual passaram
[ ] Failures = 0
[ ] Errors = 0
[ ] Nenhuma credencial foi adicionada ao código
[ ] .env não foi incluído no Git
[ ] Build Docker continua funcionando
```

---

# Checklist antes de uma release

```text
[ ] Testes automatizados passando
[ ] Maven package passando
[ ] Docker build passando
[ ] Banco PostgreSQL validado
[ ] Perfis ADMIN/OPERADOR/CONSULTA conferidos
[ ] Login validado
[ ] Entrada e saída de estoque validadas
[ ] Relatórios conferidos
[ ] Exportações conferidas
[ ] Backup conferido
[ ] Variáveis de produção configuradas
```

---

# Estado atual

```text
Testes automatizados: 113
Failures: 0
Errors: 0
Package: BUILD SUCCESS
```

A suíte cobre as principais áreas funcionais e de segurança do sistema.

---

# Repositório

```text
https://github.com/Erick0Souza/Controle-Estoque
```