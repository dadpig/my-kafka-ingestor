package com.kafka.ingestor.controller;

import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import com.kafka.ingestor.service.AnalyticsService;
import com.kafka.ingestor.streams.SalesStreamProcessor;
import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for MonitoringController - Refactored Architecture
 * Only tests DATABASE entities (Products + Sales)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "ingestor.database.enabled=false",
        "ingestor.filesystem.enabled=false",
        "ingestor.webservice.enabled=false"
    })
@ActiveProfiles("test")
class MonitoringControllerTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private SalesStreamProcessor salesStreamProcessor;

    @MockitoBean
    private StreamsBuilder streamsBuilder;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private SaleRepository saleRepository;

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void shouldReturnStatusSuccessfully() {
        when(productRepository.count()).thenReturn(500L);
        when(productRepository.countByProcessedFalse()).thenReturn(50L);
        when(saleRepository.count()).thenReturn(10000L);
        when(saleRepository.countByProcessedFalse()).thenReturn(100L);

        String url = "http://localhost:" + port + "/api/monitoring/status";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();

        Map<String, Object> products = (Map<String, Object>) body.get("products");
        assertThat(products.get("total")).isEqualTo(500);
        assertThat(products.get("pending")).isEqualTo(50);

        Map<String, Object> sales = (Map<String, Object>) body.get("sales");
        assertThat(sales.get("total")).isEqualTo(10000);
        assertThat(sales.get("pending")).isEqualTo(100);
    }

    @Test
    void shouldReturnHealthSuccessfully() {
        String url = "http://localhost:" + port + "/api/monitoring/health";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, String> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("UP");
        assertThat(body.get("application")).isEqualTo("kafka-stream-ingestor");
        assertThat(body.get("version")).isEqualTo("1.0.0");
        assertThat(body.get("architecture")).isEqualTo("refactored");
    }
}
