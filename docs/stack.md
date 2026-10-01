# Stack do laboratório

- Java 21;
- Spring Boot e Spring MVC;
- `RestClient` para chamadas HTTP ao Asaas;
- Spring JDBC para estudar SQL e persistência explícita;
- PostgreSQL para os dados normalizados;
- Redis disponível para exercícios de cache e idempotência;
- Maven executado dentro do Docker;
- Docker Compose para o ambiente local;
- React + Vite para o dashboard.

## Princípios

- usar `BigDecimal` por meio do value object `Money`;
- manter API keys fora do Git;
- separar DTOs externos do modelo normalizado;
- testar regras financeiras sem depender do Sandbox;
- tratar cobranças e transferências como operações assíncronas até confirmação;
- começar com polling/HTTP simples e só depois estudar webhooks.

CQRS, Event Sourcing e Event-Driven ficam fora deste repositório.
