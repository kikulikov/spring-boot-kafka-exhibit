package io.confluent.admin;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
public class BasicAdminComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAdminComponent.class);
    static final String ONLINE_ORDERS = "online-orders";
    static final int NUM_PARTITIONS = 5;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaAdmin kafkaAdmin;

    @PostConstruct
    @SuppressWarnings("unused")
    public void before() {
        final var topic = new NewTopic(ONLINE_ORDERS, Optional.of(NUM_PARTITIONS), Optional.empty());
        LOGGER.info("Creating='{}'", topic);

        kafkaAdmin.createOrModifyTopics(topic);
    }
}

