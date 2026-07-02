package com.smartrental.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
@Slf4j
public class RateLimitingFilter implements Filter {

    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${app.rate-limit.max-requests:100}") int maxRequests,
            @Value("${app.rate-limit.window-seconds:60}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMillis = TimeUnit.SECONDS.toMillis(windowSeconds);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();
        if (path.contains("/health") || path.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        ClientBucket bucket = buckets.computeIfAbsent(clientIp, k -> new ClientBucket());

        synchronized (bucket) {
            long now = System.currentTimeMillis();
            if (now - bucket.windowStart > windowMillis) {
                bucket.windowStart = now;
                bucket.count = 0;
            }
            bucket.count++;
            if (bucket.count > maxRequests) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\",\"status\":429}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class ClientBucket {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}
