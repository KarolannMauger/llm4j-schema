package io.llm4j.examples;

import io.llm4j.schema.LLMExtractor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleRunner {

    public static void main(String[] args) {
        SpringApplication.run(ExampleRunner.class, args);
    }

    @Bean
    CommandLineRunner run(LLMExtractor extractor) {
        return args -> {
            String userText = """
                I bought the Sony WH-1000XM5 headphones last month.
                The noise cancellation is absolutely incredible, best I've ever used.
                Battery life is great too, lasts about 30 hours.
                Only minor downside is they're a bit pricey.
                Overall I'd give them a solid 4 out of 5 and would definitely recommend them.
                """;

            System.out.println("Input: " + userText);
            System.out.println("Extracting...");

            ProductReview review = extractor.extract(ProductReview.class, userText);

            System.out.println("\n--- Result ---");
            System.out.println("Product:     " + review.productName());
            System.out.println("Rating:      " + review.rating() + "/5");
            System.out.println("Summary:     " + review.summary());
            System.out.println("Recommended: " + review.recommended());
        };
    }
}
