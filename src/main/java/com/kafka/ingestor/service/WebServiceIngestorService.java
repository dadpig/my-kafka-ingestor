package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.Salesperson;
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

/**
 * Web Service Ingestor Service - Refactored Architecture
 * Ingests: Salespeople (100) from REST API
 * Does NOT ingest: Customers (FILE_SYSTEM) or Products (DATABASE)
 */
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

    /**
     * Fetch salespeople from web service (MockWebServiceController)
     * Endpoint: /api/salespeople?limit=100
     */
    @Scheduled(fixedDelayString = "${ingestor.webservice.poll-interval}")
    public void fetchAndIngest() {
        try {
            List<Salesperson> salespeople = webClient.get()
                .uri(endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Salesperson>>() {})
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(e -> {
                    logger.error("❌ WEB_SERVICE: Error fetching salespeople from web service", e);
                    return Mono.empty();
                })
                .block();

            if (salespeople != null && !salespeople.isEmpty()) {
                logger.info("🌐 WEB_SERVICE: Fetched {} salespeople from web service", salespeople.size());
                salespeople.forEach(salesperson -> {
                    salesperson.setDataSource("WEB_SERVICE");
                    kafkaProducerService.sendSalesperson(salesperson);
                });
                logger.info("✅ WEB_SERVICE: Processed {} salespeople", salespeople.size());
            }

        } catch (Exception e) {
            logger.error("❌ WEB_SERVICE: Unexpected error in web service ingestor", e);
        }
    }
}
