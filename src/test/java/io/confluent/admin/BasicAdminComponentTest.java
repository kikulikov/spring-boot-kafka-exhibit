package io.confluent.admin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static io.confluent.admin.BasicAdminComponent.NUM_PARTITIONS;
import static io.confluent.admin.BasicAdminComponent.ONLINE_ORDERS;

@SpringBootTest
@EmbeddedKafka(brokerProperties = {"auto.create.topics.enable=false"})
class BasicAdminComponentTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Test
    public void shouldCreateTheTopic() {
        final var topics = kafkaAdmin.describeTopics(ONLINE_ORDERS);
        Assertions.assertThat(topics).containsKey(ONLINE_ORDERS);
        Assertions.assertThat(topics.get(ONLINE_ORDERS).partitions()).hasSize(NUM_PARTITIONS);
    }
}