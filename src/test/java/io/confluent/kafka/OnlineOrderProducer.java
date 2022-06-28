package io.confluent.kafka;

import io.confluent.model.avro.OnlineOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OnlineOrderProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineOrderProducer.class);

    @Autowired
    private KafkaTemplate<String, OnlineOrder> kafkaTemplate;

    public void send(String topic, OnlineOrder payload) {
        LOGGER.info("sending payload='{}' to topic='{}'", payload, topic);
        kafkaTemplate.send(topic, payload);
    }
}