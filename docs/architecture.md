# Arquitetura atual

O projeto mantém uma arquitetura pequena para estudar Java e Spring Boot antes
de adicionar persistência ou processamento assíncrono:

```text
HTTP / Spring MVC
        |
Controller de laboratório
        |
Caso de uso / modelo normalizado
        |
Adapter Asaas / PostgreSQL
```

## Fronteiras

- Controllers conhecem HTTP e DTOs da API interna.
- O adapter conhece URL, headers e payloads do Asaas.
- `NormalizedTransaction` e `Money` não conhecem HTTP nem o JSON externo.
- PostgreSQL será usado quando a etapa de persistência for estudada.
- Redis permanece disponível no Compose, mas não é requisito para o fluxo atual.

O Asaas é uma dependência externa substituível. O contrato `AsaasGateway` deixa
claro onde uma implementação fake pode ser usada nos testes sem credencial.

CQRS, Event Sourcing e Event-Driven não fazem parte deste projeto. O backlog
prioriza primeiro o fluxo síncrono: chamar API, validar resposta, normalizar e
exibir o resultado.
