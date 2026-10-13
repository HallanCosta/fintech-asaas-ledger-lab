package com.hallancosta.asaas.security;

import com.hallancosta.ServerApplication;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AsaasStartupTest {

    private static final String EXCLUDES = String.join(",",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
            "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
            "org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration");

    @ParameterizedTest
    @ValueSource(strings = {"PRODUCTION", "SANDBOX"})
    void recusaSubirQuandoIntegracaoAtivaSemSegredoInterno(String environment) {
        Throwable thrown = runExpectingFailure(
                "--asaas.enabled=true",
                "--asaas.environment=" + environment,
                "--asaas.base-url=http://asaas.test",
                "--asaas.api-key=fake-asaas-key",
                "--asaas.internal-api-key=");

        assertTrue(chainContains(thrown, AsaasInternalApiKey.MISSING_MESSAGE), describe(thrown));
    }

    @Test
    void recusaSubirQuandoSegredoInternoRepeteAChaveDoAsaas() {
        Throwable thrown = runExpectingFailure(
                "--asaas.enabled=true",
                "--asaas.environment=PRODUCTION",
                "--asaas.base-url=http://asaas.test",
                "--asaas.api-key=fake-asaas-key",
                "--asaas.internal-api-key=fake-asaas-key");

        assertTrue(chainContains(thrown, AsaasInternalApiKey.SAME_AS_PROVIDER_MESSAGE), describe(thrown));
    }

    @Test
    void sobeSemFiltroQuandoIntegracaoEstaDesligada() {
        try (ConfigurableApplicationContext context = run("--asaas.enabled=false")) {
            assertTrue(context.getBeansOfType(AsaasApiKeyFilter.class).isEmpty());
        }
    }

    private static ConfigurableApplicationContext run(String... args) {
        SpringApplication application = new SpringApplication(ServerApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        List<String> all = new ArrayList<>();
        all.add("--spring.main.banner-mode=off");
        all.add("--spring.autoconfigure.exclude=" + EXCLUDES);
        all.addAll(List.of(args));
        return application.run(all.toArray(String[]::new));
    }

    private static Throwable runExpectingFailure(String... args) {
        try {
            try (ConfigurableApplicationContext context = run(args)) {
                throw new AssertionError("A aplicação subiu sem o segredo interno");
            }
        } catch (AssertionError assertionError) {
            throw assertionError;
        } catch (Throwable thrown) {
            return thrown;
        }
    }

    private static boolean chainContains(Throwable thrown, String message) {
        Throwable current = thrown;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String describe(Throwable thrown) {
        StringBuilder chain = new StringBuilder();
        Throwable current = thrown;
        while (current != null) {
            chain.append(current.getClass().getName()).append(": ").append(current.getMessage()).append('\n');
            current = current.getCause();
        }
        return chain.toString();
    }
}
