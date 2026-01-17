package com.girikgarg.uberapigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authentication & Authorization Delegation Filter
 * 
 * Centralized security at API Gateway level:
 * 1. Extracts required role from route metadata
 * 2. Forwards Cookie header + required role to Auth Service /validate endpoint
 * 3. Auth Service validates token AND checks if user has the required role
 * 4. Only forwards request to downstream service if both authentication and authorization succeed
 * 
 * Benefits:
 * - Security at the edge: Invalid/unauthorized requests never reach downstream services
 * - Single point of control: All auth logic centralized
 * - Simpler downstream services: No auth code needed
 */
@Component
@Slf4j
public class AuthenticationDelegationFilter extends AbstractGatewayFilterFactory<AuthenticationDelegationFilter.Config> {

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private DiscoveryClient discoveryClient;

    public AuthenticationDelegationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Check for JwtToken cookie
            org.springframework.http.HttpCookie jwtCookie = request.getCookies().getFirst("JwtToken");
            if (jwtCookie == null) {
                log.warn("[AUTH] No JwtToken cookie in request to: {}", request.getPath().value());
                return onError(exchange, "Missing authentication credentials", HttpStatus.UNAUTHORIZED);
            }

            // Extract required role from route metadata
            org.springframework.cloud.gateway.route.Route route = exchange.getAttribute(
                org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
            );
            
            String extractedRole = null;
            if (route != null && route.getMetadata() != null) {
                Object roleObj = route.getMetadata().get("requiredRole");
                if (roleObj != null) {
                    extractedRole = roleObj.toString();
                }
            }
            
            if (extractedRole == null) {
                log.error("[AUTH] No required role in route metadata for: {}", request.getPath().value());
                return onError(exchange, "Route configuration error", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("[AUTH] Validating {} with required role: {}", request.getPath().value(), extractedRole);
            
            final String requiredRole = extractedRole; // Make effectively final for lambda
            return validateWithAuthService(request, requiredRole)
                    .onErrorResume(error -> {
                        // Handle authentication/authorization errors from Auth Service
                        if (error.getMessage() != null && error.getMessage().contains("403")) {
                            return Mono.error(new AuthorizationException("Forbidden: Insufficient permissions"));
                        }
                        return Mono.error(new AuthenticationException("Authentication failed"));
                    })
                    .flatMap(email -> {
                        if (email == null || email.isEmpty()) {
                            return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
                        }

                        log.info("[AUTH] Authorized: {} with role {} -> {}", email, requiredRole, request.getPath().value());

                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Email", email)
                                .header("X-User-Role", requiredRole)
                                .build();

                        // Forward to downstream service - let service unavailability errors propagate
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(AuthenticationException.class, error -> 
                        onError(exchange, error.getMessage(), HttpStatus.UNAUTHORIZED)
                    )
                    .onErrorResume(AuthorizationException.class, error -> 
                        onError(exchange, error.getMessage(), HttpStatus.FORBIDDEN)
                    );
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String errorJson = String.format(
            "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
            status.getReasonPhrase(), message, status.value()
        );
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
    }

    private Mono<String> validateWithAuthService(ServerHttpRequest request, String requiredRole) {
        List<ServiceInstance> instances = discoveryClient.getInstances("UBER-AUTH-SERVICE");
        
        if (instances.isEmpty()) {
            log.error("[AUTH] Auth Service not found in Eureka");
            return Mono.error(new RuntimeException("Auth Service unavailable"));
        }
        
        String authServiceUrl = instances.get(0).getUri() + "/api/v1/auth/validate";
        java.util.Map<String, String> requestBody = java.util.Map.of("requiredRole", requiredRole);
        
        // Forward Cookie header as-is to Auth Service (no manipulation)
        // Use WebClient.create() instead of builder to avoid load balancing issues
        return WebClient.create()
                .post()
                .uri(authServiceUrl)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    List<String> cookies = request.getHeaders().get(HttpHeaders.COOKIE);
                    if (cookies != null) {
                        headers.addAll(HttpHeaders.COOKIE, cookies);
                    }
                })
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() == 403, 
                         clientResponse -> Mono.error(new RuntimeException("403 Forbidden")))
                .bodyToMono(java.util.Map.class)
                .map(response -> {
                    Boolean valid = (Boolean) response.get("valid");
                    String email = (String) response.get("email");
                    return (valid != null && valid && email != null) ? email : null;
                })
                .onErrorResume(error -> {
                    log.error("[AUTH] Validation failed: {}", error.getMessage());
                    return Mono.error(error);
                });
    }

    public static class Config {
    }
    
    /**
     * Custom exception for authentication failures (401)
     */
    private static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
    
    /**
     * Custom exception for authorization failures (403)
     */
    private static class AuthorizationException extends RuntimeException {
        public AuthorizationException(String message) {
            super(message);
        }
    }
}
