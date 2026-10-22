package com.moviematch.backend.ratelimit;
import org.springframework.http.HttpStatus;
import com.moviematch.backend.audit.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final AuditService auditService;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitProperties properties,
            AuditService auditService) {

        this.rateLimitService = rateLimitService;
        this.properties = properties;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Rate-limit API endpoints only.
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();

        String endpointGroup;
        int limit;

        // Login rate limit.
        if ("POST".equalsIgnoreCase(method)
                && "/api/v1/auth/login".equals(path)) {

            endpointGroup = "login";
            limit = properties.getLoginLimit();

        // Registration rate limit.
        } else if ("POST".equalsIgnoreCase(method)
                && "/api/v1/users".equals(path)) {

            endpointGroup = "register";
            limit = properties.getRegistrationLimit();

        // Default rate limit for other API endpoints.
        } else {

            endpointGroup = method.toLowerCase() + ":" + path;
            limit = properties.getDefaultLimit();
        }

        String identity = resolveIdentity(
                endpointGroup,
                clientIp
        );

        boolean allowed;

        try {
            allowed = rateLimitService.isAllowed(
                    identity,
                    limit
            );

        } catch (RedisConnectionFailureException
                 | RedisSystemException
                 | IllegalStateException exception) {

            writeServiceUnavailable(response);
            return;
        }

        // Reject requests that exceed their configured limit.
        if (!allowed) {

            auditService.logRateLimitExceeded(
                    identity,
                    path
            );

            response.setStatus(
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            );

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.setHeader(
                    "Retry-After",
                    String.valueOf(properties.getWindowSeconds())
            );

            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many requests. Please try again later.\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveIdentity(
            String endpointGroup,
            String clientIp) {

        // Use the client IP for login and registration limits.
        if ("login".equals(endpointGroup)
                || "register".equals(endpointGroup)) {

            return endpointGroup + ":" + clientIp;
        }

        // Use the authenticated user's identity where available.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !authentication.getName().equals("anonymousUser")) {

            return endpointGroup
                    + ":user:"
                    + authentication.getName();
        }

        // Unauthenticated requests use their client IP.
        return endpointGroup + ":ip:" + clientIp;
    }

    private void writeServiceUnavailable(
            HttpServletResponse response) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Retry-After", "30");

        response.getWriter().write(
                "{\"status\":503,\"message\":\"Rate limiting is temporarily unavailable. Please try again later.\"}"
        );
    }
}
