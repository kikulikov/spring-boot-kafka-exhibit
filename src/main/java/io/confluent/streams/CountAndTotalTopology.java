package io.confluent.streams;

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
public class CountAndTotalTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountAndTotalTopology.class);
    private static final String COUNT_AND_TOTAL_STORE = "count-and-total-store";

    private static final Serde<String> STRING_SERDE = Serdes.String();

    private final Serde<OnlineOrder> onlineOrderSerde = new SpecificAvroSerde<>();
    private final Serde<CountAndTotal> countAndTotalSerde = new SpecificAvroSerde<>();

    public CountAndTotalTopology(@NonNull SchemaRegistryConfig registryConfig, @NonNull BasicAuthConfig authConfig) {

        final var configMap = registryConfig.asMap();
        configMap.putAll(authConfig.asMap());

        onlineOrderSerde.configure(configMap, false); // false for record values
        countAndTotalSerde.configure(configMap, false); // false for record values
    }

    /*
    Scenario: Calculate the number of orders and purchased products per customer

    GIVEN The input data stream of purchases is highly skewed
    WHEN events are repartitioned per customer and product
    THEN total quantity calculated and returned as a result
     */
    @Bean
    public KStream<String, OnlineOrder> countAndTotal(StreamsBuilder streamsBuilder) {

        final Materialized<String, CountAndTotal, KeyValueStore<Bytes, byte[]>> mat =
                Materialized.<String, CountAndTotal, KeyValueStore<Bytes, byte[]>>as(COUNT_AND_TOTAL_STORE)
                        .withKeySerde(STRING_SERDE).withValueSerde(countAndTotalSerde);

        final KStream<String, OnlineOrder> onlineOrders = streamsBuilder
                .stream(ONLINE_ORDERS, Consumed.with(STRING_SERDE, onlineOrderSerde));

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