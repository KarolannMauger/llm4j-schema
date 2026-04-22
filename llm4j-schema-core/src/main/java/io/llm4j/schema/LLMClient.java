package io.llm4j.schema;

/**
 * Abstraction over any LLM provider.
 *
 * <p>Implement this interface to support any provider (OpenAI, Anthropic, Ollama, etc.).
 * Built-in implementations: {@link OpenAIClient}, {@link AnthropicClient}.
 */
@FunctionalInterface
public interface LLMClient {

    /**
     * Sends a prompt to the LLM and returns the raw text response.
     *
     * @param systemPrompt the system instruction (schema + formatting rules)
     * @param userMessage  the user input to extract data from
     * @return raw LLM response (expected to be valid JSON)
     */
    String complete(String systemPrompt, String userMessage);
}
