package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.Salesperson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.customers}")
    private String customersTopic;

    @Value("${kafka.topics.products}")
    private String productsTopic;

    @Value("${kafka.topics.sales}")
    private String salesTopic;

    @Value("${kafka.topics.salespersons}")
    private String salespersonsTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCustomer(Customer customer) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(
                customersTopic,
                customer.getCustomerId(),
                customer
            ).get(); // Make synchronous for transactional integrity

            if (result != null && result.getRecordMetadata() != null) {
                logger.debug("Customer sent: {} to partition: {}",
                    customer.getCustomerId(), result.getRecordMetadata().partition());
            }
        } catch (Exception ex) {
            logger.error("Failed to send customer: {}", customer.getCustomerId(), ex);
            throw new RuntimeException("Failed to send customer to Kafka", ex);
        }
    }

    public void sendProduct(Product product) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(
                productsTopic,
                product.getProductId(),
                product
            ).get(); // Make synchronous for transactional integrity

            if (result != null && result.getRecordMetadata() != null) {
                logger.debug("Product sent: {} to partition: {}",
                    product.getProductId(), result.getRecordMetadata().partition());
            }
        } catch (Exception ex) {
            logger.error("Failed to send product: {}", product.getProductId(), ex);
            throw new RuntimeException("Failed to send product to Kafka", ex);
        }
    }

    public void sendSale(Sale sale) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(
                salesTopic,
                sale.getSaleId(),
                sale
            ).get(); // Make synchronous for transactional integrity

            if (result != null && result.getRecordMetadata() != null) {
                logger.debug("Sale sent: {} to partition: {}",
                    sale.getSaleId(), result.getRecordMetadata().partition());
            }
        } catch (Exception ex) {
            logger.error("Failed to send sale: {}", sale.getSaleId(), ex);
            throw new RuntimeException("Failed to send sale to Kafka", ex);
        }
    }

    public void sendSalesperson(Salesperson salesperson) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(
                salespersonsTopic,
                salesperson.getSalespersonId(),
                salesperson
            ).get(); // Make synchronous for transactional integrity

            if (result != null && result.getRecordMetadata() != null) {
                logger.debug("Salesperson sent: {} to partition: {}",
                    salesperson.getSalespersonId(), result.getRecordMetadata().partition());
            }
        } catch (Exception ex) {
            logger.error("Failed to send salesperson: {}", salesperson.getSalespersonId(), ex);
            throw new RuntimeException("Failed to send salesperson to Kafka", ex);
        }
    }
}
