# Stack escolhida

## Server

- Java 21;
- Spring Boot 4.1.1;
- Spring MVC para a API HTTP;
- Spring JDBC para estudar SQL, transações e o Event Store explicitamente;
- PostgreSQL como fonte durável dos eventos, ledger e projections;
- Spring Data Redis/Lettuce para cache, locks curtos, idempotência auxiliar e experimentos com Streams;
- Actuator para health checks;
- JUnit + Testcontainers nas próximas etapas.

Spring Boot é o caminho mais comum para aplicações Java web de produção e fornece servidor embutido, auto-configuração, health checks e configuração externa. Para este laboratório, Spring JDBC é mais didático que esconder o armazenamento do Event Store atrás de ORM.

## Por que PostgreSQL?

PostgreSQL será a fonte de verdade inicial porque permite estudar:

- transações ACID;
- constraints de unicidade para idempotência;
- locks e concorrência;
- índices e paginação;
- SQL do Event Store, ledger e projections;
- Outbox na mesma transação do domínio.

## Por que Redis?

Redis entra como infraestrutura auxiliar, não como Event Store principal. Usaremos Redis para:

- cache de consultas;
- deduplicação temporária durante a ingestão;
- rate limiting;
- locks distribuídos em experimentos;
- comparação posterior entre Pub/Sub, Streams e Outbox.

O Event Store e o ledger não devem depender de Redis para preservar histórico financeiro.

## O que não entra agora

- Kafka: só depois do Outbox e do processamento assíncrono local;
- microserviços: primeiro monólito modular;
- JPA/Hibernate: pode ser usado para CRUD auxiliar, mas o Event Store será JDBC/SQL explícito;
- Kubernetes: não resolve nenhum problema do primeiro laboratório;
- TigerBeetle/KurrentDB: entram como comparação, não como fundação inicial.

## Web

- React 19;
- Vite;
- CSS próprio para manter a interface simples e revelar os conceitos;
- proxy local `/api` para `server:8080`.

O frontend começa com dados locais e será conectado à API por etapas. Assim o design não fica bloqueado pela integração real com o Inter.
