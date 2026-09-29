package com.hallancosta.inter;

import com.hallancosta.inter.auth.InterOAuthClient;
import com.hallancosta.inter.config.InterProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
import static org.springframework.http.HttpMethod.POST;

class InterOAuthClientTest {

    @Test
    void reutilizaTokenEnquantoEleAindaEstaValido() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://inter.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        InterProperties properties = properties();
        InterOAuthClient client = new InterOAuthClient(
                restClient,
                properties,
                Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC));

        server.expect(requestTo("http://inter.test/oauth/v2/token"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ="))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                        MediaType.APPLICATION_JSON));

        assertEquals("token-1", client.accessToken());
        assertEquals("token-1", client.accessToken());

        server.verify();
    }

    private static InterProperties properties() {
        InterProperties properties = new InterProperties();
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setScope("extrato.read");
        return properties;
    }
}
