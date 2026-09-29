package com.hallancosta.inter;

import com.hallancosta.inter.auth.InterOAuthClient;
import com.hallancosta.inter.config.InterProperties;
import com.hallancosta.inter.model.ExternalTransactionDirection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

class InterBankingClientTest {

    @Test
    void consultaExtratoEConverteRespostaDoInterParaModeloInterno() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://inter.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        InterProperties properties = properties();
        InterOAuthClient oauthClient = oauthClient(restClient, properties);
        InterBankingClient client = new InterBankingClient(restClient, oauthClient, properties);

        expectToken(server);
        server.expect(requestTo(
                        "http://inter.test/banking/v2/extrato?dataInicio=2026-09-24&dataFim=2026-09-24"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-1"))
                .andExpect(header("x-conta-corrente", "123456"))
                .andRespond(withSuccess("""
                        {
                          "transacoes": [
                            {
                              "dataEntrada": "2026-09-24T12:00:00Z",
                              "tipoTransacao": "PIX",
                              "tipoOperacao": "C",
                              "valor": "100.00",
                              "titulo": "PIX recebido",
                              "descricao": "Cliente de estudo"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<com.hallancosta.inter.model.InterTransaction> transactions = client.statement(
                Instant.parse("2026-09-24T00:00:00Z"),
                Instant.parse("2026-09-24T23:59:59Z"));

        assertEquals(1, transactions.size());
        assertEquals("123456", transactions.getFirst().accountId());
        assertEquals(ExternalTransactionDirection.CREDIT, transactions.getFirst().direction());
        assertEquals("100.00", transactions.getFirst().amount().amount().toPlainString());
        assertEquals("Cliente de estudo", transactions.getFirst().description());
        server.verify();
    }

    private static void expectToken(MockRestServiceServer server) {
        server.expect(requestTo("http://inter.test/oauth/v2/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                        MediaType.APPLICATION_JSON));
    }

    private static InterOAuthClient oauthClient(RestClient restClient, InterProperties properties) {
        return new InterOAuthClient(
                restClient,
                properties,
                Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC));
    }

    private static InterProperties properties() {
        InterProperties properties = new InterProperties();
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setAccountId("123456");
        properties.setScope("extrato.read");
        return properties;
    }
}
