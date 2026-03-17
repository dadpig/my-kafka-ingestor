package com.kafka.ingestor.service;

import com.kafka.ingestor.entity.ProductEntity;
import com.kafka.ingestor.entity.SaleEntity;
import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests for DatabaseIngestorService - Refactored Architecture
 * Tests only Sales + Products ingestion (not Customers or Salespeople)
 */
@ExtendWith(MockitoExtension.class)
class DatabaseIngestorServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    private DatabaseIngestorService databaseIngestorService;

    @BeforeEach
    void setUp() {
        databaseIngestorService = new DatabaseIngestorService(
            productRepository, saleRepository, kafkaProducerService);
    }

    // ==================== PRODUCT TESTS ====================

    @Test
    void shouldIngestProductsSuccessfully() {
        ProductEntity product = new ProductEntity("PROD00001", "Test Product", "Electronics",
            BigDecimal.valueOf(99.99), "TestCorp", Instant.now());
        List<ProductEntity> products = List.of(product);

        when(productRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(products);

        databaseIngestorService.ingestProducts();

        verify(kafkaProducerService, times(1)).sendProduct(any());
        verify(productRepository, times(1)).markAsProcessed(anyList());
    }

    @Test
    void shouldSkipIngestWhenNoUnprocessedProducts() {
        when(productRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

        databaseIngestorService.ingestProducts();

        verify(kafkaProducerService, never()).sendProduct(any());
        verify(productRepository, never()).markAsProcessed(anyList());
    }

    @Test
    void shouldHandleLargeBatchOfProducts() {
        List<ProductEntity> products = generateProductEntities(150);

        when(productRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(products);

        databaseIngestorService.ingestProducts();

        // Should process only BATCH_SIZE (100) at a time
        verify(kafkaProducerService, times(100)).sendProduct(any());
        verify(productRepository, times(1)).markAsProcessed(argThat(list -> list.size() == 100));
    }

    // ==================== SALES TESTS ====================

    @Test
    void shouldIngestSalesSuccessfully() {
        SaleEntity sale = new SaleEntity("SALE00000001", "CUST00001", "PROD00001", "SP00001", 2,
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98), Instant.now(), "Online", Instant.now());
        List<SaleEntity> sales = List.of(sale);

        when(saleRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(sales);

        databaseIngestorService.ingestSales();

        verify(kafkaProducerService, times(1)).sendSale(any());
        verify(saleRepository, times(1)).markAsProcessed(anyList());
    }

    @Test
    void shouldSkipIngestWhenNoUnprocessedSales() {
        when(saleRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

        databaseIngestorService.ingestSales();

        verify(kafkaProducerService, never()).sendSale(any());
        verify(saleRepository, never()).markAsProcessed(anyList());
    }

    @Test
    void shouldHandleLargeBatchOfSales() {
        List<SaleEntity> sales = generateSaleEntities(150);

        when(saleRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(sales);

        databaseIngestorService.ingestSales();

        // Should process only BATCH_SIZE (100) at a time
        verify(kafkaProducerService, times(100)).sendSale(any());
        verify(saleRepository, times(1)).markAsProcessed(argThat(list -> list.size() == 100));
    }

    @Test
    void shouldCorrectlyMapSaleReferences() {
        // Test that sales correctly reference customers (FILE_SYSTEM), products (DATABASE), salespeople (WEB_SERVICE)
        SaleEntity sale = new SaleEntity(
            "SALE00000001",
            "CUST00001",  // References FILE_SYSTEM
            "PROD00001",  // References DATABASE
            "SP00001",    // References WEB_SERVICE
            5,
            BigDecimal.valueOf(50.00),
            BigDecimal.valueOf(250.00),
            Instant.now(),
            "Online",
            Instant.now()
        );

        when(saleRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(List.of(sale));

        databaseIngestorService.ingestSales();

        verify(kafkaProducerService).sendSale(argThat(saleDomain ->
            saleDomain.getCustomerId().equals("CUST00001") &&
            saleDomain.getProductId().equals("PROD00001") &&
            saleDomain.getSalespersonId().equals("SP00001") &&
            saleDomain.getDataSource().equals("DATABASE")
        ));
    }

    // ==================== HELPER METHODS ====================

    private List<ProductEntity> generateProductEntities(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> new ProductEntity(
                String.format("PROD%05d", i + 1),
                "Product " + (i + 1),
                "Electronics",
                BigDecimal.valueOf(10 + Math.random() * 990),
                "TestCorp",
                Instant.now()
            ))
            .toList();
    }

    private List<SaleEntity> generateSaleEntities(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> new SaleEntity(
                String.format("SALE%08d", i + 1),
                String.format("CUST%05d", (i % 1000) + 1),     // Cycle through 1,000 customers
                String.format("PROD%05d", (i % 500) + 1),      // Cycle through 500 products
                String.format("SP%05d", (i % 100) + 1),        // Cycle through 100 salespeople
                (i % 20) + 1,
                BigDecimal.valueOf(10 + Math.random() * 990),
                BigDecimal.valueOf((10 + Math.random() * 990) * ((i % 20) + 1)),
                Instant.now(),
                "Online",
                Instant.now()
            ))
            .toList();
    }
}
