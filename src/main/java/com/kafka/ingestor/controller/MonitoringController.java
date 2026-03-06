package com.kafka.ingestor.controller;

import com.kafka.ingestor.repository.CustomerRepository;
import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    public MonitoringController(CustomerRepository customerRepository,
                               ProductRepository productRepository,
                               SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();

        Map<String, Long> customerStats = new HashMap<>();
        customerStats.put("total", customerRepository.count());
        customerStats.put("pending", customerRepository.countByProcessedFalse());

        Map<String, Long> productStats = new HashMap<>();
        productStats.put("total", productRepository.count());
        productStats.put("pending", productRepository.countByProcessedFalse());

        Map<String, Long> saleStats = new HashMap<>();
        saleStats.put("total", saleRepository.count());
        saleStats.put("pending", saleRepository.countByProcessedFalse());

        status.put("customers", customerStats);
        status.put("products", productStats);
        status.put("sales", saleStats);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "kafka-stream-ingestor");
        return ResponseEntity.ok(health);
    }
}
