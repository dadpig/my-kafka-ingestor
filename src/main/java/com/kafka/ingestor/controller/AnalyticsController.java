package com.kafka.ingestor.controller;

import com.kafka.ingestor.domain.TopSalesByCity;
import com.kafka.ingestor.domain.TopSalesperson;
import com.kafka.ingestor.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/top-sales-by-city")
    public ResponseEntity<List<TopSalesByCity>> getTopSalesByCity(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String country) {

        List<TopSalesByCity> topCities = country != null
            ? analyticsService.getTopSalesByCityInCountry(limit, country)
            : analyticsService.getTopSalesByCity(limit);

        return ResponseEntity.ok(topCities);
    }

    @GetMapping("/top-salespeople")
    public ResponseEntity<List<TopSalesperson>> getTopSalespeople(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city) {

        List<TopSalesperson> topSalespeople;

        if (city != null && country != null) {
            topSalespeople = analyticsService.getTopSalespeopleInCity(limit, city, country);
        } else if (country != null) {
            topSalespeople = analyticsService.getTopSalespeopleInCountry(limit, country);
        } else {
            topSalespeople = analyticsService.getTopSalespeople(limit);
        }

        return ResponseEntity.ok(topSalespeople);
    }

    @GetMapping("/city-stats")
    public ResponseEntity<Object> getCityStatistics(@RequestParam String city, @RequestParam String country) {
        return ResponseEntity.ok(analyticsService.getCityStatistics(city, country));
    }

    @GetMapping("/salesperson-stats")
    public ResponseEntity<Object> getSalespersonStatistics(@RequestParam String salespersonId) {
        return ResponseEntity.ok(analyticsService.getSalespersonStatistics(salespersonId));
    }
}
