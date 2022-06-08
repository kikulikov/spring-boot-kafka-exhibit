package io.confluent.datasource;

import com.github.javafaker.Faker;
import io.confluent.model.avro.OnlineOrder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DataSourceFaker implements DataSourceService {

    private final Faker faker = new Faker();

    public OnlineOrder retrieveEvent() {
        return new OnlineOrder(faker.harryPotter().character(),
                faker.harryPotter().spell(), faker.random().nextInt(10));
    }

    private long recentTimestamp() {
        return faker.date().past(5, TimeUnit.MINUTES).getTime();
    }
}
