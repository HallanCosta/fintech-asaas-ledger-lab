# Fronteira com a API PJ do Inter

O adapter do Inter deve ficar fora do domínio. A responsabilidade dele é:

1. obter/renovar o token OAuth;
2. configurar o cliente HTTP com certificado e chave da integração;
3. consultar extrato/saldo ou receber callbacks;
4. transformar o payload externo em `InterTransaction`;
5. entregar o resultado ao caso de uso de importação.

O domínio não deve conhecer nomes de campos, códigos HTTP, headers ou DTOs do banco.

## Fatos úteis da documentação oficial

- A criação da integração é feita no Internet Banking PJ; o portal orienta baixar chaves/certificado e ativá-los.
- A API oferece, entre outros, extrato, saldos, pagamentos e Pix.
- O token tem validade informada de 60 minutos; gerar um token por requisição pode atingir o limite de emissão de token.
- Callbacks incluem o header `x-conta-corrente`, útil para selecionar a conta quando um endpoint recebe eventos de várias contas.
- A consulta de extrato é paginada e as APIs podem responder `429` quando o rate limit é excedido.

Fontes: [Portal do desenvolvedor Inter Empresas](https://developers.inter.co/) e [Dúvidas frequentes oficiais](https://developers.inter.co/duvidas-frequentes).

## Implementação atual

O adapter real já está preparado, mas desligado por padrão:

- `InterOAuthClient` faz `client_credentials`, usa Basic Auth e mantém o token em cache;
- `InterClientConfiguration` carrega o certificado PKCS12 e monta o mTLS;
- `InterBankingClient` chama saldo e extrato e converte o JSON externo para `InterTransaction`;
- `InterPixClient` cria/consulta uma cobrança Pix imediata;
- `InterController` expõe rotas de laboratório somente quando `INTER_ENABLED=true`.

Os testes usam `MockRestServiceServer`, sem credenciais e sem chamadas de rede ao
Inter. Quando as credenciais estiverem disponíveis, o próximo passo será
validar o mesmo fluxo no sandbox e observar os payloads reais.

Referências específicas: [SDK Java oficial](https://developers.inter.co/docs/sdks/sdk-java)
e [código-fonte oficial do SDK](https://github.com/inter-co/pj-sdk-java).

Webhook e polling deverão convergir para o mesmo caso de uso de importação. A
origem muda; o modelo normalizado permanece estável.
