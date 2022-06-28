package io.confluent.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

import static io.confluent.common.Dictionary.COUNT_AND_TOTAL;
import static io.confluent.common.Dictionary.ONLINE_ORDERS;
import static org.junit.jupiter.api.Assertions.*;

class CountAndTotalTopologyTest {

    public static final Serde<String> STRING_SERDE = Serdes.String();
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @Autowired
    private CountAndTotalTopology topology;

    @BeforeEach
    void setUp() {
        final TopologyTestDriver testDriver = new TopologyTestDriver(topology, getConfig());

        inputTopic = testDriver.createInputTopic(ONLINE_ORDERS,
                STRING_SERDE.serializer(), STRING_SERDE.serializer());

        outputTopic = testDriver.createOutputTopic(COUNT_AND_TOTAL,
                STRING_SERDE.deserializer(), STRING_SERDE.deserializer());
    }

    @Test
    void testBasicScenario() {
        inputTopic.pipeInput("moo");
        Assertions.assertThat(outputTopic.readValue()).isEqualTo("MOO");

        inputTopic.pipeInput("oink");
        Assertions.assertThat(outputTopic.readValue()).isEqualTo("OINK");
    }

    private Properties getConfig() {
        final String stringSerde = STRING_SERDE.getClass().getName();

        final Properties config = new Properties();
        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "testing");
        config.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde);
        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde);

        return config;
    }
}