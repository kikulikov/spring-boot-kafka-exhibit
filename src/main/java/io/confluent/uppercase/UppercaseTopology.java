package io.confluent.uppercase;

import io.confluent.model.avro.OnlineOrder;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import static io.confluent.common.Dictionary.ONLINE_ORDERS;

@Configuration
@EnableKafkaStreams
public class UppercaseTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(UppercaseTopology.class);
    private static final String ONLINE_ORDERS_UPPERCASE = ONLINE_ORDERS + "-uppercase";

    @Bean
    public KStream<Object, OnlineOrder> uppercase(StreamsBuilder streamsBuilder) {

        final KStream<Object, OnlineOrder> onlineOrders = streamsBuilder
                .stream(ONLINE_ORDERS);

        onlineOrders
                .mapValues(m -> {
                    m.setProductId(m.getProductId());
                    return m;
                })
                .peek((m, n) -> LOGGER.debug("Uppercase [" + m + ":" + n + "]"))
                .to(ONLINE_ORDERS_UPPERCASE);


        return onlineOrders;
    }
}