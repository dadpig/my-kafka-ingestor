package com.kafka.ingestor.service;

import com.kafka.ingestor.entity.CustomerEntity;
import com.kafka.ingestor.entity.ProductEntity;
import com.kafka.ingestor.entity.SaleEntity;
import com.kafka.ingestor.entity.SalespersonEntity;
import com.kafka.ingestor.repository.CustomerRepository;
import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import com.kafka.ingestor.repository.SalespersonRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseIngestorServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SalespersonRepository salespersonRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    private DatabaseIngestorService databaseIngestorService;

    @BeforeEach
    void setUp() {
        databaseIngestorService = new DatabaseIngestorService(
            customerRepository, productRepository, saleRepository, salespersonRepository, kafkaProducerService);
    }

    @Test
    void shouldIngestCustomersSuccessfully() {
        CustomerEntity customer = new CustomerEntity("CUST001", "Test Customer", "test@example.com",
            "Premium", "North", Instant.now());
        List<CustomerEntity> customers = List.of(customer);

        when(customerRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(customers);

        databaseIngestorService.ingestCustomers();

        verify(kafkaProducerService, times(1)).sendCustomer(any());
        verify(customerRepository, times(1)).markAsProcessed(anyList());
    }

    @Test
    void shouldSkipIngestWhenNoUnprocessedCustomers() {
        when(customerRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

        databaseIngestorService.ingestCustomers();

        verify(kafkaProducerService, never()).sendCustomer(any());
        verify(customerRepository, never()).markAsProcessed(anyList());
    }

    @Test
    void shouldIngestProductsSuccessfully() {
        ProductEntity product = new ProductEntity("PROD001", "Test Product", "Electronics",
            BigDecimal.valueOf(99.99), "TestCorp", Instant.now());
        List<ProductEntity> products = List.of(product);

        when(productRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(products);

        databaseIngestorService.ingestProducts();

        verify(kafkaProducerService, times(1)).sendProduct(any());
        verify(productRepository, times(1)).markAsProcessed(anyList());
    }

    @Test
    void shouldIngestSalesSuccessfully() {
        SaleEntity sale = new SaleEntity("SALE001", "CUST001", "PROD001", "SP001", 2,
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98), Instant.now(), "Online", Instant.now());
        List<SaleEntity> sales = List.of(sale);

        when(saleRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(sales);

        databaseIngestorService.ingestSales();

        verify(kafkaProducerService, times(1)).sendSale(any());
        verify(saleRepository, times(1)).markAsProcessed(anyList());
    }

    @Test
    void shouldIngestSalespersonsSuccessfully() {
        SalespersonEntity salesperson = new SalespersonEntity("SP001", "John Doe", "john@example.com",
            "New York", "USA", Instant.now());
        List<SalespersonEntity> salespersons = List.of(salesperson);

        when(salespersonRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(salespersons);

        databaseIngestorService.ingestSalespersons();

        verify(kafkaProducerService, times(1)).sendSalesperson(any());
        verify(salespersonRepository, times(1)).markAsProcessed(anyList());
    }

    @Test
    void shouldSkipIngestWhenNoUnprocessedSalespersons() {
        when(salespersonRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

        databaseIngestorService.ingestSalespersons();

        verify(kafkaProducerService, never()).sendSalesperson(any());
        verify(salespersonRepository, never()).markAsProcessed(anyList());
    }

    @Test
    void shouldHandleLargeBatchOfCustomers() {
        List<CustomerEntity> customers = generateCustomerEntities(150);

        when(customerRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(customers);

        databaseIngestorService.ingestCustomers();

        verify(kafkaProducerService, times(100)).sendCustomer(any());
        verify(customerRepository, times(1)).markAsProcessed(argThat(list -> list.size() == 100));
    }

    private List<CustomerEntity> generateCustomerEntities(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> new CustomerEntity(
                String.format("CUST%05d", i),
                "Customer " + i,
                "customer" + i + "@example.com",
                "Premium",
                "North",
                Instant.now()
            ))
            .toList();
    }
}
