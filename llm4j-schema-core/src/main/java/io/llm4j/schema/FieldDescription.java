package io.llm4j.schema;

import java.lang.annotation.*;

/**
 * Adds a description to a Record component for richer schema context.
 *
 * <p>Usage:
 * <pre>{@code
 * public record ProductReview(
 *     String productName,
 *     @FieldDescription("Rating from 1 (worst) to 5 (best)") int rating,
 *     String summary
 * ) {}
 * }</pre>
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldDescription {
    String value();
}
