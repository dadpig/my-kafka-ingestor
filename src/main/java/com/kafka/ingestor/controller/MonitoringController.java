package com.kafka.ingestor.controller;

import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring Controller - Refactored Architecture
 * Monitors only DATABASE entities (Products + Sales)
 * Customers are from FILE_SYSTEM, Salespeople from WEB_SERVICE
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    public MonitoringController(ProductRepository productRepository,
                               SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();

        // Products (DATABASE)
        Map<String, Long> productStats = new HashMap<>();
        productStats.put("total", productRepository.count());
        productStats.put("pending", productRepository.countByProcessedFalse());

        // Sales (DATABASE)
        Map<String, Long> saleStats = new HashMap<>();
        saleStats.put("total", saleRepository.count());
        saleStats.put("pending", saleRepository.countByProcessedFalse());

        status.put("products", productStats);
        status.put("sales", saleStats);

        // Add architecture info
        status.put("architecture", Map.of(
            "database", "Products + Sales",
            "fileSystem", "Customers (JSON)",
            "webService", "Salespeople (REST API)"
        ));

        return ResponseEntity.ok(status);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "kafka-stream-ingestor");
        health.put("version", "1.0.0");
        health.put("architecture", "refactored");
        return ResponseEntity.ok(health);
    }
}
