# Fronteira com a API do Asaas

O adapter Asaas fica fora do modelo normalizado. A responsabilidade dele é:

1. montar a URL de Sandbox ou produção;
2. enviar a API key no header `access_token`;
3. chamar saldo, extrato, cobrança Pix, QR Code e transferência;
4. transformar o extrato externo em `NormalizedTransaction`;
5. deixar o caso de uso independente do JSON do provedor.

O Asaas não exige OAuth nem certificado mTLS para este fluxo didático. A API
key é específica de cada ambiente e deve ficar somente nos arquivos ignorados
`.env.local` e `.env.production`.

## URLs e autenticação

| Ambiente | Base URL |
| --- | --- |
| Sandbox | `https://api-sandbox.asaas.com/v3` |
| Produção | `https://api.asaas.com/v3` |

O cliente envia:

```http
access_token: sua-api-key
User-Agent: fintech-pix-lab/0.1
```

## Fluxo de estudo no Sandbox

1. Criar a conta de teste e uma API key.
2. Habilitar `ASAAS_ENABLED=true` em `server/.env.local`.
3. Consultar `GET /api/asaas/balance`.
4. Criar um cliente fictício no Asaas.
5. Criar uma cobrança com `billingType=PIX`.
6. Confirmar o recebimento pela interface do Sandbox para gerar saldo fictício.
7. Consultar saldo e extrato.
8. Testar uma transferência para uma chave de homologação.
9. Acompanhar mudanças de status por webhook.

O Sandbox não movimenta dinheiro real. A confirmação da cobrança é uma
simulação feita pela interface do próprio ambiente.

## Rotas implementadas

- `GET /api/asaas/balance` → `GET /finance/balance`;
- `GET /api/asaas/statement` → `GET /financialTransactions`;
- `POST /api/asaas/payments/pix` → `POST /payments` com `billingType=PIX`;
- `GET /api/asaas/payments/{id}/pix-qrcode` → `GET /payments/{id}/pixQrCode`;
- `POST /api/asaas/transfers/pix` → `POST /transfers` com `operationType=PIX`.

As rotas só são registradas quando `ASAAS_ENABLED=true`, evitando que o server
faça chamadas externas por acidente.

## Próximos exercícios

- persistir o identificador de cada operação;
- mapear erros `400`, `401`, `403` e `429` para respostas internas claras;
- implementar idempotência com `externalReference`;
- receber eventos de cobrança e transferência em webhook;
- criar um fake do `AsaasGateway` para testes de caso de uso.

Referências: [chaves de API](https://docs.asaas.com/docs/chaves-de-api),
[saldo](https://docs.asaas.com/reference/retrieve-account-balance),
[extrato](https://docs.asaas.com/reference/retrieve-extract),
[cobrança Pix](https://docs.asaas.com/reference/create-new-payment),
[transferência Pix](https://docs.asaas.com/reference/transfer-to-another-institution-account-or-pix-key)
e [webhooks](https://docs.asaas.com/docs/webhooks-1).
