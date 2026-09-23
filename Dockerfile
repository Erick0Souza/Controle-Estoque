# =========================
# ETAPA 1 - BUILD
# =========================

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia primeiro o pom.xml
COPY pom.xml .

# Baixa as dependências
RUN mvn dependency:go-offline

# Copia o código fonte
COPY src ./src

# Compila a aplicação
RUN mvn clean package -DskipTests


# =========================
# ETAPA 2 - EXECUÇÃO
# =========================

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]