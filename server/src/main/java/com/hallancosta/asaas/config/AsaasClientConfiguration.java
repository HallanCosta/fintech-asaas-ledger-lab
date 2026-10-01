package com.hallancosta.asaas.config;

import com.hallancosta.asaas.AsaasClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AsaasProperties.class)
public class AsaasClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "asaas", name = "enabled", havingValue = "true")
    RestClient asaasRestClient(AsaasProperties properties) {
        return RestClient.builder()
                .baseUrl(required(properties.getBaseUrl(), "asaas.base-url"))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "asaas", name = "enabled", havingValue = "true")
    AsaasClient asaasClient(RestClient asaasRestClient, AsaasProperties properties) {
        return new AsaasClient(asaasRestClient, properties);
    }

    private static String required(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configure a propriedade " + propertyName
                    + " para habilitar a integração com o Asaas");
        }
        return value;
    }
}
