package com.kafka.ingestor.service;

import com.kafka.ingestor.domain.CityAggregation;
import com.kafka.ingestor.domain.SalespersonAggregation;
import com.kafka.ingestor.domain.TopSalesByCity;
import com.kafka.ingestor.domain.TopSalesperson;
import com.kafka.ingestor.streams.SalesStreamProcessor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public AnalyticsService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    @Cacheable(value = "topCitiesCache", key = "#limit")
    public List<TopSalesByCity> getTopSalesByCity(int limit) {
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            if (kafkaStreams == null) {
                logger.warn("KafkaStreams not initialized");
                return Collections.emptyList();
            }

            ReadOnlyWindowStore<String, SalesStreamProcessor.CityAggregator> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                    "city-aggregation-store",
                    QueryableStoreTypes.windowStore()
                )
            );

            Map<String, SalesStreamProcessor.CityAggregator> latestValues = new HashMap<>();
            Instant now = Instant.now();
            Instant from = now.minusSeconds(2592000); // Last 30 days

            store.fetchAll(from, now).forEachRemaining(keyValue -> {
                String key = keyValue.key.key();
                SalesStreamProcessor.CityAggregator value = keyValue.value;
                if (!latestValues.containsKey(key) ||
                    latestValues.get(key).getSalesCount() < value.getSalesCount()) {
                    latestValues.put(key, value);
                }
            });

            List<TopSalesByCity> sortedCities = latestValues.values().stream()
                .map(agg -> new TopSalesByCity(
                    agg.getCity(),
                    agg.getCountry(),
                    agg.getSalesCount(),
                    agg.getTotalRevenue(),
                    0L
                ))
                .sorted((a, b) -> {
                    int salesCompare = Long.compare(b.getTotalSales(), a.getTotalSales());
                    if (salesCompare != 0) {
                        return salesCompare;
                    }
                    return b.getTotalRevenue().compareTo(a.getTotalRevenue());
                })
                .limit(limit)
                .collect(Collectors.toList());

            // Assign ranks properly
            for (int i = 0; i < sortedCities.size(); i++) {
                sortedCities.get(i).setRank(i + 1L);
            }

            return sortedCities;

        } catch (Exception e) {
            logger.error("Error getting top sales by city", e);
            return Collections.emptyList();
        }
    }

    public List<TopSalesByCity> getTopSalesByCityInCountry(int limit, String country) {
        return getTopSalesByCity(1000).stream()
            .filter(city -> country.equalsIgnoreCase(city.getCountry()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "topSalespeopleCache", key = "#limit")
    public List<TopSalesperson> getTopSalespeople(int limit) {
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            if (kafkaStreams == null) {
                logger.warn("KafkaStreams not initialized");
                return Collections.emptyList();
            }

            ReadOnlyWindowStore<String, SalesStreamProcessor.SalespersonAggregator> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                    "salesperson-aggregation-store",
                    QueryableStoreTypes.windowStore()
                )
            );

            Map<String, SalesStreamProcessor.SalespersonAggregator> latestValues = new HashMap<>();
            Instant now = Instant.now();
            Instant from = now.minusSeconds(2592000); // Last 30 days

            store.fetchAll(from, now).forEachRemaining(keyValue -> {
                String key = keyValue.key.key();
                SalesStreamProcessor.SalespersonAggregator value = keyValue.value;
                if (!latestValues.containsKey(key) ||
                    latestValues.get(key).getSalesCount() < value.getSalesCount()) {
                    latestValues.put(key, value);
                }
            });

            List<TopSalesperson> sortedSalespeople = latestValues.values().stream()
                .map(agg -> new TopSalesperson(
                    agg.getSalespersonId(),
                    agg.getSalespersonName(),
                    agg.getCity(),
                    agg.getCountry(),
                    agg.getSalesCount(),
                    agg.getTotalRevenue(),
                    0L
                ))
                .sorted((a, b) -> {
                    int salesCompare = Long.compare(b.getTotalSales(), a.getTotalSales());
                    if (salesCompare != 0) {
                        return salesCompare;
                    }
                    return b.getTotalRevenue().compareTo(a.getTotalRevenue());
                })
                .limit(limit)
                .collect(Collectors.toList());

            // Assign ranks properly
            for (int i = 0; i < sortedSalespeople.size(); i++) {
                sortedSalespeople.get(i).setRank(i + 1L);
            }

            return sortedSalespeople;

        } catch (Exception e) {
            logger.error("Error getting top salespeople", e);
            return Collections.emptyList();
        }
    }

    public List<TopSalesperson> getTopSalespeopleInCountry(int limit, String country) {
        return getTopSalespeople(1000).stream()
            .filter(sp -> country.equalsIgnoreCase(sp.getCountry()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<TopSalesperson> getTopSalespeopleInCity(int limit, String city, String country) {
        return getTopSalespeople(1000).stream()
            .filter(sp -> city.equalsIgnoreCase(sp.getCity()) && country.equalsIgnoreCase(sp.getCountry()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public Map<String, Object> getCityStatistics(String city, String country) {
        List<TopSalesByCity> cities = getTopSalesByCity(1000);
        Optional<TopSalesByCity> cityStats = cities.stream()
            .filter(c -> city.equalsIgnoreCase(c.getCity()) && country.equalsIgnoreCase(c.getCountry()))
            .findFirst();

        Map<String, Object> result = new HashMap<>();
        if (cityStats.isPresent()) {
            TopSalesByCity stats = cityStats.get();
            result.put("city", stats.getCity());
            result.put("country", stats.getCountry());
            result.put("totalSales", stats.getTotalSales());
            result.put("totalRevenue", stats.getTotalRevenue());
            result.put("rank", stats.getRank());
        } else {
            result.put("error", "City not found or no data available");
        }
        return result;
    }

    public Map<String, Object> getSalespersonStatistics(String salespersonId) {
        List<TopSalesperson> salespeople = getTopSalespeople(1000);
        Optional<TopSalesperson> spStats = salespeople.stream()
            .filter(sp -> salespersonId.equals(sp.getSalespersonId()))
            .findFirst();

        Map<String, Object> result = new HashMap<>();
        if (spStats.isPresent()) {
            TopSalesperson stats = spStats.get();
            result.put("salespersonId", stats.getSalespersonId());
            result.put("salespersonName", stats.getSalespersonName());
            result.put("city", stats.getCity());
            result.put("country", stats.getCountry());
            result.put("totalSales", stats.getTotalSales());
            result.put("totalRevenue", stats.getTotalRevenue());
            result.put("rank", stats.getRank());
        } else {
            result.put("error", "Salesperson not found or no data available");
        }
        return result;
    }
}
