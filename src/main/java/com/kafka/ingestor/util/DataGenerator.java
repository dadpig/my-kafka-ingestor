package com.kafka.ingestor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kafka.ingestor.domain.Customer;
import com.kafka.ingestor.domain.Product;
import com.kafka.ingestor.domain.Sale;
import com.kafka.ingestor.domain.Salesperson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final Random random = new Random(42);
    private static final String[] REGIONS = {"North", "South", "East", "West", "Central"};
    private static final String[] SEGMENTS = {"Premium", "Standard", "Basic"};
    private static final String[] CATEGORIES = {"Electronics", "Clothing", "Food", "Books", "Home"};
    private static final String[] MANUFACTURERS = {"TechCorp", "FashionHub", "FoodCo", "BookWorld", "HomeStyle"};
    private static final String[] CHANNELS = {"Online", "Store", "Mobile", "Partner"};
    private static final String[] COMPANY_PREFIXES = {"Tech", "Global", "Innovate", "Digital", "Smart", "Mega", "Ultra", "Super", "Pro", "Elite"};
    private static final String[] COMPANY_SUFFIXES = {"Corp", "Solutions", "Industries", "Systems", "Enterprises", "Group", "Partners", "Technologies", "Ventures", "Labs"};

    // City and Country data (50+ cities across 7 countries)
    private static final String[][] CITIES_BY_COUNTRY = {
        {"USA", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"},
        {"Canada", "Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa", "Edmonton", "Winnipeg", "Quebec City"},
        {"UK", "London", "Manchester", "Birmingham", "Leeds", "Glasgow", "Liverpool", "Edinburgh", "Bristol"},
        {"Germany", "Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne", "Stuttgart", "Dusseldorf", "Dortmund"},
        {"France", "Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Strasbourg", "Bordeaux"},
        {"Spain", "Madrid", "Barcelona", "Valencia", "Seville", "Bilbao", "Malaga", "Zaragoza", "Murcia"},
        {"Italy", "Rome", "Milan", "Naples", "Turin", "Florence", "Genoa", "Bologna", "Venice"}
    };

    private static final String[] FIRST_NAMES = {
        "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda", "William", "Elizabeth",
        "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen",
        "Daniel", "Nancy", "Matthew", "Lisa", "Anthony", "Betty", "Mark", "Margaret", "Donald", "Sandra",
        "Steven", "Ashley", "Paul", "Kimberly", "Andrew", "Emily", "Joshua", "Donna", "Kenneth", "Michelle",
        "Kevin", "Dorothy", "Brian", "Carol", "George", "Amanda", "Edward", "Melissa", "Ronald", "Deborah"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
        "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
        "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts"
    };

    public static void main(String[] args) throws IOException {
        System.out.println("Generating data files...");

        // SQL Data (Database Source)
        generateSalespersonSQL(500, "data/load-salespersons-full.sql");
        generateSalesSQL(10000, 100, 500, "data/load-sales-full.sql");

        // JSON Data (File System Source)
        generateProductJSON(500, "data/products-bulk.json");
        generateSalespersonJSON(500, "data/salespersons-bulk.json");

        // Note: Customers are now fetched from Web Service, not generated
        System.out.println("\nData generation completed!");
        System.out.println("\nData Distribution:");
        System.out.println("- Database (SQL): 500 salespersons, 10,000 sales");
        System.out.println("- File System (JSON): 500 products, 500 salespersons");
        System.out.println("- Web Service: Customers (mocked via REST API)");
    }

    // Legacy method - not used in new architecture (customers come from Web Service)
    private static void generateCustomerSQL(int count, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 1; i <= count; i++) {
                String customerId = String.format("CUST%05d", i);
                String companyName = generateCompanyName();
                String email = generateEmail(companyName);
                String segment = SEGMENTS[random.nextInt(SEGMENTS.length)];
                String region = REGIONS[random.nextInt(REGIONS.length)];
                int daysAgo = random.nextInt(365);

                writer.write(String.format(
                    "INSERT INTO customers (customer_id, name, email, segment, region, created_at, updated_at, processed) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s', DATEADD('DAY', -%d, CURRENT_TIMESTAMP), NULL, FALSE);\n",
                    customerId, companyName, email, segment, region, daysAgo
                ));

                if (i % 100 == 0) {
                    System.out.println("Generated " + i + " customer SQL inserts");
                }
            }
        }
        System.out.println("Customer SQL file created: " + filename);
    }

    private static void generateSalespersonSQL(int count, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 1; i <= count; i++) {
                String salespersonId = String.format("SP%05d", i);
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                String name = firstName + " " + lastName;

                // Select a random country and city
                String[] countryData = CITIES_BY_COUNTRY[random.nextInt(CITIES_BY_COUNTRY.length)];
                String country = countryData[0];
                String city = countryData[1 + random.nextInt(countryData.length - 1)];

                String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@salesforce.com";
                int daysAgo = random.nextInt(730); // Up to 2 years ago

                writer.write(String.format(
                    "INSERT INTO salespersons (salesperson_id, name, email, city, country, created_at, updated_at, processed) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s', DATEADD('DAY', -%d, CURRENT_TIMESTAMP), NULL, FALSE);\n",
                    salespersonId, name, email, city, country, daysAgo
                ));

                if (i % 100 == 0) {
                    System.out.println("Generated " + i + " salesperson SQL inserts");
                }
            }
        }
        System.out.println("Salesperson SQL file created: " + filename);
    }

    private static void generateSalesSQL(int count, int numCompanies, int numSalespeople, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 1; i <= count; i++) {
                String saleId = String.format("SALE%08d", i);
                String customerId = String.format("CUST%05d", (i % numCompanies) + 1);
                String productId = String.format("PROD%05d", random.nextInt(500) + 1);
                String salespersonId = String.format("SP%05d", random.nextInt(numSalespeople) + 1);
                int quantity = random.nextInt(20) + 1;
                BigDecimal unitPrice = BigDecimal.valueOf(10 + random.nextDouble() * 990)
                    .setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
                String channel = CHANNELS[random.nextInt(CHANNELS.length)];
                int daysAgo = random.nextInt(90);
                int hoursAgo = random.nextInt(24);
                int minutesAgo = random.nextInt(60);

                writer.write(String.format(
                    "INSERT INTO sales (sale_id, customer_id, product_id, salesperson_id, quantity, unit_price, total_amount, sale_date, channel, created_at, processed) " +
                    "VALUES ('%s', '%s', '%s', '%s', %d, %s, %s, DATEADD('MINUTE', -%d, DATEADD('HOUR', -%d, DATEADD('DAY', -%d, CURRENT_TIMESTAMP))), '%s', CURRENT_TIMESTAMP, FALSE);\n",
                    saleId, customerId, productId, salespersonId, quantity, unitPrice, totalAmount, minutesAgo, hoursAgo, daysAgo, channel
                ));

                if (i % 1000 == 0) {
                    System.out.println("Generated " + i + " sales SQL inserts");
                }
            }
        }
        System.out.println("Sales SQL file created: " + filename);
    }

    // Legacy method - not used in new architecture (customers come from Web Service)
    private static void generateCustomerJSON(int count, String filename) throws IOException {
        List<Customer> customers = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Customer customer = new Customer();
            customer.setCustomerId(String.format("CUST%05d", i));
            customer.setName(generateCompanyName());
            customer.setEmail(generateEmail(customer.getName()));
            customer.setSegment(SEGMENTS[random.nextInt(SEGMENTS.length)]);
            customer.setRegion(REGIONS[random.nextInt(REGIONS.length)]);
            customer.setCreatedAt(Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS));
            customers.add(customer);
        }

        ObjectMapper mapper = createObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), customers);
        System.out.println("Customer JSON file created: " + filename + " (" + count + " records)");
    }

    private static void generateProductJSON(int count, String filename) throws IOException {
        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Product product = new Product();
            product.setProductId(String.format("PROD%05d", i));
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            product.setName(category + " Product " + i);
            product.setCategory(category);
            product.setPrice(BigDecimal.valueOf(10 + random.nextDouble() * 990).setScale(2, RoundingMode.HALF_UP));
            product.setManufacturer(MANUFACTURERS[random.nextInt(MANUFACTURERS.length)]);
            product.setCreatedAt(Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS));
            products.add(product);
        }

        ObjectMapper mapper = createObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), products);
        System.out.println("Product JSON file created: " + filename + " (" + count + " records)");
    }

    private static void generateSalespersonJSON(int count, String filename) throws IOException {
        List<Salesperson> salespersons = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Salesperson salesperson = new Salesperson();
            salesperson.setSalespersonId(String.format("SP%05d", i));

            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            salesperson.setName(firstName + " " + lastName);

            // Select a random country and city
            String[] countryData = CITIES_BY_COUNTRY[random.nextInt(CITIES_BY_COUNTRY.length)];
            salesperson.setCountry(countryData[0]);
            salesperson.setCity(countryData[1 + random.nextInt(countryData.length - 1)]);

            salesperson.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@salesforce.com");
            salesperson.setCreatedAt(Instant.now().minus(random.nextInt(730), ChronoUnit.DAYS));

            salespersons.add(salesperson);
        }

        ObjectMapper mapper = createObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), salespersons);
        System.out.println("Salesperson JSON file created: " + filename + " (" + count + " records)");
    }

    private static String generateCompanyName() {
        String prefix = COMPANY_PREFIXES[random.nextInt(COMPANY_PREFIXES.length)];
        String suffix = COMPANY_SUFFIXES[random.nextInt(COMPANY_SUFFIXES.length)];
        return prefix + suffix + (random.nextBoolean() ? " Inc" : " Ltd");
    }

    private static String generateEmail(String companyName) {
        String domain = companyName.toLowerCase()
            .replaceAll("[^a-z0-9]", "")
            .substring(0, Math.min(10, companyName.length()));
        return "contact@" + domain + ".com";
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
