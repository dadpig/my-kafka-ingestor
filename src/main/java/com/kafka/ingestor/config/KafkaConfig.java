package com.kafka.ingestor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

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
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
