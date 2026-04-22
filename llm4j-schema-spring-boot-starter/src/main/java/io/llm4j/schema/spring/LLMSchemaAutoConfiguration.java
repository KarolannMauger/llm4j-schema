package io.llm4j.schema.spring;

import io.llm4j.schema.AnthropicClient;
import io.llm4j.schema.LLMClient;
import io.llm4j.schema.LLMExtractor;
import io.llm4j.schema.OpenAIClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for llm4j-schema.
 *
 * <p>Automatically creates an {@link LLMExtractor} bean based on {@code application.yml} config.
 * Override by declaring your own {@link LLMExtractor} or {@link LLMClient} bean.
 */
@AutoConfiguration
@ConditionalOnClass(LLMExtractor.class)
@EnableConfigurationProperties(LLMSchemaProperties.class)
public class LLMSchemaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLMClient llmClient(LLMSchemaProperties props) {
        String provider = props.getProvider();
        String apiKey = props.getApiKey();

        return switch (provider.toLowerCase()) {
            case "anthropic" -> props.getModel() != null
                ? new AnthropicClient(apiKey, props.getModel())
                : new AnthropicClient(apiKey);
            case "openai" -> props.getModel() != null
                ? new OpenAIClient(apiKey, props.getModel())
                : new OpenAIClient(apiKey);
            default -> throw new IllegalArgumentException(
                "Unknown llm4j provider: '" + provider + "'. Supported: openai, anthropic"
            );
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public LLMExtractor llmExtractor(LLMClient client, LLMSchemaProperties props) {
        return new LLMExtractor(client, props.getMaxRetries());
    }
}
