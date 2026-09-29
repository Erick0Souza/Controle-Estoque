# Guia de Instalação e Execução

Este documento explica como executar o projeto **Controle de Estoque** em ambiente local.

A forma recomendada é utilizar **Docker Compose**, pois ele configura automaticamente a API Spring Boot e o PostgreSQL.

---

## 1. Requisitos

Para executar utilizando Docker, instale:

- Git
- Docker
- Docker Compose

Verifique se o Docker está funcionando:

```bash
docker --version
```

```bash
docker compose version
```

---

## 2. Clonar o projeto

Clone o repositório:

```bash
git clone https://github.com/Erick0Souza/Controle-Estoque.git
```

Entre na pasta:

```bash
cd Controle-Estoque
```

---

## 3. Configurar as variáveis de ambiente

O projeto possui o arquivo:

```text
.env.example
```

Crie uma cópia chamada:

```text
.env
```

### Windows PowerShell

```powershell
Copy-Item .env.example .env
```

### Linux / macOS

```bash
cp .env.example .env
```

---

## 4. Configuração do `.env`

O arquivo deve possuir uma configuração semelhante a:

```env
SPRING_PROFILES_ACTIVE=dev

POSTGRES_DB=estoque
POSTGRES_USER=postgres
POSTGRES_PASSWORD=troque-esta-senha

APP_JWT_SECRET=gere-uma-chave-segura-com-pelo-menos-32-bytes

CORS_ALLOWED_ORIGINS=http://localhost:8081,http://127.0.0.1:8081

PORT=8081
```

### APP_JWT_SECRET

A variável:

```text
APP_JWT_SECRET
```

é obrigatória.

A aplicação não inicia caso ela não esteja configurada.

A chave deve possuir pelo menos:

```text
32 bytes
```

Utilize uma chave própria e não publique a chave real no GitHub.

---

## 5. Iniciar a aplicação

Execute:

```bash
docker compose up -d --build
```

O Docker irá:

```text
1. Criar o PostgreSQL
2. Aguardar o banco ficar disponível
3. Construir a aplicação Spring Boot
4. Iniciar a API
5. Criar os volumes persistentes
```

---

## 6. Verificar os containers

Execute:

```bash
docker compose ps
```

O resultado deve apresentar os serviços:

```text
estoque-postgres
estoque-api
```

Os dois devem estar em execução.

---

## 7. Acessar a aplicação

Com a porta padrão:

```text
http://localhost:8081
```

A interface web é servida pela própria aplicação Spring Boot.

---

## 8. Swagger

No perfil de desenvolvimento:

```env
SPRING_PROFILES_ACTIVE=dev
```

o Swagger fica habilitado.

Acesse:

```text
http://localhost:8081/swagger-ui/index.html
```

A documentação OpenAPI também fica disponível em:

```text
http://localhost:8081/v3/api-docs
```

---

## 9. Autenticação

A maior parte dos endpoints exige autenticação.

Primeiro realize:

```http
POST /auth/login
```

Após obter o JWT, envie:

```text
Authorization: Bearer SEU_TOKEN
```

No Swagger:

```text
Authorize
→ informe o JWT
→ confirme
```

---

## 10. Cadastro de usuário

No perfil `dev`, o cadastro público está habilitado.

Endpoint:

```http
POST /auth/register
```

Exemplo:

```json
{
  "nomeUsuario": "Usuario Teste",
  "email": "usuario@teste.com",
  "senha": "Senha123"
}
```

Novos usuários recebem inicialmente o perfil:

```text
CONSULTA
```

---

## 11. Dados iniciais

Atualmente o projeto possui um `DataInitializer` para facilitar testes locais.

Ele pode criar automaticamente:

```text
Usuário administrador de demonstração
Categorias
Produtos iniciais
```

Esse comportamento é destinado principalmente ao ambiente de desenvolvimento.

---

## 12. Logs

Para acompanhar os logs da API:

```bash
docker compose logs -f api
```

Para acompanhar o PostgreSQL:

```bash
docker compose logs -f postgres
```

Para sair da visualização dos logs:

```text
CTRL + C
```

Isso não encerra os containers.

---

## 13. Reiniciar os serviços

```bash
docker compose restart
```

Ou somente a API:

