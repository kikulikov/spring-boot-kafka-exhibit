package io.confluent.admin;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
public class BasicAdminComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAdminComponent.class);

    @Value("${application.topic.online-orders}")
    private String onlineOrdersTopic;

    @Value("${application.topic.count-and-total}")
    private String countAndTotalTopic;

    @Value("${application.topic.default-num-partitions}")
    private int defaultNumPartitions;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaAdmin kafkaAdmin;

    @PostConstruct
    @SuppressWarnings("unused")
    public void before() {
        final var onlineOrders = new NewTopic(onlineOrdersTopic, Optional.of(defaultNumPartitions), Optional.empty());
        LOGGER.info("Creating='{}'", onlineOrders);

        final var countAndTotal = new NewTopic(countAndTotalTopic, Optional.of(defaultNumPartitions), Optional.empty());
        LOGGER.info("Creating='{}'", countAndTotal);

        kafkaAdmin.createOrModifyTopics(onlineOrders, countAndTotal);
    }
}

