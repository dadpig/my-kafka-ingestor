package com.kafka.ingestor.mock;

import com.kafka.ingestor.domain.Customer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/customers")
@ConditionalOnProperty(prefix = "mock.webservice", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MockWebServiceController {

    private static final Random random = new Random();
    private static final String[] SEGMENTS = {"Premium", "Standard", "Basic"};
    private static final String[] REGIONS = {"North", "South", "East", "West", "Central"};
    private final AtomicLong customerCounter = new AtomicLong(50000);

    @GetMapping
    public List<Customer> getCustomers(@RequestParam(defaultValue = "10") int limit) {
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            long id = customerCounter.incrementAndGet();
            Customer customer = new Customer();
            customer.setCustomerId(String.format("CUST%05d", id));
            customer.setName(generateCustomerName(id));
            customer.setEmail(String.format("customer%d@example.com", id));
            customer.setSegment(SEGMENTS[random.nextInt(SEGMENTS.length)]);
            customer.setRegion(REGIONS[random.nextInt(REGIONS.length)]);
            customer.setCreatedAt(Instant.now()
                .minus(random.nextInt(365), ChronoUnit.DAYS)
                .minus(random.nextInt(24), ChronoUnit.HOURS));
            customer.setDataSource("WEB_SERVICE");
            customers.add(customer);
        }

        return customers;
    }

    @GetMapping("/batch")
    public List<Customer> getCustomersBatch(@RequestParam(defaultValue = "100") int batchSize) {
        return getCustomers(batchSize);
    }

    @GetMapping("/segment")
    public List<Customer> getCustomersBySegment(@RequestParam String segment,
                                                @RequestParam(defaultValue = "10") int limit) {
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            long id = customerCounter.incrementAndGet();
            Customer customer = new Customer();
            customer.setCustomerId(String.format("CUST%05d", id));
            customer.setName(generateCustomerName(id));
            customer.setEmail(String.format("customer%d@example.com", id));
            customer.setSegment(segment);
            customer.setRegion(REGIONS[random.nextInt(REGIONS.length)]);
            customer.setCreatedAt(Instant.now()
                .minus(random.nextInt(365), ChronoUnit.DAYS)
                .minus(random.nextInt(24), ChronoUnit.HOURS));
            customer.setDataSource("WEB_SERVICE");
            customers.add(customer);
        }

        return customers;
    }

    private String generateCustomerName(long id) {
        String[] firstNames = {"John", "Jane", "Michael", "Emily", "David", "Sarah", "James", "Maria", "Robert", "Linda"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"};

        return firstNames[(int)(id % firstNames.length)] + " " + lastNames[(int)((id / 10) % lastNames.length)];
    }
}
