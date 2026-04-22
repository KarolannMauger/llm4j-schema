package io.llm4j.schema;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The main entry point for structured LLM extraction.
 *
 * <p>Given a Java Record class annotated with {@link LLMSchema}, {@code LLMExtractor}:
 * <ol>
 *   <li>Generates the JSON Schema for the target class</li>
 *   <li>Sends it as a system prompt to the LLM</li>
 *   <li>Deserializes the LLM response into a typed Java object</li>
 *   <li>Retries automatically on parse failure (up to {@code maxRetries} attempts)</li>
 * </ol>
 *
 * <p>Example:
 * <pre>{@code
 * LLMClient client = new OpenAIClient(System.getenv("OPENAI_API_KEY"));
 * LLMExtractor extractor = new LLMExtractor(client);
 *
 * ProductReview review = extractor.extract(ProductReview.class,
 *     "I bought an iPhone last week. Really great phone, 4/5.");
 *
 * System.out.println(review.productName()); // "iPhone"
 * System.out.println(review.rating());      // 4
 * }</pre>
 */
public class LLMExtractor {

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final LLMClient client;
    private final SchemaGenerator schemaGenerator;
    private final ObjectMapper mapper;
    private final int maxRetries;

    public LLMExtractor(LLMClient client) {
        this(client, DEFAULT_MAX_RETRIES);
    }

    public LLMExtractor(LLMClient client, int maxRetries) {
        if (client == null) throw new IllegalArgumentException("LLMClient must not be null");
        if (maxRetries < 1) throw new IllegalArgumentException("maxRetries must be >= 1");
        this.client = client;
        this.schemaGenerator = new SchemaGenerator();
        this.mapper = new ObjectMapper();
        this.maxRetries = maxRetries;
    }

    /**
     * Extracts a typed Java object from the given text.
     *
     * @param targetClass the Record class to extract into
     * @param userInput   the raw text to extract data from
     * @param <T>         the target type
     * @return a populated instance of {@code targetClass}
     * @throws LLMExtractionException if extraction fails after all retries
     */
    public <T> T extract(Class<T> targetClass, String userInput) {
        String schema = schemaGenerator.generate(targetClass);

        LLMSchema annotation = targetClass.getAnnotation(LLMSchema.class);
        String schemaDesc = (annotation != null && !annotation.description().isEmpty())
            ? annotation.description()
            : "a structured data object";

        String systemPrompt = buildSystemPrompt(schema, schemaDesc);

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String response = client.complete(systemPrompt, userInput);

                if (response == null || response.isBlank()) {
                    throw new LLMExtractionException("LLM returned an empty response");
                }

                // Strip potential markdown code fences (```json ... ```)
                String cleaned = stripMarkdownFences(response);

                return mapper.readValue(cleaned, targetClass);

            } catch (LLMExtractionException e) {
                throw e; // API-level errors — no point retrying
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    // Log retry (simple stderr — no logging dependency in core)
                    System.err.printf(
                        "[llm4j-schema] Parse attempt %d/%d failed: %s. Retrying...%n",
                        attempt, maxRetries, e.getMessage()
                    );
                }
            }
        }

        throw new LLMExtractionException(
            "Failed to extract " + targetClass.getSimpleName()
                + " after " + maxRetries + " attempts",
            lastException
        );
    }

    private String buildSystemPrompt(String schema, String schemaDescription) {
        return """
            You are a precise data extraction engine. Your only job is to extract \
            structured data from user text.

            Extract: %s

            Respond with ONLY a valid JSON object matching this schema exactly:
            %s

            Rules:
            - Output raw JSON only. No markdown, no explanation, no code fences.
            - All fields in the schema are required.
            - If a value cannot be determined from the text, use a reasonable default \
            (empty string for text, 0 for numbers, false for booleans).
            """.formatted(schemaDescription, schema);
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).strip();
            }
        }
        return trimmed;
    }
}
