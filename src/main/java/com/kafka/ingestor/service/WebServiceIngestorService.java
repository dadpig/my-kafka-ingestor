package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "ingestor.webservice", name = "enabled", havingValue = "true")
public class WebServiceIngestorService {

    private static final Logger logger = LoggerFactory.getLogger(WebServiceIngestorService.class);

    private final WebClient webClient;
    private final KafkaProducerService kafkaProducerService;

    @Value("${ingestor.webservice.endpoint}")
    private String endpoint;

    @Value("${ingestor.webservice.timeout}")
    private long timeout;

    public WebServiceIngestorService(WebClient.Builder webClientBuilder, KafkaProducerService kafkaProducerService) {
        this.webClient = webClientBuilder.build();
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedDelayString = "${ingestor.webservice.poll-interval}")
    public void fetchAndIngest() {
        try {
            List<Customer> customers = webClient.get()
                .uri(endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Customer>>() {})
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(e -> {
                    logger.error("Error fetching customers from web service", e);
                    return Mono.empty();
                })
                .block();

            if (customers != null && !customers.isEmpty()) {
                logger.info("Fetched {} customers from web service", customers.size());
                customers.forEach(kafkaProducerService::sendCustomer);
            }

        } catch (Exception e) {
            logger.error("Unexpected error in web service ingestor", e);
        }
    }
}
