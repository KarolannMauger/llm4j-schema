package io.llm4j.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a JSON Schema object from a Java Record class annotated with {@link LLMSchema}.
 *
 * <p>Only Java Records are supported in v0.1.x. Support for plain classes is on the roadmap.
 */
public class SchemaGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Generates a JSON Schema string for the given Record class.
     *
     * @param clazz a Java Record class, optionally annotated with {@link LLMSchema}
     * @return JSON Schema as a string
     * @throws IllegalArgumentException if {@code clazz} is not a Record
     */
    public String generate(Class<?> clazz) {
        if (!clazz.isRecord()) {
            throw new IllegalArgumentException(
                "llm4j-schema v0.1 only supports Java Records. Got: " + clazz.getName()
            );
        }

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        LLMSchema annotation = clazz.getAnnotation(LLMSchema.class);
        if (annotation != null && !annotation.description().isEmpty()) {
            schema.put("description", annotation.description());
        }

        ObjectNode properties = mapper.createObjectNode();
        List<String> required = new ArrayList<>();

        for (RecordComponent component : clazz.getRecordComponents()) {
            ObjectNode field = mapper.createObjectNode();
            field.put("type", mapType(component.getType()));

            FieldDescription desc = component.getAnnotation(FieldDescription.class);
            if (desc != null) {
                field.put("description", desc.value());
            }

            properties.set(component.getName(), field);

            // All record components are required by definition
            required.add(component.getName());
        }

        schema.set("properties", properties);

        ArrayNode requiredNode = schema.putArray("required");
        required.forEach(requiredNode::add);

        return schema.toPrettyString();
    }

    private String mapType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "integer";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        if (type == Double.class || type == double.class) return "number";
        if (type == Float.class || type == float.class) return "number";
        if (type == java.util.List.class) return "array";
        // Default — LLM can usually figure it out from context
        return "string";
    }
}
