package com.kafka.ingestor.mock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.ingestor.domain.Salesperson;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock Web Service Controller - Serves 100 salespeople
 * Architecture: Web Service provides Salesperson data (SP00001-SP00100)
 * Data is loaded from salespeople-100-reference.json
 */
@RestController
@RequestMapping("/api/salespeople")
@ConditionalOnProperty(prefix = "mock.webservice", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MockWebServiceController {

    private static final Logger logger = LoggerFactory.getLogger(MockWebServiceController.class);
    private final ObjectMapper objectMapper;
    private List<Salesperson> salespeople = new ArrayList<>();

    public MockWebServiceController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadSalespeople() {
        try {
            // Try to load from file system first
            java.io.File file = new java.io.File("data/salespeople-100-reference.json");
            if (file.exists()) {
                salespeople = objectMapper.readValue(file, new TypeReference<List<Salesperson>>() {});
                logger.info("✅ Loaded {} salespeople from filesystem: data/salespeople-100-reference.json", salespeople.size());
            } else {
                // Fallback to classpath
                ClassPathResource resource = new ClassPathResource("data/salespeople-100-reference.json");
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        salespeople = objectMapper.readValue(is, new TypeReference<List<Salesperson>>() {});
                        logger.info("✅ Loaded {} salespeople from classpath: data/salespeople-100-reference.json", salespeople.size());
                    }
                } else {
                    logger.warn("⚠️ Salespeople reference file not found, returning empty list");
                }
            }

            // Set data source for all loaded salespeople
            salespeople.forEach(sp -> sp.setDataSource("WEB_SERVICE"));

        } catch (IOException e) {
            logger.error("❌ Failed to load salespeople reference file", e);
        }
    }

    /**
     * Get all salespeople or a limited batch
     * GET /api/salespeople?limit=100
     */
    @GetMapping
    public List<Salesperson> getSalespeople(@RequestParam(defaultValue = "100") int limit) {
        return salespeople.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get salespeople batch (alias for compatibility)
     * GET /api/salespeople/batch?batchSize=100
     */
    @GetMapping("/batch")
    public List<Salesperson> getSalespeopleBatch(@RequestParam(defaultValue = "100") int batchSize) {
        return getSalespeople(batchSize);
    }

    /**
     * Get salespeople by city
     * GET /api/salespeople/city?city=London&limit=10
     */
    @GetMapping("/city")
    public List<Salesperson> getSalespeopleByCity(@RequestParam String city,
                                                   @RequestParam(defaultValue = "10") int limit) {
        return salespeople.stream()
            .filter(sp -> city.equalsIgnoreCase(sp.getCity()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get salespeople by country
     * GET /api/salespeople/country?country=USA&limit=10
     */
    @GetMapping("/country")
    public List<Salesperson> getSalespeopleByCountry(@RequestParam String country,
                                                      @RequestParam(defaultValue = "10") int limit) {
        return salespeople.stream()
            .filter(sp -> country.equalsIgnoreCase(sp.getCountry()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get salesperson by ID
     * GET /api/salespeople/SP00001
     */
    @GetMapping("/{salespersonId}")
    public Salesperson getSalespersonById(@PathVariable String salespersonId) {
        return salespeople.stream()
            .filter(sp -> salespersonId.equals(sp.getSalespersonId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Health check endpoint
     * GET /api/salespeople/health
     */
    @GetMapping("/health")
    public java.util.Map<String, Object> health() {
        return java.util.Map.of(
            "status", "UP",
            "totalSalespeople", salespeople.size(),
            "dataSource", "WEB_SERVICE",
            "idRange", salespeople.isEmpty() ? "N/A" :
                salespeople.get(0).getSalespersonId() + " - " +
                salespeople.get(salespeople.size() - 1).getSalespersonId()
        );
    }
}
