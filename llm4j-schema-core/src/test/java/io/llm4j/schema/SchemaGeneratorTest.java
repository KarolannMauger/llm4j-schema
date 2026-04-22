package io.llm4j.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaGeneratorTest {

    private SchemaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SchemaGenerator();
    }

    @LLMSchema(description = "A product review")
    record ProductReview(String productName, int rating, String summary) {}

    record SimpleRecord(String name, boolean active, double score) {}

    @Test
    void shouldGenerateSchemaForAnnotatedRecord() {
        String schema = generator.generate(ProductReview.class);

        assertNotNull(schema);
        assertTrue(schema.contains("\"type\" : \"object\""));
        assertTrue(schema.contains("\"productName\""));
        assertTrue(schema.contains("\"rating\""));
        assertTrue(schema.contains("\"summary\""));
        assertTrue(schema.contains("\"description\" : \"A product review\""));
    }

    @Test
    void shouldMapJavaTypesToJsonSchemaTypes() {
        String schema = generator.generate(SimpleRecord.class);

        assertTrue(schema.contains("\"string\""));
        assertTrue(schema.contains("\"boolean\""));
        assertTrue(schema.contains("\"number\""));
    }

    @Test
    void shouldIncludeRequiredArray() {
        String schema = generator.generate(ProductReview.class);

        assertTrue(schema.contains("\"required\""));
        assertTrue(schema.contains("\"productName\""));
    }

    @Test
    void shouldThrowForNonRecordClass() {
        class NotARecord {}
        assertThrows(IllegalArgumentException.class,
            () -> generator.generate(NotARecord.class));
    }

    @Test
    void shouldIncludeFieldDescriptions() {
        record ReviewWithDesc(
            String productName,
            @FieldDescription("Rating from 1 to 5") int rating
        ) {}

        String schema = generator.generate(ReviewWithDesc.class);
        assertTrue(schema.contains("Rating from 1 to 5"));
    }
}
