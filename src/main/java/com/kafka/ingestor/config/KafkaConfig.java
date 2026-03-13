package com.kafka.ingestor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.customers}")
    private String customersTopic;

    @Value("${kafka.topics.products}")
    private String productsTopic;

    @Value("${kafka.topics.sales}")
    private String salesTopic;

    @Value("${kafka.topics.sales-enriched}")
    private String salesEnrichedTopic;

    @Value("${kafka.topics.sales-aggregation}")
    private String salesAggregationTopic;

    @Value("${kafka.topics.salespersons}")
    private String salespersonsTopic;

    @Value("${kafka.topics.city-aggregation}")
    private String cityAggregationTopic;

    @Value("${kafka.topics.salesperson-aggregation}")
    private String salespersonAggregationTopic;

    @Value("${kafka.topics.sales-enriched-dlq}")
    private String salesEnrichedDlqTopic;

    @Bean
    public NewTopic customersTopic() {
        return TopicBuilder.name(customersTopic)
            .partitions(3)
            .replicas(1)
            .compact()
            .build();
    }

    @Bean
    public NewTopic productsTopic() {
        return TopicBuilder.name(productsTopic)
            .partitions(3)
            .replicas(1)
            .compact()
            .build();
    }

    @Bean
    public NewTopic salesTopic() {
        return TopicBuilder.name(salesTopic)
            .partitions(6)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salesEnrichedTopic() {
        return TopicBuilder.name(salesEnrichedTopic)
            .partitions(6)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salesAggregationTopic() {
        return TopicBuilder.name(salesAggregationTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salespersonsTopic() {
        return TopicBuilder.name(salespersonsTopic)
            .partitions(3)
            .replicas(1)
            .compact()
            .build();
    }

    @Bean
    public NewTopic cityAggregationTopic() {
        return TopicBuilder.name(cityAggregationTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salespersonAggregationTopic() {
        return TopicBuilder.name(salespersonAggregationTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salesEnrichedDlqTopic() {
        return TopicBuilder.name(salesEnrichedDlqTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
