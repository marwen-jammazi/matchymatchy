package com.moviematch.backend.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RateLimitService {

    private static final String KEY_PREFIX = "rate-limit:";

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();

        RATE_LIMIT_SCRIPT.setScriptText("""
                local current = redis.call('INCR', KEYS[1])

                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end

                return current
                """);

        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;
    private final Duration window;

    public RateLimitService(
            StringRedisTemplate redisTemplate,
            RateLimitProperties properties) {

        if (properties.getWindowSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "Rate-limit window must be greater than zero"
            );
        }

        this.redisTemplate = redisTemplate;
        this.window = Duration.ofSeconds(
                properties.getWindowSeconds()
        );
    }

    public boolean isAllowed(String clientId, int limit) {

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException(
                    "Client ID must not be empty"
            );
        }

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Rate limit must be greater than zero"
            );
        }

        String key = KEY_PREFIX + clientId;

        Long requestCount = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(window.toSeconds())
        );

        if (requestCount == null) {
            throw new IllegalStateException(
                    "Redis did not return a rate-limit counter"
            );
        }

        return requestCount <= limit;
    }
}