package com.hrd.asset_holder_api.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Implements rate limiting for API endpoints to prevent DoS attacks.
 */
@Slf4j
@Configuration
@EnableCaching
public class RateLimitConfig {

    // Rate limit buckets per IP address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Creates a rate limit interceptor for asset operations.
     * Limits: 100 requests per minute per IP address
     */
    @Bean(name = "rateLimitInterceptor")
    public HandlerInterceptor rateLimitInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                // Skip rate limiting for public endpoints
                String path = request.getRequestURI();
                if (path.startsWith("/rest/auth/") || 
                    path.startsWith("/v3/api-docs") || 
                    path.startsWith("/swagger-ui")) {
                    return true;
                }

                String clientId = getClientId(request);
                Bucket bucket = resolveBucket(clientId);

                if (bucket.tryConsume(1)) {
                    return true;
                } else {
                    log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
                    return false;
                }
            }

            private String getClientId(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }

            private Bucket resolveBucket(String clientId) {
                return cache.computeIfAbsent(clientId, key -> {
                    log.debug("Creating rate limit bucket for client: {}", key);
                    return createNewBucket();
                });
            }

            private Bucket createNewBucket() {
                // 100 requests per minute
                Refill refill = Refill.intervally(100, Duration.ofMinutes(1));
                Bandwidth limit = Bandwidth.classic(100, refill);
                return Bucket.builder()
                        .addLimit(limit)
                        .build();
            }
        };
    }
}
