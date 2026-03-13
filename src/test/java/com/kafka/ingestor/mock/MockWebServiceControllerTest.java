package com.kafka.ingestor.mock;

import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.service.AnalyticsService;
import com.kafka.ingestor.service.KafkaProducerService;
import com.kafka.ingestor.streams.SalesStreamProcessor;
import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "mock.webservice.enabled=true",
        "ingestor.database.enabled=false",
        "ingestor.filesystem.enabled=false",
        "ingestor.webservice.enabled=false"
    })
@ActiveProfiles("test")
class MockWebServiceControllerTest {

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

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnCustomersWithDefaultLimit() {
        ResponseEntity<List<Customer>> response = restTemplate.exchange(
            "/api/customers",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Customer>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().size());
    }

    @Test
    void shouldReturnCustomersWithCustomLimit() {
        ResponseEntity<List<Customer>> response = restTemplate.exchange(
            "/api/customers?limit=5",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Customer>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().size());
    }

    @Test
    void shouldReturnCustomersBatch() {
        ResponseEntity<List<Customer>> response = restTemplate.exchange(
            "/api/customers/batch?batchSize=50",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Customer>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().size());
    }

    @Test
    void shouldReturnCustomersBySegment() {
        ResponseEntity<List<Customer>> response = restTemplate.exchange(
            "/api/customers/segment?segment=Premium&limit=3",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Customer>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        response.getBody().forEach(customer ->
            assertEquals("Premium", customer.getSegment())
        );
    }

    @Test
    void shouldGenerateValidCustomerData() {
        ResponseEntity<List<Customer>> response = restTemplate.exchange(
            "/api/customers?limit=1",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Customer>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Customer customer = response.getBody().get(0);

        assertNotNull(customer.getCustomerId());
        assertNotNull(customer.getName());
        assertNotNull(customer.getEmail());
        assertNotNull(customer.getSegment());
        assertNotNull(customer.getRegion());
        assertNotNull(customer.getCreatedAt());
    }
}
