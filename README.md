# llm4j-schema

> Structured, type-safe LLM output for Java.
> Stop parsing raw strings — get real Java objects from any LLM.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.karolannmauger/llm4j-schema-core.svg)](https://central.sonatype.com/artifact/io.github.karolannmauger/llm4j-schema-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21%2B-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Build](https://img.shields.io/github/actions/workflow/status/KarolannMauger/llm4j-schema/ci.yml)](https://github.com/KarolannMauger/llm4j-schema/actions)
[![Ko-fi](https://img.shields.io/badge/Support-Ko--fi-ff5e5b?logo=ko-fi)](https://ko-fi.com/karolannmauger)

---

## The problem

```java
// Without llm4j-schema
String raw = llm.complete("Extract the product name and rating from: " + userText);
// Now what? JSON.parse? Hope the LLM didn't add markdown? Retry logic?
// Welcome to a world of pain.
```

## The solution

```java
@LLMSchema(description = "A product review")
public record ProductReview(
    String productName,
    @FieldDescription("Rating from 1 (worst) to 5 (best)") int rating,
    String summary,
    boolean recommended
) {}

// One line. Type-safe. Auto-retried on failure.
ProductReview review = extractor.extract(ProductReview.class, userText);

System.out.println(review.productName());  // "Sony WH-1000XM5"
System.out.println(review.rating());       // 4
System.out.println(review.recommended());  // true
```

---

## Quick Start

### Spring Boot (recommended)

**1. Add the dependency**

```xml
<dependency>
    <groupId>io.github.KarolannMauger</groupId>
    <artifactId>llm4j-schema-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

**2. Configure your API key**

```yaml
# application.yml
llm4j:
  provider: openai          # or: anthropic
  api-key: ${OPENAI_API_KEY}
```

**3. Inject and use**

```java
@Service
public class ReviewService {

    private final LLMExtractor extractor;

    public ReviewService(LLMExtractor extractor) {
        this.extractor = extractor;
    }

    public ProductReview analyze(String userText) {
        return extractor.extract(ProductReview.class, userText);
    }
}
```

### Without Spring Boot

```xml
<dependency>
    <groupId>io.github.KarolannMauger</groupId>
    <artifactId>llm4j-schema-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

```java
LLMClient client = new OpenAIClient(System.getenv("OPENAI_API_KEY"));
LLMExtractor extractor = new LLMExtractor(client);

ProductReview review = extractor.extract(ProductReview.class, userText);
```

---

## Supported Providers

| Provider | Client class | Default model |
|----------|-------------|---------------|
| OpenAI | `OpenAIClient` | `gpt-4o-mini` |
| Anthropic | `AnthropicClient` | `claude-haiku-4-5` |

Implement `LLMClient` to add any other provider (Ollama, Mistral, etc.).

---

## Why llm4j-schema?

| Feature | llm4j-schema | Manual parsing |
|---------|-------------|----------------|
| Type-safe Java objects | ✅ | ❌ |
| Auto-retry on parse failure | ✅ | ❌ |
| JSON Schema generation | ✅ | ❌ |
| Spring Boot starter | ✅ | ❌ |
| Multi-provider support | ✅ | ❌ |
| Zero extra dependencies* | ✅ | — |

*Core module only requires `jackson-databind`. HTTP uses Java 21's built-in `HttpClient`.

---

## Defining schemas

Use any Java 21 **Record** annotated with `@LLMSchema`:

```java
@LLMSchema(description = "Contact information extracted from text")
public record Contact(
    String firstName,
    String lastName,
    @FieldDescription("Email address if mentioned") String email,
    @FieldDescription("Phone number in E.164 format if mentioned") String phone
) {}
```

---

## Configuration reference

| Property | Default | Description |
|----------|---------|-------------|
| `llm4j.provider` | `openai` | LLM provider: `openai` or `anthropic` |
| `llm4j.api-key` | — | API key (required) |
| `llm4j.model` | provider default | Model name override |
| `llm4j.max-retries` | `3` | Retries on parse failure |

---

## Roadmap

- [ ] Nested object support
- [ ] `List<T>` field support
- [ ] `@NotNull` / `@Range` validation annotations
- [ ] Async extraction (`CompletableFuture<T>`)
- [ ] Ollama support (local LLMs)
- [ ] Streaming support

---

## Contributing

Contributions are welcome. Please open an issue first to discuss what you'd like to change.

## License

Apache License 2.0 — see [LICENSE](LICENSE).

---

*Built with Java 21. Inspired by [Instructor](https://github.com/jxnl/instructor) (Python).*
