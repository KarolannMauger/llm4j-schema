package io.llm4j.examples;

import io.llm4j.schema.FieldDescription;
import io.llm4j.schema.LLMSchema;

@LLMSchema(description = "A product review extracted from user-written text")
public record ProductReview(
    String productName,
    @FieldDescription("Rating from 1 (terrible) to 5 (excellent)") int rating,
    String summary,
    boolean recommended
) {}
