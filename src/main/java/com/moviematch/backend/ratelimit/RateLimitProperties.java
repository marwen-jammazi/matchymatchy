package com.moviematch.backend.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private int loginLimit = 5;
    private int registrationLimit = 5;
    private int defaultLimit = 100;
    private long windowSeconds = 60;

    public int getLoginLimit() {
        return loginLimit;
    }

    public void setLoginLimit(int loginLimit) {
        this.loginLimit = loginLimit;
    }

    public int getRegistrationLimit() {
        return registrationLimit;
    }

    public void setRegistrationLimit(int registrationLimit) {
        this.registrationLimit = registrationLimit;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}