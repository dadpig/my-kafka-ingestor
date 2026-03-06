package com.kafka.ingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
@EnableScheduling
public class KafkaStreamIngestorApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamIngestorApplication.class, args);
    }
}
