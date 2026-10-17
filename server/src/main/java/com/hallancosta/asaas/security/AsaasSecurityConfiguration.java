package com.hallancosta.asaas.security;

import com.hallancosta.asaas.config.AsaasProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AsaasProperties.class)
public class AsaasSecurityConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "asaas", name = "enabled", havingValue = "true")
    AsaasApiKeyFilter asaasApiKeyFilter(AsaasProperties properties) {
        return new AsaasApiKeyFilter(properties.getInternalApiKey());
    }
}
