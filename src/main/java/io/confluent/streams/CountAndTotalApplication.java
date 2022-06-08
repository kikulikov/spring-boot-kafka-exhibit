package io.confluent.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"io.confluent.config", "io.confluent.streams"})
@SpringBootApplication
public class CountAndTotalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountAndTotalApplication.class, args);
    }
}
