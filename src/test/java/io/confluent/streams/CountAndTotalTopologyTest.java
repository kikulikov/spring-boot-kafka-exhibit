package io.confluent.streams;

import io.confluent.config.BasicAuthConfig;
import io.confluent.config.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.model.avro.CountAndTotal;
import io.confluent.model.avro.OnlineOrder;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static io.confluent.common.Dictionary.COUNT_AND_TOTAL;
import static io.confluent.common.Dictionary.ONLINE_ORDERS;
import static org.assertj.core.api.Assertions.assertThat;

class CountAndTotalTopologyTest {

    public static final Serde<String> STRING_SERDE = Serdes.String();
    public static final String MOCK_SCHEMA_REGISTRY = "mock://dummy:8081";

    private TestInputTopic<String, OnlineOrder> inputTopic;
    private TestOutputTopic<String, CountAndTotal> outputTopic;

//    @Autowired
//    private CountAndTotalTopology topology;

    private TopologyTestDriver testDriver;

    @BeforeEach
    void setUp() {
        final var registryConfig = new SchemaRegistryConfig();
        registryConfig.setUrl(MOCK_SCHEMA_REGISTRY);
        final var countAndTotalTopology = new CountAndTotalTopology(registryConfig, new BasicAuthConfig());

        final var streamsBuilder = new StreamsBuilder();
        countAndTotalTopology.countAndTotal(streamsBuilder); // injects the streams builder

        final Topology topology = streamsBuilder.build();
        this.testDriver = new TopologyTestDriver(topology, getConfig());

        final SpecificAvroSerde<OnlineOrder> onlineOrderSerde = new SpecificAvroSerde<>();
        onlineOrderSerde.configure(Map.of("schema.registry.url", MOCK_SCHEMA_REGISTRY), false);

        final SpecificAvroSerde<CountAndTotal> countAndTotalSerde = new SpecificAvroSerde<>();
        countAndTotalSerde.configure(Map.of("schema.registry.url", MOCK_SCHEMA_REGISTRY), false);

        inputTopic = testDriver.createInputTopic(ONLINE_ORDERS,
                STRING_SERDE.serializer(), onlineOrderSerde.serializer());

        outputTopic = testDriver.createOutputTopic(COUNT_AND_TOTAL,
                STRING_SERDE.deserializer(), countAndTotalSerde.deserializer());
    }

    @AfterEach
    void tearDown() {
        MockSchemaRegistry.dropScope(MOCK_SCHEMA_REGISTRY);
        testDriver.close(); // closing the topology test driver
    }

    @Test
    void testBasicScenario() {
        inputTopic.pipeInput(OnlineOrder.newBuilder().setCustomerId("CUSTOMER-101")
                .setProductId("PRODUCT-201").setQuantity(5).build());

        final var result = outputTopic.readValue();
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(5);
    }

    @Test
    void testComplexScenario() {
        inputTopic.pipeInput(OnlineOrder.newBuilder().setCustomerId("CUSTOMER-101")
                .setProductId("PRODUCT-201").setQuantity(5).build());

        // different customer but same product
        inputTopic.pipeInput(OnlineOrder.newBuilder().setCustomerId("CUSTOMER-X")
                .setProductId("PRODUCT-201").setQuantity(17).build());

        // different product but same customer
        inputTopic.pipeInput(OnlineOrder.newBuilder().setCustomerId("CUSTOMER-101")
                .setProductId("PRODUCT-X").setQuantity(19).build());

        // same product and same customer
        inputTopic.pipeInput(OnlineOrder.newBuilder().setCustomerId("CUSTOMER-101")
                .setProductId("PRODUCT-201").setQuantity(20).build());

        final var result1 = outputTopic.readKeyValue();
        assertThat(result1.key).isEqualTo("CUSTOMER-101+PRODUCT-201");
        assertThat(result1.value.getCount()).isEqualTo(1);
        assertThat(result1.value.getTotal()).isEqualTo(5);

        final var result2 = outputTopic.readKeyValue();
        assertThat(result2.key).isEqualTo("CUSTOMER-X+PRODUCT-201");
        assertThat(result2.value.getCount()).isEqualTo(1);
        assertThat(result2.value.getTotal()).isEqualTo(17);

        final var result3 = outputTopic.readKeyValue();
        assertThat(result3.key).isEqualTo("CUSTOMER-101+PRODUCT-X");
        assertThat(result3.value.getCount()).isEqualTo(1);
        assertThat(result3.value.getTotal()).isEqualTo(19);

        final var result4 = outputTopic.readKeyValue();
        assertThat(result4.key).isEqualTo("CUSTOMER-101+PRODUCT-201");
        assertThat(result4.value.getCount()).isEqualTo(2);
        assertThat(result4.value.getTotal()).isEqualTo(25);
    }

    private Properties getConfig() {
        final String stringSerde = STRING_SERDE.getClass().getName();

        final Properties config = new Properties();
        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "testing");
        config.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde);
        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde);

        return config;
    }
}