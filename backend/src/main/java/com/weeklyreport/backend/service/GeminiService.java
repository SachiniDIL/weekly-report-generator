package com.weeklyreport.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Calls Gemini's {@code generateContent} endpoint with a single prompt. Any transport failure or
 * unusable response is turned into a plain "temporarily unavailable" string — callers never see
 * an exception or a stack trace.
 */
@Service
public class GeminiService {

    static final String UNAVAILABLE_MESSAGE =
            "The AI assistant is temporarily unavailable. Please try again in a moment.";

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final RestClient geminiRestClient;
    private final String model;

    public GeminiService(RestClient geminiRestClient, @Value("${gemini.model}") String model) {
        this.geminiRestClient = geminiRestClient;
        this.model = model;
    }

    public String generate(String prompt) {
        try {
            GenerateContentResponse response = geminiRestClient
                    .post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(GenerateContentRequest.of(prompt))
                    .retrieve()
                    .body(GenerateContentResponse.class);

            return firstText(response).orElseGet(() -> {
                log.warn("Gemini returned no usable text: {}", response);
                return UNAVAILABLE_MESSAGE;
            });
        } catch (RestClientResponseException e) {
            log.error("Gemini call failed with HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return UNAVAILABLE_MESSAGE;
        } catch (RestClientException e) {
            log.error("Gemini call failed", e);
            return UNAVAILABLE_MESSAGE;
        }
    }

    private static Optional<String> firstText(GenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return Optional.empty();
        }
        Content content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            return Optional.empty();
        }
        String text = content.parts().get(0).text();
        return text == null || text.isBlank() ? Optional.empty() : Optional.of(text);
    }

    record GenerateContentRequest(List<Content> contents, GenerationConfig generationConfig) {
        static GenerateContentRequest of(String prompt) {
            return new GenerateContentRequest(
                    List.of(new Content(List.of(new Part(prompt)))),
                    // Keep reasoning shallow — a reporting assistant doesn't need deep chains of
                    // thought, and "low" cuts response time from ~30-40s to a few seconds.
                    new GenerationConfig(new ThinkingConfig("LOW")));
        }
    }

    record GenerationConfig(ThinkingConfig thinkingConfig) {}

    record ThinkingConfig(String thinkingLevel) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GenerateContentResponse(List<Candidate> candidates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {}
}
