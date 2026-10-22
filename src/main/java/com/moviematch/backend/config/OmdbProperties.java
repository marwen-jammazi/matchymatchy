package com.moviematch.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OmdbProperties {

    @Value("${omdb.api.base-url}")
    private String baseUrl;

    @Value("${omdb.api.key}")
    private String apiKey;

    public String getBaseUrl() { return baseUrl; }

    public String getApiKey() { return apiKey; }
}