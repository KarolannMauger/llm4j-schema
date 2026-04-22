package io.llm4j.schema.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for llm4j-schema.
 *
 * <p>Example {@code application.yml}:
 * <pre>
 * llm4j:
 *   provider: openai        # openai | anthropic (default: openai)
 *   api-key: ${OPENAI_API_KEY}
 *   model: gpt-4o-mini      # optional override
 *   max-retries: 3          # optional (default: 3)
 * </pre>
 */
@ConfigurationProperties(prefix = "llm4j")
public class LLMSchemaProperties {

    /** LLM provider. Supported: {@code openai}, {@code anthropic}. */
    private String provider = "openai";

    /** API key for the selected provider. */
    private String apiKey;

    /** Model name override. Uses provider default if not set. */
    private String model;

    /** Maximum retries on parse failure. */
    private int maxRetries = 3;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
