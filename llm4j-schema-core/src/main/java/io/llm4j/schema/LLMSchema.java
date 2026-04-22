package io.llm4j.schema;

import java.lang.annotation.*;

/**
 * Marks a Java Record as an LLM-extractable schema.
 *
 * <p>Usage:
 * <pre>{@code
 * @LLMSchema(description = "A product review extracted from user text")
 * public record ProductReview(
 *     String productName,
 *     int rating,
 *     String summary
 * ) {}
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LLMSchema {

    /**
     * Human-readable description of this schema, included in the LLM system prompt
     * to improve extraction accuracy.
     */
    String description() default "";
}
