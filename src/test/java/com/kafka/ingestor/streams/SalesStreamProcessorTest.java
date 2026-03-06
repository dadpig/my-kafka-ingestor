package com.kafka.ingestor.streams;

import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.Salesperson;
import com.kafka.ingestor.domain.SalesEnriched;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SalesStreamProcessorTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Customer> customersTopic;
    private TestInputTopic<String, Product> productsTopic;
    private TestInputTopic<String, Salesperson> salespersonsTopic;
    private TestInputTopic<String, Sale> salesTopic;
    private TestOutputTopic<String, SalesEnriched> enrichedTopic;

    @BeforeEach
    void setUp() {
        SalesStreamProcessor processor = new SalesStreamProcessor();
        setProcessorFields(processor);

        StreamsBuilder builder = new StreamsBuilder();
        var customersTable = processor.customersTable(builder);
        var productsTable = processor.productsTable(builder);
        var salespersonsTable = processor.salespersonsTable(builder);
        KStream<String, Sale> salesStream = processor.salesStream(builder);
        processor.enrichedSalesStream(
            salesStream,
            customersTable,
            productsTable,
            salespersonsTable
        );

        Topology topology = builder.build();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);

        testDriver = new TopologyTestDriver(topology, config);

        customersTopic = testDriver.createInputTopic(
            "customers",
            Serdes.String().serializer(),
            new JsonSerde<>(Customer.class).serializer()
        );

        productsTopic = testDriver.createInputTopic(
            "products",
            Serdes.String().serializer(),
            new JsonSerde<>(Product.class).serializer()
        );

        salespersonsTopic = testDriver.createInputTopic(
            "salespersons",
            Serdes.String().serializer(),
            new JsonSerde<>(Salesperson.class).serializer()
        );

        salesTopic = testDriver.createInputTopic(
            "sales",
            Serdes.String().serializer(),
            new JsonSerde<>(Sale.class).serializer()
        );

        enrichedTopic = testDriver.createOutputTopic(
            "sales-enriched",
            Serdes.String().deserializer(),
            new JsonSerde<>(SalesEnriched.class).deserializer()
        );
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void shouldEnrichSaleWithCustomerAndProduct() {
        Customer customer = new Customer("CUST001", "Test Customer", "test@example.com",
            "Premium", "North", Instant.now());
        Product product = new Product("PROD001", "Test Product", "Electronics",
            BigDecimal.valueOf(99.99), "TestCorp", Instant.now());
        Salesperson salesperson = new Salesperson("SP001", "John Smith", "john.smith@salesforce.com",
            "New York", "USA", Instant.now());
        Sale sale = new Sale("SALE001", "CUST001", "PROD001", "SP001", 2,
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98), Instant.now(), "Online");

        customersTopic.pipeInput("CUST001", customer);
        productsTopic.pipeInput("PROD001", product);
        salespersonsTopic.pipeInput("SP001", salesperson);
        salesTopic.pipeInput("SALE001", sale);

        assertFalse(enrichedTopic.isEmpty());
        TestRecord<String, SalesEnriched> result = enrichedTopic.readRecord();

        assertNotNull(result);
        assertEquals("SALE001", result.getKey());
        assertEquals("Test Customer", result.getValue().getCustomerName());
        assertEquals("Premium", result.getValue().getCustomerSegment());
        assertEquals("Test Product", result.getValue().getProductName());
        assertEquals("Electronics", result.getValue().getProductCategory());
        assertEquals("John Smith", result.getValue().getSalespersonName());
        assertEquals("New York", result.getValue().getSalespersonCity());
        assertEquals("USA", result.getValue().getSalespersonCountry());
    }

    @Test
    void shouldHandleSaleWithMissingCustomer() {
        Product product = new Product("PROD001", "Test Product", "Electronics",
            BigDecimal.valueOf(99.99), "TestCorp", Instant.now());
        Salesperson salesperson = new Salesperson("SP001", "John Smith", "john.smith@salesforce.com",
            "New York", "USA", Instant.now());
        Sale sale = new Sale("SALE001", "CUST999", "PROD001", "SP001", 2,
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98), Instant.now(), "Online");

        productsTopic.pipeInput("PROD001", product);
        salespersonsTopic.pipeInput("SP001", salesperson);
        salesTopic.pipeInput("SALE001", sale);

        assertFalse(enrichedTopic.isEmpty());
        TestRecord<String, SalesEnriched> result = enrichedTopic.readRecord();

        assertNotNull(result);
        assertNull(result.getValue().getCustomerName());
        assertEquals("Test Product", result.getValue().getProductName());
        assertEquals("John Smith", result.getValue().getSalespersonName());
    }

    @Test
    void shouldHandleSaleWithMissingProduct() {
        Customer customer = new Customer("CUST001", "Test Customer", "test@example.com",
            "Premium", "North", Instant.now());
        Salesperson salesperson = new Salesperson("SP001", "John Smith", "john.smith@salesforce.com",
            "New York", "USA", Instant.now());
        Sale sale = new Sale("SALE001", "CUST001", "PROD999", "SP001", 2,
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98), Instant.now(), "Online");

        customersTopic.pipeInput("CUST001", customer);
        salespersonsTopic.pipeInput("SP001", salesperson);
        salesTopic.pipeInput("SALE001", sale);

        assertFalse(enrichedTopic.isEmpty());
        TestRecord<String, SalesEnriched> result = enrichedTopic.readRecord();

        assertNotNull(result);
        assertEquals("Test Customer", result.getValue().getCustomerName());
        assertNull(result.getValue().getProductName());
        assertEquals("John Smith", result.getValue().getSalespersonName());
    }

    private void setProcessorFields(SalesStreamProcessor processor) {
        try {
            java.lang.reflect.Field customersTopicField = SalesStreamProcessor.class.getDeclaredField("customersTopic");
            customersTopicField.setAccessible(true);
            customersTopicField.set(processor, "customers");

            java.lang.reflect.Field productsTopicField = SalesStreamProcessor.class.getDeclaredField("productsTopic");
            productsTopicField.setAccessible(true);
            productsTopicField.set(processor, "products");

            java.lang.reflect.Field salespersonsTopicField = SalesStreamProcessor.class.getDeclaredField("salespersonsTopic");
            salespersonsTopicField.setAccessible(true);
            salespersonsTopicField.set(processor, "salespersons");

            java.lang.reflect.Field salesTopicField = SalesStreamProcessor.class.getDeclaredField("salesTopic");
            salesTopicField.setAccessible(true);
            salesTopicField.set(processor, "sales");

            java.lang.reflect.Field enrichedTopicField = SalesStreamProcessor.class.getDeclaredField("salesEnrichedTopic");
            enrichedTopicField.setAccessible(true);
            enrichedTopicField.set(processor, "sales-enriched");

            java.lang.reflect.Field aggTopicField = SalesStreamProcessor.class.getDeclaredField("salesAggregationTopic");
            aggTopicField.setAccessible(true);
            aggTopicField.set(processor, "sales-aggregation");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set processor fields", e);
        }
    }
}
