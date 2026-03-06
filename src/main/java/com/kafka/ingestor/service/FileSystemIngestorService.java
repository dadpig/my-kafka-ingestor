package com.kafka.ingestor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.Salesperson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;

@Service
@ConditionalOnProperty(prefix = "ingestor.filesystem", name = "enabled", havingValue = "true")
public class FileSystemIngestorService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemIngestorService.class);

    private final ObjectMapper objectMapper;
    private final KafkaProducerService kafkaProducerService;

    @Value("${ingestor.filesystem.watch-directory}")
    private String watchDirectory;

    @Value("${ingestor.filesystem.processed-directory}")
    private String processedDirectory;

    public FileSystemIngestorService(ObjectMapper objectMapper, KafkaProducerService kafkaProducerService) {
        this.objectMapper = objectMapper;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(watchDirectory));
        Files.createDirectories(Paths.get(processedDirectory));
        logger.info("File system ingestor initialized. Watching: {}", watchDirectory);
    }

    @Scheduled(fixedDelayString = "${ingestor.filesystem.poll-interval}")
    public void scanAndIngest() {
        try {
            File watchDir = new File(watchDirectory);
            File[] files = watchDir.listFiles((dir, name) -> name.endsWith(".json"));

            if (files == null || files.length == 0) {
                return;
            }

            Arrays.stream(files)
                .sorted(Comparator.comparing(File::lastModified))
                .forEach(this::processFile);

        } catch (Exception e) {
            logger.error("Error scanning directory: {}", watchDirectory, e);
        }
    }

    private void processFile(File file) {
        try {
            String fileName = file.getName().toLowerCase();

            if (fileName.startsWith("customer")) {
                processCustomerFile(file);
            } else if (fileName.startsWith("product")) {
                processProductFile(file);
            } else if (fileName.startsWith("sale")) {
                processSaleFile(file);
            } else if (fileName.startsWith("salesperson")) {
                processSalespersonFile(file);
            } else {
                logger.warn("Unknown file type: {}", fileName);
            }

            moveToProcessed(file);

        } catch (Exception e) {
            logger.error("Error processing file: {}", file.getName(), e);
        }
    }

    private void processCustomerFile(File file) throws IOException {
        Customer[] customers = objectMapper.readValue(file, Customer[].class);
        logger.info("Processing {} customers from file: {}", customers.length, file.getName());

        Arrays.stream(customers).forEach(kafkaProducerService::sendCustomer);
    }

    private void processProductFile(File file) throws IOException {
        Product[] products = objectMapper.readValue(file, Product[].class);
        logger.info("Processing {} products from file: {}", products.length, file.getName());

        Arrays.stream(products).forEach(kafkaProducerService::sendProduct);
    }

    private void processSaleFile(File file) throws IOException {
        Sale[] sales = objectMapper.readValue(file, Sale[].class);
        logger.info("Processing {} sales from file: {}", sales.length, file.getName());

        Arrays.stream(sales).forEach(kafkaProducerService::sendSale);
    }

    private void processSalespersonFile(File file) throws IOException {
        Salesperson[] salespersons = objectMapper.readValue(file, Salesperson[].class);
        logger.info("Processing {} salespersons from file: {}", salespersons.length, file.getName());

        Arrays.stream(salespersons).forEach(kafkaProducerService::sendSalesperson);
    }

    private void moveToProcessed(File file) throws IOException {
        Path source = file.toPath();
        Path target = Paths.get(processedDirectory, file.getName());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("Moved file to processed: {}", file.getName());
    }
}
