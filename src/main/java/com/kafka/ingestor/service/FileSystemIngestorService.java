package com.kafka.ingestor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.ingestor.domain.Customer;
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

/**
 * File System Ingestor Service - Refactored Architecture
 * Ingests: Customers (1,000) from JSON files
 * Does NOT ingest: Products (DATABASE) or Salespeople (WEB_SERVICE)
 */
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
        logger.info("📁 FILE_SYSTEM: Ingestor initialized. Watching: {}", watchDirectory);
    }

    /**
     * Scan watch directory for customer JSON files and ingest them
     * Expected file name pattern: customer*.json (e.g., customers-1k.json)
     */
    @Scheduled(fixedDelayString = "${ingestor.filesystem.poll-interval}")
    public void scanAndIngest() {
        try {
            File watchDir = new File(watchDirectory);
            File[] files = watchDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".json") && name.toLowerCase().startsWith("customer"));

            if (files == null || files.length == 0) {
                return;
            }

            Arrays.stream(files)
                .sorted(Comparator.comparing(File::lastModified))
                .forEach(this::processCustomerFile);

        } catch (Exception e) {
            logger.error("❌ FILE_SYSTEM: Error scanning directory: {}", watchDirectory, e);
        }
    }

    private void processCustomerFile(File file) {
        try {
            Customer[] customers = objectMapper.readValue(file, Customer[].class);
            logger.info("📁 FILE_SYSTEM: Processing {} customers from file: {}", customers.length, file.getName());

            Arrays.stream(customers)
                .peek(customer -> customer.setDataSource("FILE_SYSTEM"))
                .forEach(kafkaProducerService::sendCustomer);

            moveToProcessed(file);
            logger.info("✅ FILE_SYSTEM: Processed {} customers from {}", customers.length, file.getName());

        } catch (IOException e) {
            logger.error("❌ FILE_SYSTEM: Error processing customer file: {}", file.getName(), e);
        }
    }

    private void moveToProcessed(File file) throws IOException {
        Path source = file.toPath();
        Path target = Paths.get(processedDirectory, file.getName());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("📦 FILE_SYSTEM: Moved file to processed: {}", file.getName());
    }
}
