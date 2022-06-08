package io.confluent.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class BasicConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicConsumerApplication.class, args);
    }
}
