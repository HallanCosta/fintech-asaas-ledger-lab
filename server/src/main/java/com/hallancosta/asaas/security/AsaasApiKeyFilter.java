package com.hallancosta.asaas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/** Exige o header X-Api-Key em /api/asaas. Health e actuator permanecem públicos. */
public class AsaasApiKeyFilter extends OncePerRequestFilter implements Ordered {

    public static final String HEADER_NAME = "X-Api-Key";

    private static final byte[] UNAUTHORIZED_BODY = """
            {"status":401,"error":"Unauthorized","message":"Credencial ausente ou inválida. Envie o header X-Api-Key."}\
            """.getBytes(StandardCharsets.UTF_8);

    private final String expectedKey;

    public AsaasApiKeyFilter(String expectedKey) {
        if (expectedKey == null || expectedKey.isBlank()) {
            throw new IllegalArgumentException("O segredo interno não pode ser vazio");
        }
        this.expectedKey = expectedKey;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !protects(requestPath(request));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER_NAME);
        if (!ConstantTimeSecret.matches(expectedKey, provided == null ? "" : provided)) {
            writeUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    static boolean protects(String path) {
        return "/api/asaas".equals(path) || (path != null && path.startsWith("/api/asaas/"));
    }

    static String requestPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (request.getPathInfo() != null) {
            path = (path == null ? "" : path) + request.getPathInfo();
        }
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (path != null && contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
        }
        return path;
    }

    private static void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey realm=\"asaas\"");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType("application/json;charset=UTF-8");
        response.setContentLength(UNAUTHORIZED_BODY.length);
        response.getOutputStream().write(UNAUTHORIZED_BODY);
        response.flushBuffer();
    }
}
