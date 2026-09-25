# Fronteira com a API PJ do Inter

O adapter do Inter deve ficar fora do domínio. A responsabilidade dele é:

1. obter/renovar o token OAuth;
2. configurar o cliente HTTP com certificado e chave da integração;
3. consultar extrato/saldo ou receber callbacks;
4. transformar o payload externo em `InterTransaction`;
5. entregar o resultado ao mesmo caso de uso de importação.

O domínio não deve conhecer nomes de campos, códigos HTTP, headers ou DTOs do banco.

## Fatos úteis da documentação oficial

- A criação da integração é feita no Internet Banking PJ; o portal orienta baixar chaves/certificado e ativá-los.
- A API oferece, entre outros, extrato, saldos, pagamentos e Pix.
- O token tem validade informada de 60 minutos; gerar um token por requisição pode atingir o limite de emissão de token.
- Callbacks incluem o header `x-conta-corrente`, útil para selecionar a conta quando um endpoint recebe eventos de várias contas.
- A consulta de extrato é paginada e as APIs podem responder `429` quando o rate limit é excedido.

Fontes: [Portal do desenvolvedor Inter Empresas](https://developers.inter.co/) e [Dúvidas frequentes oficiais](https://developers.inter.co/duvidas-frequentes).

## Decisão de laboratório

Antes de conectar credenciais reais, usamos uma implementação fake do gateway e payloads fixos. Assim conseguimos estudar idempotência, replay, reconciliação e falhas sem risco operacional.

Quando o adapter real entrar, webhook e polling deverão convergir para o mesmo `PixImportService`. A origem muda; as invariantes do ledger não.
