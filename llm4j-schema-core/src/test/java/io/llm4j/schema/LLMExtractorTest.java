package io.llm4j.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LLMExtractorTest {

    @LLMSchema(description = "A product review")
    record ProductReview(String productName, int rating, String summary) {}

    record PersonName(String firstName, String lastName) {}

    // ---------- Happy path ----------

    @Test
    void shouldExtractProductReview() {
        LLMClient mockClient = (sys, user) ->
            "{\"productName\":\"iPhone\",\"rating\":4,\"summary\":\"Great phone\"}";

        LLMExtractor extractor = new LLMExtractor(mockClient);
        ProductReview review = extractor.extract(ProductReview.class,
            "I bought an iPhone last week, really great phone, 4/5");

        assertEquals("iPhone", review.productName());
        assertEquals(4, review.rating());
        assertEquals("Great phone", review.summary());
    }

    @Test
    void shouldExtractPersonName() {
        LLMClient mockClient = (sys, user) ->
            "{\"firstName\":\"Marie\",\"lastName\":\"Dupont\"}";

        LLMExtractor extractor = new LLMExtractor(mockClient);
        PersonName person = extractor.extract(PersonName.class, "My name is Marie Dupont");

        assertEquals("Marie", person.firstName());
        assertEquals("Dupont", person.lastName());
    }

    // ---------- Retry logic ----------

    @Test
    void shouldRetryOnParseFailureThenSucceed() {
        // First call returns garbage, second returns valid JSON
        int[] callCount = {0};
        LLMClient mockClient = (sys, user) -> {
            callCount[0]++;
            return callCount[0] == 1
                ? "This is not JSON at all!"
                : "{\"productName\":\"Sony TV\",\"rating\":5,\"summary\":\"Amazing\"}";
        };

        LLMExtractor extractor = new LLMExtractor(mockClient, 3);
        ProductReview review = extractor.extract(ProductReview.class, "Great Sony TV, 5 stars");

        assertEquals(2, callCount[0]);
        assertEquals("Sony TV", review.productName());
    }

    @Test
    void shouldThrowAfterExhaustedRetries() {
        LLMClient alwaysBadClient = (sys, user) -> "not json";

        LLMExtractor extractor = new LLMExtractor(alwaysBadClient, 2);

        assertThrows(LLMExtractionException.class,
            () -> extractor.extract(ProductReview.class, "some input"));
    }

    // ---------- Markdown fence stripping ----------

    @Test
    void shouldHandleMarkdownCodeFences() {
        LLMClient mockClient = (sys, user) -> """
            ```json
            {"productName":"MacBook","rating":5,"summary":"Best laptop ever"}
            ```
            """;

        LLMExtractor extractor = new LLMExtractor(mockClient);
        ProductReview review = extractor.extract(ProductReview.class, "MacBook review");

        assertEquals("MacBook", review.productName());
    }

    // ---------- Guard clauses ----------

    @Test
    void shouldThrowOnNullClient() {
        assertThrows(IllegalArgumentException.class, () -> new LLMExtractor(null));
    }

    @Test
    void shouldThrowOnZeroRetries() {
        LLMClient mockClient = (sys, user) -> "{}";
        assertThrows(IllegalArgumentException.class, () -> new LLMExtractor(mockClient, 0));
    }
}
