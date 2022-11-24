package io.confluent.uppercase;

import io.confluent.config.BasicAuthConfig;
import io.confluent.config.SchemaRegistryConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.model.avro.CountAndTotal;
import io.confluent.model.avro.OnlineOrder;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.lang.NonNull;

import static io.confluent.common.Dictionary.COUNT_AND_TOTAL;
import static io.confluent.common.Dictionary.ONLINE_ORDERS;

@Configuration
@EnableKafkaStreams
public class UppercaseTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(UppercaseTopology.class);

    @Bean
    public KStream<Object, Object> uppercase(StreamsBuilder streamsBuilder) {

        final KStream<Object, Object> onlineOrders = streamsBuilder
                .stream(ONLINE_ORDERS);

        final KTable<String, CountAndTotal> aggregated =
                onlineOrders
                        .selectKey((m, n) -> n.getCustomerId() + "+" + n.getProductId())
                        .repartition()
                        .groupByKey()
                        .aggregate(() -> new CountAndTotal(0, 0), (key, value, agg) -> {
                            agg.setCount(agg.getCount() + 1);
                            agg.setTotal(agg.getTotal() + value.getQuantity());
                            return agg;
                        }, mat);

        aggregated.toStream()
                .peek((m, n) -> LOGGER.debug("Aggregated [" + m + ":" + n + "]"))
                .to(COUNT_AND_TOTAL, Produced.with(STRING_SERDE, countAndTotalSerde));

        return onlineOrders;
    }
}