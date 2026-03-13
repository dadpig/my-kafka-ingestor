package com.kafka.ingestor.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.ingestor.domain.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Configuration
public class SalesStreamProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SalesStreamProcessor.class);

    @Value("${kafka.topics.customers}")
    private String customersTopic;

    @Value("${kafka.topics.products}")
    private String productsTopic;

    @Value("${kafka.topics.sales}")
    private String salesTopic;

    @Value("${kafka.topics.salespersons}")
    private String salespersonsTopic;

    @Value("${kafka.topics.sales-enriched}")
    private String salesEnrichedTopic;

    @Value("${kafka.topics.sales-aggregation}")
    private String salesAggregationTopic;

    @Value("${kafka.topics.city-aggregation}")
    private String cityAggregationTopic;

    @Value("${kafka.topics.salesperson-aggregation}")
    private String salespersonAggregationTopic;

    @Value("${kafka.topics.sales-enriched-dlq}")
    private String salesEnrichedDlqTopic;

    private final ObjectMapper objectMapper;

    public SalesStreamProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Helper method to create Serde using Jackson ObjectMapper
    private <T> Serde<T> jsonSerde(Class<T> targetType) {
        return Serdes.serdeFrom(new JacksonSerializer<>(), new JacksonDeserializer<>(targetType, objectMapper));
    }

    // Custom Jackson Serializer
    private static class JacksonSerializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper;

        public JacksonSerializer() {
            this.objectMapper = new ObjectMapper();
            this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }

        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing JSON", e);
            }
        }
    }

    // Custom Jackson Deserializer
    private static class JacksonDeserializer<T> implements Deserializer<T> {
        private final Class<T> targetType;
        private final ObjectMapper objectMapper;

        public JacksonDeserializer(Class<T> targetType, ObjectMapper objectMapper) {
            this.targetType = targetType;
            this.objectMapper = objectMapper;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.readValue(data, targetType);
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing JSON", e);
            }
        }
    }

    @Bean
    public KTable<String, Customer> customersTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.table(
            customersTopic,
            Consumed.with(Serdes.String(), jsonSerde(Customer.class)),
            Materialized.<String, Customer, KeyValueStore<Bytes, byte[]>>as("customers-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(jsonSerde(Customer.class))
                .withCachingEnabled()
                .withLoggingEnabled(java.util.Collections.emptyMap())
        );
    }

    @Bean
    public KTable<String, Product> productsTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.table(
            productsTopic,
            Consumed.with(Serdes.String(), jsonSerde(Product.class)),
            Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as("products-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(jsonSerde(Product.class))
                .withCachingEnabled()
                .withLoggingEnabled(java.util.Collections.emptyMap())
        );
    }

    @Bean
    public KTable<String, Salesperson> salespersonsTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.table(
            salespersonsTopic,
            Consumed.with(Serdes.String(), jsonSerde(Salesperson.class)),
            Materialized.<String, Salesperson, KeyValueStore<Bytes, byte[]>>as("salespersons-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(jsonSerde(Salesperson.class))
                .withCachingEnabled()
                .withLoggingEnabled(java.util.Collections.emptyMap())
        );
    }

    @Bean
    public KStream<String, Sale> salesStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(
            salesTopic,
            Consumed.with(Serdes.String(), jsonSerde(Sale.class))
                .withName("sales-source")
        );
    }

    @Bean
    public KStream<String, SalesEnriched> enrichedSalesStream(
            KStream<String, Sale> salesStream,
            KTable<String, Customer> customersTable,
            KTable<String, Product> productsTable,
            KTable<String, Salesperson> salespersonsTable) {

        KStream<String, SalesEnriched> enrichedStream = salesStream
            .selectKey((key, sale) -> sale.getCustomerId(), Named.as("rekey-by-customer"))
            .leftJoin(
                customersTable,
                (sale, customer) -> enrichWithCustomer(sale, customer),
                Joined.with(Serdes.String(), jsonSerde(Sale.class), jsonSerde(Customer.class))
                    .withName("join-customer")
            )
            .selectKey((key, enriched) -> enriched.getProductId(), Named.as("rekey-by-product"))
            .leftJoin(
                productsTable,
                (enriched, product) -> enrichWithProduct(enriched, product),
                Joined.with(Serdes.String(), jsonSerde(SalesEnriched.class), jsonSerde(Product.class))
                    .withName("join-product")
            )
            .selectKey((key, enriched) -> enriched.getSalespersonId(), Named.as("rekey-by-salesperson"))
            .leftJoin(
                salespersonsTable,
                (enriched, salesperson) -> enrichWithSalesperson(enriched, salesperson),
                Joined.with(Serdes.String(), jsonSerde(SalesEnriched.class), jsonSerde(Salesperson.class))
                    .withName("join-salesperson")
            )
            .selectKey((key, enriched) -> enriched.getSaleId(), Named.as("rekey-by-sale-id"));

        // Branch stream into successful and failed enrichments
        KStream<String, SalesEnriched> successfulEnrichments = enrichedStream
            .filter((key, value) -> value != null
                && value.getCustomerName() != null
                && value.getProductName() != null,
                Named.as("filter-successful-enriched"));

        KStream<String, SalesEnriched> failedEnrichments = enrichedStream
            .filter((key, value) -> value == null
                || value.getCustomerName() == null
                || value.getProductName() == null,
                Named.as("filter-failed-enriched"));

        // Send successful enrichments to main topic
        successfulEnrichments.to(
            salesEnrichedTopic,
            Produced.with(Serdes.String(), jsonSerde(SalesEnriched.class))
                .withName("enriched-sales-sink")
        );

        // Send failed enrichments to DLQ
        failedEnrichments.to(
            salesEnrichedDlqTopic,
            Produced.with(Serdes.String(), jsonSerde(SalesEnriched.class))
                .withName("enriched-sales-dlq-sink")
        );

        return successfulEnrichments;
    }

    @Bean
    public KStream<String, SalesAggregation> aggregatedSalesStream(
            KStream<String, SalesEnriched> enrichedSalesStream) {

        KStream<String, SalesAggregation> byRegion = aggregateByDimension(
            enrichedSalesStream,
            SalesEnriched::getCustomerRegion,
            "region",
            "region-aggregation"
        );

        KStream<String, SalesAggregation> bySegment = aggregateByDimension(
            enrichedSalesStream,
            SalesEnriched::getCustomerSegment,
            "segment",
            "segment-aggregation"
        );

        KStream<String, SalesAggregation> byCategory = aggregateByDimension(
            enrichedSalesStream,
            SalesEnriched::getProductCategory,
            "category",
            "category-aggregation"
        );

        KStream<String, SalesAggregation> byChannel = aggregateByDimension(
            enrichedSalesStream,
            SalesEnriched::getChannel,
            "channel",
            "channel-aggregation"
        );

        KStream<String, SalesAggregation> mergedAggregations = byRegion
            .merge(bySegment, Named.as("merge-segment"))
            .merge(byCategory, Named.as("merge-category"))
            .merge(byChannel, Named.as("merge-channel"));

        mergedAggregations.to(
            salesAggregationTopic,
            Produced.with(Serdes.String(), jsonSerde(SalesAggregation.class))
                .withName("aggregation-sink")
        );

        return mergedAggregations;
    }

    @Bean
    public KStream<String, CityAggregation> cityAggregationStream(
            KStream<String, SalesEnriched> enrichedSalesStream) {

        KStream<String, CityAggregation> cityStream = enrichedSalesStream
            .filter((key, value) -> value.getSalespersonCity() != null && value.getSalespersonCountry() != null,
                Named.as("filter-valid-city"))
            .groupBy(
                (key, value) -> value.getSalespersonCity() + "|" + value.getSalespersonCountry(),
                Grouped.<String, SalesEnriched>as("city-grouping")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(jsonSerde(SalesEnriched.class))
            )
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
            .aggregate(
                CityAggregator::new,
                (key, value, aggregate) -> aggregate.add(value),
                Materialized.<String, CityAggregator, WindowStore<Bytes, byte[]>>as("city-aggregation-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(jsonSerde(CityAggregator.class))
                    .withCachingEnabled()
                    .withLoggingEnabled(java.util.Collections.emptyMap())
            )
            .toStream(Named.as("city-to-stream"))
            .map((windowedKey, aggregator) -> {
                String[] parts = windowedKey.key().split("\\|");
                return KeyValue.pair(
                    windowedKey.key(),
                    aggregator.toCityAggregation(parts[0], parts[1])
                );
            }, Named.as("city-map-result"));

        cityStream.to(
            cityAggregationTopic,
            Produced.with(Serdes.String(), jsonSerde(CityAggregation.class))
                .withName("city-aggregation-sink")
        );

        return cityStream;
    }

    @Bean
    public KStream<String, SalespersonAggregation> salespersonAggregationStream(
            KStream<String, SalesEnriched> enrichedSalesStream) {

        KStream<String, SalespersonAggregation> salespersonStream = enrichedSalesStream
            .filter((key, value) -> value.getSalespersonId() != null,
                Named.as("filter-valid-salesperson"))
            .groupBy(
                (key, value) -> value.getSalespersonId(),
                Grouped.<String, SalesEnriched>as("salesperson-grouping")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(jsonSerde(SalesEnriched.class))
            )
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
            .aggregate(
                SalespersonAggregator::new,
                (key, value, aggregate) -> aggregate.add(value),
                Materialized.<String, SalespersonAggregator, WindowStore<Bytes, byte[]>>as("salesperson-aggregation-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(jsonSerde(SalespersonAggregator.class))
                    .withCachingEnabled()
                    .withLoggingEnabled(java.util.Collections.emptyMap())
            )
            .toStream(Named.as("salesperson-to-stream"))
            .map((windowedKey, aggregator) -> KeyValue.pair(
                windowedKey.key(),
                aggregator.toSalespersonAggregation(windowedKey.key())
            ), Named.as("salesperson-map-result"));

        salespersonStream.to(
            salespersonAggregationTopic,
            Produced.with(Serdes.String(), jsonSerde(SalespersonAggregation.class))
                .withName("salesperson-aggregation-sink")
        );

        return salespersonStream;
    }

    private KStream<String, SalesAggregation> aggregateByDimension(
            KStream<String, SalesEnriched> stream,
            KeyExtractor keyExtractor,
            String dimension,
            String storeName) {

        return stream
            .groupBy(
                (key, value) -> keyExtractor.extractKey(value),
                Grouped.<String, SalesEnriched>as(dimension + "-grouping")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(jsonSerde(SalesEnriched.class))
            )
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
            .aggregate(
                SalesAggregator::new,
                (key, value, aggregate) -> aggregate.add(value),
                Materialized.<String, SalesAggregator, WindowStore<Bytes, byte[]>>as(storeName)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(jsonSerde(SalesAggregator.class))
                    .withCachingEnabled()
                    .withLoggingEnabled(java.util.Collections.emptyMap())
            )
            .toStream(Named.as(dimension + "-to-stream"))
            .map((windowedKey, aggregator) -> KeyValue.pair(
                windowedKey.key(),
                aggregator.toSalesAggregation(windowedKey.key(), dimension)
            ), Named.as(dimension + "-map-result"));
    }

    private SalesEnriched enrichWithCustomer(Sale sale, Customer customer) {
        SalesEnriched enriched = new SalesEnriched();
        enriched.setSaleId(sale.getSaleId());
        enriched.setCustomerId(sale.getCustomerId());
        enriched.setProductId(sale.getProductId());
        enriched.setSalespersonId(sale.getSalespersonId());
        enriched.setQuantity(sale.getQuantity());
        enriched.setUnitPrice(sale.getUnitPrice());
        enriched.setTotalAmount(sale.getTotalAmount());
        enriched.setSaleDate(sale.getSaleDate());
        enriched.setChannel(sale.getChannel());
        enriched.setSaleDataSource(sale.getDataSource());

        if (customer != null) {
            enriched.setCustomerName(customer.getName());
            enriched.setCustomerSegment(customer.getSegment());
            enriched.setCustomerRegion(customer.getRegion());
            enriched.setCustomerDataSource(customer.getDataSource());
        }

        return enriched;
    }

    private SalesEnriched enrichWithProduct(SalesEnriched enriched, Product product) {
        if (product != null) {
            enriched.setProductName(product.getName());
            enriched.setProductCategory(product.getCategory());
            enriched.setProductDataSource(product.getDataSource());
        }
        return enriched;
    }

    private SalesEnriched enrichWithSalesperson(SalesEnriched enriched, Salesperson salesperson) {
        if (salesperson != null) {
            enriched.setSalespersonName(salesperson.getName());
            enriched.setSalespersonCity(salesperson.getCity());
            enriched.setSalespersonCountry(salesperson.getCountry());
            enriched.setSalespersonDataSource(salesperson.getDataSource());
        }
        return enriched;
    }

    @FunctionalInterface
    private interface KeyExtractor {
        String extractKey(SalesEnriched enriched);
    }

    public static class SalesAggregator {
        private long count = 0L;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private int totalQuantity = 0;

        public SalesAggregator() {
        }

        public SalesAggregator add(SalesEnriched sale) {
            this.count++;
            this.totalRevenue = this.totalRevenue.add(sale.getTotalAmount());
            this.totalQuantity += sale.getQuantity();
            return this;
        }

        public SalesAggregation toSalesAggregation(String key, String dimension) {
            BigDecimal avgOrderValue = count > 0
                ? totalRevenue.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            return new SalesAggregation(
                key,
                count,
                totalRevenue,
                totalQuantity,
                avgOrderValue,
                dimension
            );
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
    }

    public static class CityAggregator {
        private String city;
        private String country;
        private long salesCount = 0L;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private int totalQuantity = 0;

        public CityAggregator() {
        }

        public CityAggregator add(SalesEnriched sale) {
            this.city = sale.getSalespersonCity();
            this.country = sale.getSalespersonCountry();
            this.salesCount++;
            this.totalRevenue = this.totalRevenue.add(sale.getTotalAmount());
            this.totalQuantity += sale.getQuantity();
            return this;
        }

        public CityAggregation toCityAggregation(String city, String country) {
            return new CityAggregation(city, country, salesCount, totalRevenue, totalQuantity);
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public long getSalesCount() {
            return salesCount;
        }

        public void setSalesCount(long salesCount) {
            this.salesCount = salesCount;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
    }

    public static class SalespersonAggregator {
        private String salespersonId;
        private String salespersonName;
        private String city;
        private String country;
        private long salesCount = 0L;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private int totalQuantity = 0;

        public SalespersonAggregator() {
        }

        public SalespersonAggregator add(SalesEnriched sale) {
            this.salespersonId = sale.getSalespersonId();
            this.salespersonName = sale.getSalespersonName();
            this.city = sale.getSalespersonCity();
            this.country = sale.getSalespersonCountry();
            this.salesCount++;
            this.totalRevenue = this.totalRevenue.add(sale.getTotalAmount());
            this.totalQuantity += sale.getQuantity();
            return this;
        }

        public SalespersonAggregation toSalespersonAggregation(String salespersonId) {
            return new SalespersonAggregation(
                salespersonId,
                salespersonName,
                city,
                country,
                salesCount,
                totalRevenue,
                totalQuantity
            );
        }

        public String getSalespersonId() {
            return salespersonId;
        }

        public void setSalespersonId(String salespersonId) {
            this.salespersonId = salespersonId;
        }

        public String getSalespersonName() {
            return salespersonName;
        }

        public void setSalespersonName(String salespersonName) {
            this.salespersonName = salespersonName;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public long getSalesCount() {
            return salesCount;
        }

        public void setSalesCount(long salesCount) {
            this.salesCount = salesCount;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
    }
}
