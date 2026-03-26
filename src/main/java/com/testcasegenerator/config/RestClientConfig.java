package com.testcasegenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${anthropic.base-url}")
    private String anthropicBaseUrl;

    @Value("${anthropic.api-key}")
    private String apiKey;

    @Value("${anthropic.version}")
    private String anthropicVersion;

    @Bean
    public RestClient anthropicRestClient() {
        return RestClient.builder()
                .baseUrl(anthropicBaseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", anthropicVersion)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
