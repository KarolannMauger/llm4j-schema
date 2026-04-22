package io.llm4j.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * {@link LLMClient} implementation for the OpenAI Chat Completions API.
 *
 * <p>Uses Java 21's built-in {@link HttpClient} — no extra HTTP dependencies needed.
 *
 * <p>Example:
 * <pre>{@code
 * LLMClient client = new OpenAIClient(System.getenv("OPENAI_API_KEY"));
 * }</pre>
 */
public class OpenAIClient implements LLMClient {

    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public OpenAIClient(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public OpenAIClient(String apiKey, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OpenAI API key must not be blank");
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
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new LLMExtractionException(
                    "OpenAI API error " + response.statusCode() + ": " + response.body()
                );
            }

            JsonNode root = mapper.readTree(response.body());
            return root
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();

        } catch (LLMExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMExtractionException("HTTP request to OpenAI failed", e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        // Escape quotes in prompts to avoid breaking JSON
        String escapedSystem = systemPrompt.replace("\"", "\\\"").replace("\n", "\\n");
        String escapedUser = userMessage.replace("\"", "\\\"").replace("\n", "\\n");

        return """
            {
              "model": "%s",
              "messages": [
                {"role": "system", "content": "%s"},
                {"role": "user",   "content": "%s"}
              ],
              "response_format": {"type": "json_object"},
              "temperature": 0
            }
            """.formatted(model, escapedSystem, escapedUser);
    }
}
