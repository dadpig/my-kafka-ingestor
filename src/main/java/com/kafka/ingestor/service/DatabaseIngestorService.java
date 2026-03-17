package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.entity.ProductEntity;
import com.kafka.ingestor.entity.SaleEntity;
import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Database Ingestor Service - Refactored Architecture
 * Ingests: Sales (10,000) + Products (500) from database
 * Does NOT ingest: Customers (FILE_SYSTEM) or Salespeople (WEB_SERVICE)
 */
@Service
@ConditionalOnProperty(prefix = "ingestor.database", name = "enabled", havingValue = "true")
public class DatabaseIngestorService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIngestorService.class);
    private static final int BATCH_SIZE = 100;

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final KafkaProducerService kafkaProducerService;

    public DatabaseIngestorService(ProductRepository productRepository,
                                  SaleRepository saleRepository,
                                  KafkaProducerService kafkaProducerService) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Ingest products from database (500 records)
     * Products are stored in database and sent to Kafka topic
     */
    @Scheduled(fixedDelayString = "${ingestor.database.poll-interval}")
    @Transactional
    public void ingestProducts() {
        List<ProductEntity> unprocessed = productRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (unprocessed.isEmpty()) {
            return;
        }

        logger.info("📊 DATABASE: Ingesting {} products", unprocessed.size());

        unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(this::toProductDomain)
            .forEach(kafkaProducerService::sendProduct);

        List<String> processedIds = unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(ProductEntity::getProductId)
            .toList();

        productRepository.markAsProcessed(processedIds);
        logger.info("✅ DATABASE: Marked {} products as processed", processedIds.size());
    }

    /**
     * Ingest sales from database (10,000 records)
     * Sales reference: Customers (FILE_SYSTEM), Products (DATABASE), Salespeople (WEB_SERVICE)
     */
    @Scheduled(fixedDelayString = "${ingestor.database.poll-interval}")
    @Transactional
    public void ingestSales() {
        List<SaleEntity> unprocessed = saleRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (unprocessed.isEmpty()) {
            return;
        }

        logger.info("📊 DATABASE: Ingesting {} sales", unprocessed.size());

        unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(this::toSaleDomain)
            .forEach(kafkaProducerService::sendSale);

        List<String> processedIds = unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(SaleEntity::getSaleId)
            .toList();

        saleRepository.markAsProcessed(processedIds);
        logger.info("✅ DATABASE: Marked {} sales as processed", processedIds.size());
    }

    private Product toProductDomain(ProductEntity entity) {
        return new Product(
            entity.getProductId(),
            entity.getName(),
            entity.getCategory(),
            entity.getPrice(),
            entity.getManufacturer(),
            entity.getCreatedAt(),
            "DATABASE"
        );
    }

    private Sale toSaleDomain(SaleEntity entity) {
        return new Sale(
            entity.getSaleId(),
            entity.getCustomerId(),      // References FILE_SYSTEM (CUST00001-CUST01000)
            entity.getProductId(),       // References DATABASE (PROD00001-PROD00500)
            entity.getSalespersonId(),   // References WEB_SERVICE (SP00001-SP00100)
            entity.getQuantity(),
            entity.getUnitPrice(),
            entity.getTotalAmount(),
            entity.getSaleDate(),
            entity.getChannel(),
            "DATABASE"
        );
    }
}
