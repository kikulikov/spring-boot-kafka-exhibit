package io.confluent.kafka;

import io.confluent.model.avro.OnlineOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.concurrent.TimeUnit;

import static io.confluent.common.Dictionary.ONLINE_ORDERS;
import static org.assertj.core.api.Assertions.assertThat;

//@EnableAutoConfiguration
//@ComponentScan({"io.confluent.utils", "io.confluent.consumer"})
@SpringBootTest
@EmbeddedKafka
class BasicKafkaTest {

    @Autowired
    private OnlineOrderProducer producer;

    @Autowired
    private OnlineOrderConsumer consumer;

    @Test
    public void shouldConsumeData() throws InterruptedException {

        final var order = OnlineOrder.newBuilder()
                .setCustomerId("CUSTOMER-11").setProductId("PRODUCT-22").setQuantity(42).build();

        producer.send(ONLINE_ORDERS, order);
        assertThat(consumer.getLatch().await(10, TimeUnit.SECONDS)).isTrue();

        final var payload = consumer.getPayload();
        assertThat(payload.getCustomerId()).isEqualTo(order.getCustomerId());
        assertThat(payload.getProductId()).isEqualTo(order.getProductId());
        assertThat(payload.getQuantity()).isEqualTo(order.getQuantity());
    }
}