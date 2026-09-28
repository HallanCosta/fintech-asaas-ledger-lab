# Arquitetura atual

O projeto usa uma arquitetura simples para estudar Java e Spring Boot antes da
integração real com o Inter PJ:

```text
HTTP / Spring MVC
        |
Caso de uso de importação
        |
Modelo normalizado do Inter (transações e valores)
        |
Gateway do Inter PJ / PostgreSQL
```

## Fronteiras

- Controllers conhecem HTTP e DTOs.
- Casos de uso coordenam a aplicação.
- O modelo normalizado contém as regras básicas de entrada e não conhece HTTP.
- O adapter do Inter traduz OAuth/mTLS, payloads e erros externos.
- PostgreSQL pode persistir os dados normalizados quando essa etapa for implementada.

CQRS, Event Sourcing e Event-Driven não fazem parte deste projeto. Eles serão
estudados em repositórios separados.
