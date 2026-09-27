# Stack do laboratório

- Java 21;
- Spring Boot;
- Spring MVC para a API HTTP;
- Spring JDBC para estudar SQL e persistência explícita;
- PostgreSQL para os dados normalizados da integração;
- Maven para build e testes;
- Docker Compose para o ambiente local.

## Princípios

- usar `BigDecimal` por meio do value object `Money`;
- manter certificados e credenciais fora do Git;
- separar DTOs externos do domínio;
- testar regras financeiras antes de conectar ao sandbox;
- manter o adapter do Inter substituível por uma implementação fake.

Redis, React, CQRS, Event Sourcing e Event-Driven não são necessários para o
escopo atual.
