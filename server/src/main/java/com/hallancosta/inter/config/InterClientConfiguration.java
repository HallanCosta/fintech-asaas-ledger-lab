package com.hallancosta.inter.config;

import com.hallancosta.inter.InterBankingClient;
import com.hallancosta.inter.auth.InterOAuthClient;
import com.hallancosta.inter.pix.InterPixClient;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Clock;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(InterProperties.class)
public class InterClientConfiguration {

    @Bean
    Clock interClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnProperty(prefix = "inter", name = "enabled", havingValue = "true")
    RestClient interRestClient(RestClient.Builder builder, InterProperties properties) {
        return builder
                .baseUrl(required(properties.getBaseUrl(), "inter.base-url"))
                .requestFactory(new JdkClientHttpRequestFactory(buildHttpClient(properties)))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "inter", name = "enabled", havingValue = "true")
    InterOAuthClient interOAuthClient(RestClient interRestClient, InterProperties properties, Clock interClock) {
        return new InterOAuthClient(interRestClient, properties, interClock);
    }

    @Bean
    @ConditionalOnProperty(prefix = "inter", name = "enabled", havingValue = "true")
    InterBankingClient interBankingGateway(
            RestClient interRestClient,
            InterOAuthClient oauthClient,
            InterProperties properties) {
        return new InterBankingClient(interRestClient, oauthClient, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "inter", name = "enabled", havingValue = "true")
    InterPixClient interPixClient(
            RestClient interRestClient,
            InterOAuthClient oauthClient,
            InterProperties properties) {
        return new InterPixClient(interRestClient, oauthClient, properties);
    }

    private static HttpClient buildHttpClient(InterProperties properties) {
        String certificatePath = required(properties.getCertificatePath(), "inter.certificate-path");
        String certificatePassword = required(
                properties.getCertificatePassword(), "inter.certificate-password");

        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream input = Files.newInputStream(Path.of(certificatePath))) {
                keyStore.load(input, certificatePassword.toCharArray());
            }

            KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keyStore, certificatePassword.toCharArray());

            TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagers.init((KeyStore) null);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);

            return HttpClient.newBuilder().sslContext(sslContext).build();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar o certificado PKCS12 do Inter em " + certificatePath,
                    exception);
        }
    }

    private static String required(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configure a propriedade " + propertyName
                    + " para habilitar a integração com o Inter");
        }
        return value;
    }
}
