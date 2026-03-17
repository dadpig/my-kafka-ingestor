package com.kafka.ingestor.mock;

import com.kafka.ingestor.domain.Salesperson;
import com.kafka.ingestor.service.AnalyticsService;
import com.kafka.ingestor.service.KafkaProducerService;
import com.kafka.ingestor.streams.SalesStreamProcessor;
import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MockWebServiceController - Refactored to test Salespeople (not Customers)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "mock.webservice.enabled=true",
        "ingestor.database.enabled=false",
        "ingestor.filesystem.enabled=false",
        "ingestor.webservice.enabled=false"
    })
@ActiveProfiles("test")
class MockWebServiceControllerTest {

    @LocalServerPort
    private int port;

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

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void shouldReturnSalespeopleWithDefaultLimit() {
        String url = "http://localhost:" + port + "/api/salespeople";
        ResponseEntity<List<Salesperson>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() <= 100, "Should return at most 100 salespeople");
    }

    @Test
    void shouldReturnSalespeopleWithCustomLimit() {
        String url = "http://localhost:" + port + "/api/salespeople?limit=5";
        ResponseEntity<List<Salesperson>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() <= 5, "Should return at most 5 salespeople");
    }

    @Test
    void shouldReturnSalespeopleByCity() {
        // First get all salespeople to find a valid city
        String allUrl = "http://localhost:" + port + "/api/salespeople?limit=100";
        ResponseEntity<List<Salesperson>> allResponse = restTemplate.exchange(
            allUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertNotNull(allResponse.getBody());
        if (!allResponse.getBody().isEmpty()) {
            String city = allResponse.getBody().get(0).getCity();

            String url = "http://localhost:" + port + "/api/salespeople/city?city=" + city + "&limit=10";
            ResponseEntity<List<Salesperson>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Salesperson>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            response.getBody().forEach(salesperson ->
                assertEquals(city, salesperson.getCity())
            );
        }
    }

    @Test
    void shouldReturnSalespeopleByCountry() {
        // First get all salespeople to find a valid country
        String allUrl = "http://localhost:" + port + "/api/salespeople?limit=100";
        ResponseEntity<List<Salesperson>> allResponse = restTemplate.exchange(
            allUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertNotNull(allResponse.getBody());
        if (!allResponse.getBody().isEmpty()) {
            String country = allResponse.getBody().get(0).getCountry();

            String url = "http://localhost:" + port + "/api/salespeople/country?country=" + country + "&limit=10";
            ResponseEntity<List<Salesperson>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Salesperson>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            response.getBody().forEach(salesperson ->
                assertEquals(country, salesperson.getCountry())
            );
        }
    }

    @Test
    void shouldReturnSalespersonById() {
        // First get all salespeople
        String allUrl = "http://localhost:" + port + "/api/salespeople?limit=100";
        ResponseEntity<List<Salesperson>> allResponse = restTemplate.exchange(
            allUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertNotNull(allResponse.getBody());
        if (!allResponse.getBody().isEmpty()) {
            String salespersonId = allResponse.getBody().get(0).getSalespersonId();

            String url = "http://localhost:" + port + "/api/salespeople/" + salespersonId;
            ResponseEntity<Salesperson> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Salesperson.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(salespersonId, response.getBody().getSalespersonId());
        }
    }

    @Test
    void shouldReturnHealthStatus() {
        String url = "http://localhost:" + port + "/api/salespeople/health";
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("totalSalespeople"));
    }

    @Test
    void shouldGenerateValidSalespersonData() {
        String url = "http://localhost:" + port + "/api/salespeople?limit=100";
        ResponseEntity<List<Salesperson>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        if (!response.getBody().isEmpty()) {
            Salesperson salesperson = response.getBody().get(0);

            assertNotNull(salesperson.getSalespersonId());
            assertTrue(salesperson.getSalespersonId().startsWith("SP"));
            assertNotNull(salesperson.getName());
            assertNotNull(salesperson.getEmail());
            assertNotNull(salesperson.getCity());
            assertNotNull(salesperson.getCountry());
            assertNotNull(salesperson.getCreatedAt());
            assertEquals("WEB_SERVICE", salesperson.getDataSource());
        }
    }

    @Test
    void shouldReturnBatchOfSalespeople() {
        String url = "http://localhost:" + port + "/api/salespeople/batch?batchSize=50";
        ResponseEntity<List<Salesperson>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Salesperson>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() <= 50, "Should return at most 50 salespeople");
    }
}
