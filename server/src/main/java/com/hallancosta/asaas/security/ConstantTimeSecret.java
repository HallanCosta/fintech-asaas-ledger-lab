package com.hallancosta.asaas.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Compara segredos sem curto-circuitar no conteúdo.
 * Os dois valores viram SHA-256, do mesmo tamanho, e {@code MessageDigest.isEqual}
 * compara esses resumos em tempo constante.
 */
public final class ConstantTimeSecret {

    private ConstantTimeSecret() {
    }

    public static boolean matches(String expected, String provided) {
        if (expected == null || expected.isEmpty()) {
            return false;
        }
        byte[] expectedDigest = digest(expected);
        byte[] providedDigest = digest(provided == null ? "" : provided);
        return MessageDigest.isEqual(expectedDigest, providedDigest);
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível", exception);
        }
    }
}
