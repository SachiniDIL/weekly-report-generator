package com.weeklyreport.backend.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * The {@link RestClient} for Google's Generative Language API. Kept as a bean so the base URL,
 * API-key header, and timeouts live here rather than inside
 * {@link com.weeklyreport.backend.service.GeminiService}.
 */
@Configuration
public class GeminiConfig {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    RestClient geminiRestClient(
            @Value("${gemini.api-key}") String apiKey,
            // Current Gemini "flash" models reason before answering and a real prompt can take
            // 20-40s; the read timeout has to clear that or every call fails.
            @Value("${gemini.timeout-seconds:60}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        return RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("x-goog-api-key", apiKey)
                .requestFactory(requestFactory)
                .build();
    }
}
