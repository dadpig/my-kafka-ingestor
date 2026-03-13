package com.kafka.ingestor.integration;

import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.SalesEnriched;
import com.kafka.ingestor.service.AnalyticsService;
import com.kafka.ingestor.service.KafkaProducerService;
import com.kafka.ingestor.streams.SalesStreamProcessor;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "ingestor.database.enabled=false",
    "ingestor.filesystem.enabled=false",
    "ingestor.webservice.enabled=false",
    "spring.kafka.streams.auto-startup=false"
})
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"customers", "sales", "sales-enriched"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
class KafkaIntegrationTest {

    @MockitoBean
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private SalesStreamProcessor salesStreamProcessor;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private StreamsBuilder streamsBuilder;

    @Test
    void shouldProduceAndConsumeCustomer() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        Customer customer = new Customer("CUST001", "Test Customer", "test@example.com",
            "Premium", "North", Instant.now());

        try (KafkaProducer<String, Customer> producer = new KafkaProducer<>(producerProps);
             KafkaConsumer<String, Customer> consumer = new KafkaConsumer<>(consumerProps)) {

            producer.send(new ProducerRecord<>("customers", customer.getCustomerId(), customer));
            producer.flush();

            consumer.subscribe(Collections.singletonList("customers"));
            ConsumerRecords<String, Customer> records = consumer.poll(Duration.ofSeconds(10));

            assertFalse(records.isEmpty());
            records.forEach(record -> {
                assertEquals("CUST001", record.key());
                assertEquals("Test Customer", record.value().getName());
            });
        }
    }
}
