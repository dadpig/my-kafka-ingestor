package com.kafka.ingestor.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final Random random = new Random();

    private static final String[] REGIONS = {"North", "South", "East", "West", "Central"};
    private static final String[] SEGMENTS = {"Premium", "Standard", "Basic"};
    private static final String[] CATEGORIES = {"Electronics", "Clothing", "Food", "Books", "Home"};
    private static final String[] MANUFACTURERS = {"TechCorp", "FashionHub", "FoodCo", "BookWorld", "HomeStyle"};
    private static final String[] CHANNELS = {"Online", "Store", "Mobile", "Partner"};

    // City and Country data for salesperson generation
    private static final String[][] CITIES_BY_COUNTRY = {
        {"USA", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix"},
        {"Canada", "Toronto", "Vancouver", "Montreal", "Calgary"},
        {"UK", "London", "Manchester", "Birmingham", "Leeds"},
        {"Germany", "Berlin", "Munich", "Frankfurt", "Hamburg"},
        {"France", "Paris", "Lyon", "Marseille", "Toulouse"},
        {"Spain", "Madrid", "Barcelona", "Valencia", "Seville"},
        {"Italy", "Rome", "Milan", "Naples", "Turin"}
    };

    private static final String[] FIRST_NAMES = {
        "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
        "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson"
    };

    @Bean
    public CommandLineRunner initializeData(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            SalespersonRepository salespersonRepository,
            SaleRepository saleRepository) {

        return args -> {
            logger.info("Initializing sample data...");

            // Create customers first (referenced by sales)
            List<CustomerEntity> customers = generateCustomers(50);
            customerRepository.saveAll(customers);
            logger.info("Created {} customers", customers.size());

            // Create products (referenced by sales)
            List<ProductEntity> products = generateProducts(30);
            productRepository.saveAll(products);
            logger.info("Created {} products", products.size());

            // Create salespersons (referenced by sales) - IMPORTANT: Before sales!
            List<SalespersonEntity> salespersons = generateSalespersons(20);
            salespersonRepository.saveAll(salespersons);
            logger.info("Created {} salespersons across {} cities in {} countries",
                    salespersons.size(),
                    salespersons.stream().map(SalespersonEntity::getCity).distinct().count(),
                    salespersons.stream().map(SalespersonEntity::getCountry).distinct().count());

            // Create sales (references customers, products, and salespersons)
            List<SaleEntity> sales = generateSales(customers, products, salespersons, 100);
            saleRepository.saveAll(sales);
            logger.info("Created {} sales", sales.size());

            logger.info("Sample data initialization completed");
        };
    }

    private List<CustomerEntity> generateCustomers(int count) {
        List<CustomerEntity> customers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            CustomerEntity customer = new CustomerEntity();
            customer.setCustomerId("CUST" + String.format("%05d", i));
            customer.setName("Customer " + i);
            customer.setEmail("customer" + i + "@example.com");
            customer.setSegment(SEGMENTS[random.nextInt(SEGMENTS.length)]);
            customer.setRegion(REGIONS[random.nextInt(REGIONS.length)]);
            customer.setCreatedAt(Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS));
            customers.add(customer);
        }
        return customers;
    }

    private List<ProductEntity> generateProducts(int count) {
        List<ProductEntity> products = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ProductEntity product = new ProductEntity();
            product.setProductId("PROD" + String.format("%05d", i));
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            product.setName(category + " Product " + i);
            product.setCategory(category);
            product.setPrice(BigDecimal.valueOf(10 + random.nextDouble() * 990).setScale(2, BigDecimal.ROUND_HALF_UP));
            product.setManufacturer(MANUFACTURERS[random.nextInt(MANUFACTURERS.length)]);
            product.setCreatedAt(Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS));
            products.add(product);
        }
        return products;
    }

    private List<SalespersonEntity> generateSalespersons(int count) {
        List<SalespersonEntity> salespersons = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            SalespersonEntity salesperson = new SalespersonEntity();
            salesperson.setSalespersonId("SP" + String.format("%05d", i));

            // Generate realistic name
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            salesperson.setName(firstName + " " + lastName);

            // Assign random city and country
            String[] countryData = CITIES_BY_COUNTRY[random.nextInt(CITIES_BY_COUNTRY.length)];
            salesperson.setCountry(countryData[0]);
            salesperson.setCity(countryData[1 + random.nextInt(countryData.length - 1)]);

            // Generate email
            salesperson.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@salesforce.com");

            // Set timestamps
            salesperson.setCreatedAt(Instant.now().minus(random.nextInt(730), ChronoUnit.DAYS));

            salespersons.add(salesperson);
        }
        return salespersons;
    }

    private List<SaleEntity> generateSales(List<CustomerEntity> customers, List<ProductEntity> products,
                                           List<SalespersonEntity> salespersons, int count) {
        List<SaleEntity> sales = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            CustomerEntity customer = customers.get(random.nextInt(customers.size()));
            ProductEntity product = products.get(random.nextInt(products.size()));
            SalespersonEntity salesperson = salespersons.get(random.nextInt(salespersons.size()));

            SaleEntity sale = new SaleEntity();
            sale.setSaleId("SALE" + String.format("%08d", i));
            sale.setCustomerId(customer.getCustomerId());
            sale.setProductId(product.getProductId());
            sale.setSalespersonId(salesperson.getSalespersonId());
            sale.setQuantity(1 + random.nextInt(10));
            sale.setUnitPrice(product.getPrice());
            sale.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
            sale.setSaleDate(Instant.now().minus(random.nextInt(30), ChronoUnit.DAYS));
            sale.setChannel(CHANNELS[random.nextInt(CHANNELS.length)]);
            sale.setCreatedAt(Instant.now().minus(random.nextInt(5), ChronoUnit.MINUTES));
            sales.add(sale);
        }
        return sales;
    }
}
