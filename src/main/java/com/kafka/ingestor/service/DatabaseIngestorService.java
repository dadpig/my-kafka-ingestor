package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.Salesperson;
import com.kafka.ingestor.entity.CustomerEntity;
import com.kafka.ingestor.entity.ProductEntity;
import com.kafka.ingestor.entity.SaleEntity;
import com.kafka.ingestor.entity.SalespersonEntity;
import com.kafka.ingestor.repository.CustomerRepository;
import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import com.kafka.ingestor.repository.SalespersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "ingestor.database", name = "enabled", havingValue = "true")
public class DatabaseIngestorService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIngestorService.class);
    private static final int BATCH_SIZE = 100;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final SalespersonRepository salespersonRepository;
    private final KafkaProducerService kafkaProducerService;

    public DatabaseIngestorService(CustomerRepository customerRepository,
                                  ProductRepository productRepository,
                                  SaleRepository saleRepository,
                                  SalespersonRepository salespersonRepository,
                                  KafkaProducerService kafkaProducerService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.salespersonRepository = salespersonRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedDelayString = "${ingestor.database.poll-interval}")
    @Transactional
    public void ingestCustomers() {
        List<CustomerEntity> unprocessed = customerRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (unprocessed.isEmpty()) {
            return;
        }

        logger.info("Ingesting {} customers from database", unprocessed.size());

        unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(this::toCustomerDomain)
            .forEach(kafkaProducerService::sendCustomer);

        List<String> processedIds = unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(CustomerEntity::getCustomerId)
            .toList();

        customerRepository.markAsProcessed(processedIds);
        logger.info("Marked {} customers as processed", processedIds.size());
    }

    @Scheduled(fixedDelayString = "${ingestor.database.poll-interval}")
    @Transactional
    public void ingestProducts() {
        List<ProductEntity> unprocessed = productRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (unprocessed.isEmpty()) {
            return;
        }

        logger.info("Ingesting {} products from database", unprocessed.size());

        unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(this::toProductDomain)
            .forEach(kafkaProducerService::sendProduct);

        List<String> processedIds = unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(ProductEntity::getProductId)
            .toList();

        productRepository.markAsProcessed(processedIds);
        logger.info("Marked {} products as processed", processedIds.size());
    }

    @Scheduled(fixedDelayString = "${ingestor.database.poll-interval}")
    @Transactional
    public void ingestSales() {
        List<SaleEntity> unprocessed = saleRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (unprocessed.isEmpty()) {
            return;
        }

        logger.info("Ingesting {} sales from database", unprocessed.size());

        unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(this::toSaleDomain)
            .forEach(kafkaProducerService::sendSale);

        List<String> processedIds = unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(SaleEntity::getSaleId)
            .toList();

        saleRepository.markAsProcessed(processedIds);
        logger.info("Marked {} sales as processed", processedIds.size());
    }

    @Scheduled(fixedDelayString = "${ingestor.database.poll-interval}")
    @Transactional
    public void ingestSalespersons() {
        List<SalespersonEntity> unprocessed = salespersonRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (unprocessed.isEmpty()) {
            return;
        }

        logger.info("Ingesting {} salespersons from database", unprocessed.size());

        unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(this::toSalespersonDomain)
            .forEach(kafkaProducerService::sendSalesperson);

        List<String> processedIds = unprocessed.stream()
            .limit(BATCH_SIZE)
            .map(SalespersonEntity::getSalespersonId)
            .toList();

        salespersonRepository.markAsProcessed(processedIds);
        logger.info("Marked {} salespersons as processed", processedIds.size());
    }

    private Customer toCustomerDomain(CustomerEntity entity) {
        return new Customer(
            entity.getCustomerId(),
            entity.getName(),
            entity.getEmail(),
            entity.getSegment(),
            entity.getRegion(),
            entity.getCreatedAt()
        );
    }

    private Product toProductDomain(ProductEntity entity) {
        return new Product(
            entity.getProductId(),
            entity.getName(),
            entity.getCategory(),
            entity.getPrice(),
            entity.getManufacturer(),
            entity.getCreatedAt()
        );
    }

    private Sale toSaleDomain(SaleEntity entity) {
        return new Sale(
            entity.getSaleId(),
            entity.getCustomerId(),
            entity.getProductId(),
            entity.getSalespersonId(),
            entity.getQuantity(),
            entity.getUnitPrice(),
            entity.getTotalAmount(),
            entity.getSaleDate(),
            entity.getChannel()
        );
    }

    private Salesperson toSalespersonDomain(SalespersonEntity entity) {
        return new Salesperson(
            entity.getSalespersonId(),
            entity.getName(),
            entity.getEmail(),
            entity.getCity(),
            entity.getCountry(),
            entity.getCreatedAt()
        );
    }
}
