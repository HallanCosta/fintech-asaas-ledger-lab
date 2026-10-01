package com.hallancosta.asaas;

import com.hallancosta.asaas.config.AsaasProperties;
import com.hallancosta.asaas.model.ExternalTransactionDirection;
import com.hallancosta.asaas.model.NormalizedTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AsaasClientTest {

    @Test
    void consultaSaldoUsandoChaveNoHeaderDoAsaas() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://asaas.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AsaasClient client = new AsaasClient(builder.build(), properties());

        server.expect(requestTo("http://asaas.test/finance/balance"))
                .andExpect(method(GET))
                .andExpect(header("access_token", "asaas-key"))
                .andRespond(withSuccess("{\"balance\":123.45}", MediaType.APPLICATION_JSON));

        assertEquals(new BigDecimal("123.45"), client.balance().balance());
        server.verify();
    }

    @Test
    void consultaExtratoEConverteRespostaDoAsaasParaModeloInterno() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://asaas.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AsaasClient client = new AsaasClient(builder.build(), properties());

        server.expect(requestTo(
                        "http://asaas.test/financialTransactions?startDate=2026-09-24&finishDate=2026-09-24&limit=100&offset=0"))
                .andExpect(method(GET))
                .andExpect(header("access_token", "asaas-key"))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "id": "ft_001",
                              "type": "PIX_TRANSACTION_CREDIT",
                              "value": 100.00,
                              "date": "2026-09-24",
                              "description": "Cliente de estudo"
                            },
                            {
                              "id": "ft_002",
                              "type": "TRANSFER",
                              "value": -25.50,
                              "date": "2026-09-24",
                              "description": "Transferência Pix"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<NormalizedTransaction> transactions = client.statement(
                Instant.parse("2026-09-24T00:00:00Z"),
                Instant.parse("2026-09-24T23:59:59Z"));

        assertEquals(2, transactions.size());
        assertEquals("asaas-account", transactions.getFirst().accountId());
        assertEquals(ExternalTransactionDirection.CREDIT, transactions.getFirst().direction());
        assertEquals("100.00", transactions.getFirst().amount().amount().toPlainString());
        assertEquals(ExternalTransactionDirection.DEBIT, transactions.get(1).direction());
        assertEquals("25.50", transactions.get(1).amount().amount().toPlainString());
        server.verify();
    }

    private static AsaasProperties properties() {
        AsaasProperties properties = new AsaasProperties();
        properties.setApiKey("asaas-key");
        return properties;
    }
}
