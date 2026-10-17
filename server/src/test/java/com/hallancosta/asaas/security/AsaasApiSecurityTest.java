package com.hallancosta.asaas.security;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hallancosta.asaas.AsaasClient;
import com.hallancosta.asaas.external.AsaasBalanceResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = {
        "asaas.enabled=true",
        "asaas.environment=PRODUCTION",
        "asaas.base-url=http://asaas.test",
        "asaas.api-key=fake-asaas-key",
        "asaas.internal-api-key=internal-test-key",
        "spring.main.banner-mode=off",
        "management.health.redis.enabled=false",
        "management.health.db.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration,org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration,org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration,org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration"
})
@AutoConfigureMockMvc
class AsaasApiSecurityTest {

    private static final String UNAUTHORIZED_MESSAGE =
            "Credencial ausente ou inválida. Envie o header X-Api-Key.";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsaasClient asaasClient;

    @BeforeEach
    void limpaInteracoes() {
        reset(asaasClient);
    }

    @Test
    void rotaProtegidaSemCredencialRetorna401() throws Exception {
        expectUnauthorized(mockMvc.perform(get("/api/asaas/balance")));
        expectUnauthorized(mockMvc.perform(get("/api/asaas/payments/pay_1/pix-qrcode")));
        expectUnauthorized(mockMvc.perform(post("/api/asaas/transfers/pix")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":10.00}")));

        verifyNoInteractions(asaasClient);
    }

    @Test
    void credencialErradaRetorna401() throws Exception {
        expectUnauthorized(mockMvc.perform(get("/api/asaas/balance").header("X-Api-Key", "wrong-key")));
        expectUnauthorized(mockMvc.perform(get("/api/asaas/balance").header("X-Api-Key", "internal-test-key ")));
        expectUnauthorized(mockMvc.perform(get("/api/asaas/balance").header("access_token", "internal-test-key")));

        verifyNoInteractions(asaasClient);
    }

    @Test
    void credencialCorretaConsultaOSaldo() throws Exception {
        when(asaasClient.balance()).thenReturn(new AsaasBalanceResponse(new BigDecimal("15.25")));

        mockMvc.perform(get("/api/asaas/balance").header("x-api-key", "internal-test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(15.25));

        verify(asaasClient).balance();
    }

    @Test
    void healthEActuatorContinuamPublicos() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        verifyNoInteractions(asaasClient);
    }

    private void expectUnauthorized(ResultActions actions) throws Exception {
        actions.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "ApiKey realm=\"asaas\""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED_MESSAGE));
    }
}
