package io.llm4j.schema;

/**
 * Thrown when the LLM fails to return a valid, parseable response after all retries.
 */
public class LLMExtractionException extends RuntimeException {

    public LLMExtractionException(String message) {
        super(message);
    }

    public LLMExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
