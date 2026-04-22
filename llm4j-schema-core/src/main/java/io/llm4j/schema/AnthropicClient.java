package io.llm4j.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * {@link LLMClient} implementation for the Anthropic Messages API (Claude models).
 *
 * <p>Example:
 * <pre>{@code
 * LLMClient client = new AnthropicClient(System.getenv("ANTHROPIC_API_KEY"));
 * }</pre>
 */
public class AnthropicClient implements LLMClient {

    private static final String DEFAULT_MODEL = "claude-haiku-4-5-20251001";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private final String apiKey;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public AnthropicClient(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public AnthropicClient(String apiKey, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Anthropic API key must not be blank");
        }
        this.apiKey = apiKey;
        this.model = model;
        this.http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        String body = buildRequestBody(systemPrompt, userMessage);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("x-api-key", apiKey)
            .header("anthropic-version", API_VERSION)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new LLMExtractionException(
                    "Anthropic API error " + response.statusCode() + ": " + response.body()
                );
            }

            JsonNode root = mapper.readTree(response.body());
            return root
                .path("content")
                .path(0)
                .path("text")
                .asText();

        } catch (LLMExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMExtractionException("HTTP request to Anthropic failed", e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        String escapedSystem = systemPrompt.replace("\"", "\\\"").replace("\n", "\\n");
        String escapedUser = userMessage.replace("\"", "\\\"").replace("\n", "\\n");

        return """
            {
              "model": "%s",
              "max_tokens": 1024,
              "system": "%s",
              "messages": [
                {"role": "user", "content": "%s"}
              ]
            }
            """.formatted(model, escapedSystem, escapedUser);
    }
}
