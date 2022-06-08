package io.confluent.consumer;

import io.confluent.model.avro.OnlineOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class BasicConsumerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicConsumerComponent.class);
    private static final String ONLINE_ORDERS = "online-orders";

    @KafkaListener(topics = ONLINE_ORDERS)
    @SuppressWarnings("unused")
    public void receive(@Payload OnlineOrder record) {
        LOGGER.info("Received='{}'", record.toString());
    }
}

