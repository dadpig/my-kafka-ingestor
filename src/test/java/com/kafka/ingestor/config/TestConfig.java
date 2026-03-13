package com.kafka.ingestor.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@EnableAutoConfiguration(excludeName = {
    "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@ComponentScan(
    basePackages = "com.kafka.ingestor",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {KafkaConfig.class, DataInitializer.class}
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com.kafka.ingestor.streams.*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com.kafka.ingestor.service.*Ingestor.*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com.kafka.ingestor.service.AnalyticsService"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com.kafka.ingestor.controller.AnalyticsController"
        )
    }
)
public class TestConfig {
}
