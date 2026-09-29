package com.hallancosta.inter;

import com.hallancosta.inter.auth.InterOAuthClient;
import com.hallancosta.inter.config.InterProperties;
import com.hallancosta.inter.pix.InterPixClient;
import com.hallancosta.inter.pix.model.ImmediatePixChargeRequest;
import com.hallancosta.inter.pix.model.PixCalendar;
import com.hallancosta.inter.pix.model.PixValue;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class InterPixClientTest {

    @Test
    void criaCobrancaPixImediata() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://inter.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        InterProperties properties = properties();
        InterOAuthClient oauthClient = new InterOAuthClient(
                restClient,
                properties,
                Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC));
        InterPixClient client = new InterPixClient(restClient, oauthClient, properties);

        server.expect(requestTo("http://inter.test/oauth/v2/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://inter.test/pix/v2/cob"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-1"))
                .andExpect(content().json("""
                        {
                          "calendario": {"expiracao": 3600},
                          "valor": {"original": "10.00"},
                          "chave": "pix@example.com",
                          "solicitacaoPagador": "Estudo Java"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "txid": "TXID-001",
                          "status": "ATIVA",
                          "pixCopiaECola": "000201...",
                          "location": "https://inter.test/cob/TXID-001",
                          "valor": {"original": "10.00"}
                        }
                        """, MediaType.APPLICATION_JSON));

        var response = client.createImmediateCharge(new ImmediatePixChargeRequest(
                null,
                new PixCalendar(3600),
                null,
                new PixValue("10.00"),
                "pix@example.com",
                "Estudo Java"));

        assertEquals("TXID-001", response.txid());
        assertEquals("ATIVA", response.status());
        assertEquals("000201...", response.pixCopyAndPaste());
        server.verify();
    }

    private static InterProperties properties() {
        InterProperties properties = new InterProperties();
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setAccountId("123456");
        properties.setScope("cob.write");
        return properties;
    }
}
