package io.confluent.kafka;

import io.confluent.model.avro.OnlineOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static io.confluent.common.Dictionary.ONLINE_ORDERS;

@Component
public class OnlineOrderConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineOrderConsumer.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private OnlineOrder payload;

    @SuppressWarnings("unused")
    @KafkaListener(topics = ONLINE_ORDERS)
    public void receive(@Payload OnlineOrder order) {
        LOGGER.info("received payload='{}'", order.toString());
        this.payload = order;
        this.latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public OnlineOrder getPayload() {
        return payload;
    }
}