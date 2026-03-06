package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.Salesperson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SendResult<String, Object> sendResult;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
        ReflectionTestUtils.setField(kafkaProducerService, "customersTopic", "customers");
        ReflectionTestUtils.setField(kafkaProducerService, "productsTopic", "products");
        ReflectionTestUtils.setField(kafkaProducerService, "salesTopic", "sales");
        ReflectionTestUtils.setField(kafkaProducerService, "salespersonsTopic", "salespersons");
    }

    @Test
    void shouldSendCustomerSuccessfully() {
        Customer customer = new Customer("CUST001", "Test Customer", "test@example.com",
            "Premium", "North", Instant.now());

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq("customers"), eq("CUST001"), any(Customer.class))).thenReturn(future);

        kafkaProducerService.sendCustomer(customer);

        verify(kafkaTemplate, times(1)).send(eq("customers"), eq("CUST001"), eq(customer));
    }

    @Test
    void shouldSendProductSuccessfully() {
        Product product = new Product("PROD001", "Test Product", "Electronics",
            BigDecimal.valueOf(99.99), "TestCorp", Instant.now());

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq("products"), eq("PROD001"), any(Product.class))).thenReturn(future);

        kafkaProducerService.sendProduct(product);

        verify(kafkaTemplate, times(1)).send(eq("products"), eq("PROD001"), eq(product));
    }

    @Test
    void shouldSendSaleSuccessfully() {
        Sale sale = new Sale("SALE001", "CUST001", "PROD001", "SP001", 2,
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98), Instant.now(), "Online");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq("sales"), eq("SALE001"), any(Sale.class))).thenReturn(future);

        kafkaProducerService.sendSale(sale);

        verify(kafkaTemplate, times(1)).send(eq("sales"), eq("SALE001"), eq(sale));
    }

    @Test
    void shouldSendSalespersonSuccessfully() {
        Salesperson salesperson = new Salesperson("SP001", "John Doe", "john@example.com",
            "New York", "USA", Instant.now());

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq("salespersons"), eq("SP001"), any(Salesperson.class))).thenReturn(future);

        kafkaProducerService.sendSalesperson(salesperson);

        verify(kafkaTemplate, times(1)).send(eq("salespersons"), eq("SP001"), eq(salesperson));
    }

    @Test
    void shouldHandleKafkaFailure() {
        Customer customer = new Customer("CUST001", "Test Customer", "test@example.com",
            "Premium", "North", Instant.now());

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(eq("customers"), eq("CUST001"), any(Customer.class))).thenReturn(future);

        kafkaProducerService.sendCustomer(customer);

        verify(kafkaTemplate, times(1)).send(eq("customers"), eq("CUST001"), eq(customer));
    }
}
