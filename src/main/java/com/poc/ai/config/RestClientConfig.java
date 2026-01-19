package com.poc.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient githubRestClient(@Value("${github.baseUrl}") String githubBaseUrl) {
        return RestClient.builder()
                .baseUrl(githubBaseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "Spring-AI-Agent-PoC")
                .build();
    }
}
