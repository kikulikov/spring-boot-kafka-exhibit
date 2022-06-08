package io.confluent.producer;

import io.confluent.datasource.DataSourceService;
import io.confluent.model.avro.OnlineOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static io.confluent.common.Dictionary.ONLINE_ORDERS;

@Component
public class BasicProducerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicProducerComponent.class);

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @SuppressWarnings("unused")
    private DataSourceService dataSource;

    @Scheduled(initialDelay = 500, fixedRate = 2000)
    @SuppressWarnings("unused")
    public void produce() {
        final OnlineOrder event = dataSource.retrieveEvent();

        LOGGER.info("Sending='{}'", event);
        kafkaTemplate.send(ONLINE_ORDERS, event);
    }
}
