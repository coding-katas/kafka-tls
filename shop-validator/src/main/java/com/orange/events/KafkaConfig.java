package com.orange.events;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.orange.dto.ShopDTO;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.Stores;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.serializer.JsonSerde;

import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.apache.kafka.streams.KeyValue;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConfig {


    private static final String INPUT_TOPIC_NAME = "SHOP_TOPIC";
    private static final String OUTPUT_TOPIC_EVENT_NAME = "SHOP_TOPIC_EVENT";

    private static final int COMMIT_MS = 700;
    private static final int WINDOWS_MS = 200;
    private static final int GRACE_MS = 100;

    private Path stateDirectory;
    private static final String storeName = "eventId-store";

    @Value(value = "${spring.kafka.bootstrap-servers:instance-1:9093}")
    private String bootstrapServers;

    @Value(value = "${spring.kafka.security.protocol:SSL}")
    private String securityProtocol;

    @Value(value = "${spring.kafka.ssl.trust-store-location:/data/certs/truststore.jks}")
    private String trustStoreLocation;

    @Value(value = "${spring.kafka.ssl.trust-store-password:password}")
    private String trustStorePassword;

    @Value(value = "${spring.kafka.ssl.key-store-location:/data/certs/keystore.jks}")
    private String keyStoreLocation;

    @Value(value = "${spring.kafka.ssl.key-store-password:password}")
    private String keyStorePassword;

    @Value(value = "${spring.kafka.ssl.key-password:password}")
    private String keyPassword;


    public ProducerFactory<String, ShopDTO> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "100");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client-1");
        props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);

        props.put("security.protocol", securityProtocol);
        props.put("ssl.truststore.location", trustStoreLocation);
        props.put("ssl.truststore.password", trustStorePassword);
        props.put("ssl.keystore.location", keyStoreLocation);
        props.put("ssl.keystore.password", keyStorePassword);
        props.put("ssl.key.password", keyPassword);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ShopDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public ConsumerFactory<String, ShopDTO> consumerFactory() {
        JsonDeserializer<ShopDTO> deserializer = new JsonDeserializer<>(ShopDTO.class);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        props.put("security.protocol", securityProtocol);
        props.put("ssl.truststore.location", trustStoreLocation);
        props.put("ssl.truststore.password", trustStorePassword);
        props.put("ssl.keystore.location", keyStoreLocation);
        props.put("ssl.keystore.password", keyStorePassword);
        props.put("ssl.key.password", keyPassword);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShopDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ShopDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public KafkaStreams kafkaStreams() throws InterruptedException {

        log.info("START KAFKA STREAMINIG");

        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "deduplication-kafka");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try {
            this.stateDirectory = Files.createTempDirectory("kafka-streams");
            streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, this.stateDirectory.toAbsolutePath()
                .toString());
        } catch (final IOException e) {
            throw new UncheckedIOException("Cannot create temporary directory", e);
        }        

        final int windowTime = 700;

        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  new JsonSerde<>(ShopDTO.class).getClass());
        streamsConfiguration.put("commit.interval.ms", COMMIT_MS);
        streamsConfiguration.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");        
        streamsConfiguration.put("security.protocol", securityProtocol);
        streamsConfiguration.put("ssl.truststore.location", trustStoreLocation);
        streamsConfiguration.put("ssl.truststore.password", trustStorePassword);
        streamsConfiguration.put("ssl.keystore.location", keyStoreLocation);
        streamsConfiguration.put("ssl.keystore.password", keyStorePassword);
        streamsConfiguration.put("ssl.key.password", keyPassword);

        final Duration windowSize = Duration.ofMinutes(10);

        final StreamsBuilder builder = new StreamsBuilder();

        final Duration retentionPeriod = windowSize;

        final StoreBuilder<WindowStore<String, Long>> dedupStoreBuilder = Stores.windowStoreBuilder(
                Stores.persistentWindowStore(storeName,
                                             retentionPeriod,
                                             windowSize,
                                             false
                ),
                Serdes.String(),
                Serdes.Long());

        builder.addStateStore(dedupStoreBuilder);

	KStream<String, ShopDTO> source = builder.stream(INPUT_TOPIC_NAME, Consumed.with(Serdes.String(), new JsonSerde<>(ShopDTO.class)));
 
        KTable reducedTable =
            source
                .selectKey((key, value) -> value.getBuyerIdentifier())
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(ShopDTO.class)))
		.windowedBy(TimeWindows.of(Duration.ofMillis(WINDOWS_MS)).grace(Duration.ofMillis(GRACE_MS)))
                .reduce((v1, v2) -> v2, Materialized.<String, ShopDTO, WindowStore<Bytes, byte[]>>as("NewStore")
                        .withValueSerde(new JsonSerde<>(ShopDTO.class))
                        .withKeySerde(Serdes.String()));
        KStream<String, ShopDTO> outputStream = reducedTable.toStream().map((windowedId, value) -> new KeyValue<>(windowedId.toString(), value));
        outputStream.to(OUTPUT_TOPIC_EVENT_NAME, Produced.with(Serdes.String(), new JsonSerde<>(ShopDTO.class)));

//                .suppress(Suppressed.untilWindowCloses(unbounded()));
//                .mapValues(value -> {
  //                  log.info("MAP key={}, value={}", value.getBuyerIdentifier(), value.toString());
   //                 return value;
    //            });



        final Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, new StreamsConfig(streamsConfiguration));
        streams.start();
        return streams;               
   }

}