```bash
docker compose restart api
```

---

## 14. Parar a aplicação

Execute:

```bash
docker compose down
```

Os containers serão removidos, mas os dados permanecerão nos volumes Docker.

---

## 15. Remover também os dados

Caso queira apagar completamente o banco e os uploads locais:

```bash
docker compose down -v
```

> Atenção: esse comando remove os volumes e os dados armazenados neles.

---

# Persistência

O projeto utiliza os volumes:

```text
estoque_postgres_data
estoque_uploads_data
```

Eles armazenam respectivamente:

```text
Banco PostgreSQL
Arquivos enviados para a aplicação
```

Por isso, executar:

```bash
docker compose down
```

não apaga os dados.

---

# Executando os testes

Os testes utilizam H2 e não precisam do PostgreSQL.

Com Maven instalado:

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

# Gerando o build

Execute:

```bash
mvn clean package
```

O arquivo `.jar` será criado em:

```text
target/
```

---

# Executando sem Docker

Também é possível executar a aplicação diretamente com Java e Maven.

Nesse caso, é necessário possuir:

```text
Java 17
Maven
PostgreSQL
```

Configure as variáveis de ambiente:

```text
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://localhost:5432/estoque
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
APP_JWT_SECRET=sua_chave_com_pelo_menos_32_bytes
PORT=8081
```

Depois execute:

```bash
mvn spring-boot:run
```

Ou gere o `.jar`:

```bash
mvn clean package
```

E execute:

```bash
java -jar target/SEU_ARQUIVO.jar
```

---

# Porta personalizada

Por padrão:

```text
8081
```

Para utilizar outra porta, altere:

```env
PORT=9090
```

Com Docker, a aplicação poderá ser acessada por:

```text
http://localhost:9090
```

---

# Perfis disponíveis

## DEV

```env
SPRING_PROFILES_ACTIVE=dev
```

Características:

```text
PostgreSQL
Swagger habilitado
Cadastro público habilitado
Hibernate ddl-auto=update
```

---

## TEST

Utilizado automaticamente pelos testes.

Características:

```text
H2
Swagger desabilitado
Ambiente isolado
```

---

## PROD

```env
SPRING_PROFILES_ACTIVE=prod
```

Características:

```text
Swagger desabilitado
Cadastro público desabilitado
ddl-auto=validate
Mensagens internas ocultadas
Credenciais obrigatórias por variáveis de ambiente
```

O perfil de produção não deve ser utilizado localmente sem configurar corretamente o banco e todas as variáveis necessárias.

---

# Problemas comuns

## APP_JWT_SECRET não definida

Se aparecer uma mensagem semelhante a:

```text
APP_JWT_SECRET nao definida
```

confirme se existe um arquivo:

```text
.env
```

e se ele contém:

```env
APP_JWT_SECRET=sua-chave-com-pelo-menos-32-bytes
```

---

## Porta 8081 já está sendo utilizada

Altere:

```env
PORT=8082
```

Depois execute novamente:

```bash
docker compose up -d --build
```

Acesse:

```text
http://localhost:8082
```

---

## PostgreSQL não inicia

Confira os logs:

```bash
docker compose logs postgres
```

E verifique:

```bash
docker compose ps
```

---

## API não inicia

Confira:

```bash
docker compose logs api
```

Problemas comuns:

```text
APP_JWT_SECRET ausente
Banco ainda indisponível
Variáveis incorretas
Porta ocupada
```

---

## Recriar completamente o ambiente

Somente se puder apagar os dados locais:

```bash
docker compose down -v
docker compose up -d --build
```

---

# Estrutura do ambiente Docker

```text
Computador
│
├── Docker Compose
│
├── estoque-api
│   └── Spring Boot / Java 17
│
├── estoque-postgres
│   └── PostgreSQL 16
│
├── estoque_postgres_data
│   └── Dados do banco
│
└── estoque_uploads_data
    └── Imagens enviadas
```

---

# Segurança

Nunca publique no GitHub:

```text
.env
Senhas reais
APP_JWT_SECRET real
Tokens JWT
Credenciais do banco de produção
```

O repositório deve possuir somente:

```text
.env.example
```

com valores de exemplo.

---

# Repositório

https://github.com/Erick0Souza/Controle-Estoque