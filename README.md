# Kafka Stream Ingestor

A comprehensive Spring Boot application demonstrating multi-source data ingestion with Apache Kafka Streams, featuring real-time data enrichment and aggregation with geographic analytics.

## Architecture Overview

### Refactored Data Sources (March 2026)

**Clear separation of concerns across three data sources:**

1. **📊 Relational Database (H2)**:
   - **Sales** (10,000 records): Transactional data with references to customers, products, and salespeople
   - **Products** (500 records): Product catalog

2. **📁 File System**:
   - **Customers** (1,000 records): Customer master data from JSON files

3. **🌐 Web Service (REST API)**:
   - **Salespeople** (100 records): Salesperson data with geographic information (city/country)

### Data Relationships

All sales transactions reference:
- **CUST00001-CUST01000**: 1,000 customers from FILE_SYSTEM
- **PROD00001-PROD00500**: 500 products from DATABASE
- **SP00001-SP00100**: 100 salespeople from WEB_SERVICE

### Kafka Streams Processing

- **4-Way Join**: Sales enriched with Customer + Product + Salesperson data
- **Aggregations**:
  - Regional sales metrics
  - City-based performance (by salesperson location)
  - Salesperson performance tracking
  - Category and channel analytics
- **Windowing**: 5-minute tumbling windows for time-based aggregations
- **Exactly-once semantics** (EOS v2)

### Key Features

- Multi-source data ingestion (Database, File System, Web Service)
- Real-time data enrichment with 4-way joins
- Geographic analytics (city/country aggregations)
- State stores with changelog topics
- High-throughput batch processing
- Prometheus metrics

## Technology Stack

- **Java 25** (with preview features)
- **Spring Boot 4.0.3**
- **Apache Kafka 3.9.0**
- **Kafka Streams** (exactly-once semantics v2)
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **WebFlux** (reactive HTTP client)
- **Micrometer & Prometheus**

## Prerequisites

- Java 25 JDK (or Java 21 JDK for bytecode compatibility)
- Maven 3.9+
- Docker & Docker Compose
- Python 3 (for data generation)

## Quick Start

### 1. Start Kafka Infrastructure

```bash
docker-compose up -d
```

This starts:
- Zookeeper (port 2181)
- Kafka Broker (port 9092)
- Schema Registry (port 8081)
- Kafka UI (port 8080)

### 2. Generate Sample Data

```bash
python3 data/generate-refactored-data.py
```

This generates:
- `data/products-500.sql` - 500 products for database
- `data/sales-10k.sql` - 10,000 sales for database
- `data/customers-1k.json` - 1,000 customers for file system
- `data/salespeople-100-reference.json` - 100 salespeople for web service

### 3. Build the Application

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home mvn clean package -Dmaven.test.skip=true
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or with Java directly:

```bash
java --enable-preview -jar target/kafka-stream-ingestor-1.0.0.jar
```

### 5. Load Data

**Database (Sales + Products)**:
```sql
-- Connect to H2 console at http://localhost:8090/h2-console
-- Run the SQL files:
\i data/products-500.sql
\i data/sales-10k.sql
```

**File System (Customers)**:
```bash
# Copy customer file to watch directory
cp data/customers-1k.json /tmp/kafka-ingestor/input/
```

**Web Service (Salespeople)**:
Salespeople are automatically loaded by `MockWebServiceController` from `data/salespeople-100-reference.json`

## Configuration

### Application Properties

Key configurations in `application.yml`:

```yaml
kafka:
  topics:
    customers: customers
    products: products
    sales: sales
    salespersons: salespersons
    sales-enriched: sales-enriched
    sales-aggregation: sales-aggregation
    city-aggregation: city-aggregation
    salesperson-aggregation: salesperson-aggregation

ingestor:
  database:
    enabled: true
    poll-interval: 5000     # Polls for Sales + Products

  filesystem:
    enabled: true
    watch-directory: /tmp/kafka-ingestor/input
    poll-interval: 3000     # Scans for Customers JSON

  webservice:
    enabled: true
    endpoint: http://localhost:8090/api/salespeople?limit=100
    poll-interval: 10000    # Fetches Salespeople from REST API
```

## Data Flow

```
┌─────────────────┐     ┌──────────────┐     ┌─────────────────┐
│    DATABASE     │────▶│              │     │                 │
│ Sales+Products  │     │   Kafka      │────▶│  Kafka Streams  │
└─────────────────┘     │  Producers   │     │                 │
┌─────────────────┐     │              │     │  4-Way Join     │
│  FILE_SYSTEM    │────▶│              │     │  (Customer +    │
│   Customers     │     │              │     │   Product +     │
└─────────────────┘     └──────────────┘     │   Salesperson)  │
┌─────────────────┐            │             │                 │
│  WEB_SERVICE    │────────────┘             │  Aggregations   │
│  Salespeople    │                          │  (City, Sales-  │
└─────────────────┘                          │   person, etc)  │
                                             └─────────────────┘
                                                      │
                                                      ▼
                                             ┌─────────────────┐
                                             │  Analytics API  │
                                             │  State Stores   │
                                             └─────────────────┘
```

### Topics & State Stores

**Input Topics**:
1. **customers**: Customer master data (FILE_SYSTEM) - compacted
2. **products**: Product catalog (DATABASE) - compacted
3. **salespersons**: Salesperson data (WEB_SERVICE) - compacted
4. **sales**: Raw sales transactions (DATABASE)

**Output Topics**:
5. **sales-enriched**: Sales joined with customer, product, and salesperson data
6. **sales-aggregation**: Real-time aggregated metrics
7. **city-aggregation**: City-based performance metrics
8. **salesperson-aggregation**: Individual salesperson performance

**State Stores**:
- `customers-table` - KTable for customer lookups
- `products-table` - KTable for product lookups
- `salespersons-table` - KTable for salesperson lookups
- `city-aggregation-store` - City performance data
- `salesperson-aggregation-store` - Salesperson performance data

## Analytics Endpoints

### Geographic Analytics

```bash
# Top performing cities
curl http://localhost:8090/api/analytics/top-sales-by-city

# Top performing salespeople
curl http://localhost:8090/api/analytics/top-salespeople

# City statistics
curl http://localhost:8090/api/analytics/city-stats?city=London

# Salesperson statistics
curl http://localhost:8090/api/analytics/salesperson-stats?salespersonId=SP00001
```

### System Monitoring

```bash
# Application health
curl http://localhost:8090/actuator/health

# Prometheus metrics
curl http://localhost:8090/actuator/prometheus

# Monitoring status
curl http://localhost:8090/api/monitoring/status
```

## Sample Data Structure

### Customers (FILE_SYSTEM)
```json
{
  "customerId": "CUST00001",
  "name": "DigitalVentures Inc",
  "email": "contact@digitalven.com",
  "segment": "Standard",
  "region": "Central",
  "createdAt": "2026-02-15T06:48:22.269026Z"
}
```

### Products (DATABASE)
```sql
INSERT INTO products (product_id, name, category, price, manufacturer, created_at, updated_at, processed)
VALUES ('PROD00001', 'Electronics Product 1', 'Electronics', 34.76, 'FoodCo',
        DATEADD('DAY', -125, CURRENT_TIMESTAMP), NULL, FALSE);
```

### Salespeople (WEB_SERVICE)
```json
{
  "salespersonId": "SP00001",
  "name": "Karen Jackson",
  "email": "karen.jackson@salesforce.com",
  "city": "Turin",
  "country": "Italy",
  "createdAt": "2025-06-13T06:48:22.277284Z"
}
```

### Sales (DATABASE)
```sql
INSERT INTO sales (sale_id, customer_id, product_id, salesperson_id, quantity,
                   unit_price, total_amount, sale_date, channel, created_at, processed)
VALUES ('SALE00000001', 'CUST00001', 'PROD00443', 'SP00079', 14, 255.79, 3581.06,
        DATEADD('MINUTE', -46, DATEADD('HOUR', -18, DATEADD('DAY', -41, CURRENT_TIMESTAMP))),
        'Store', CURRENT_TIMESTAMP, FALSE);
```

## Kafka Streams Topology

### Enrichment Flow

```
sales-stream
  │
  ├─▶ rekey-by-customer
  │    └─▶ left-join(customers-table)
  │
  ├─▶ rekey-by-product
  │    └─▶ left-join(products-table)
  │
  ├─▶ rekey-by-salesperson
  │    └─▶ left-join(salespersons-table)
  │
  └─▶ sales-enriched-topic
```

### Aggregation Flow

```
sales-enriched-stream
  │
  ├─▶ group-by-city ────────▶ aggregate ──┐
  ├─▶ group-by-salesperson ─▶ aggregate ──┤
  ├─▶ group-by-region ──────▶ aggregate ──┼─▶ merge ──▶ aggregation-topics
  ├─▶ group-by-category ────▶ aggregate ──┤
  └─▶ group-by-channel ─────▶ aggregate ──┘
```

## Performance Tuning

### Producer Settings
- `acks=all`: Wait for all in-sync replicas
- `enable.idempotence=true`: Prevent duplicates
- `compression.type=lz4`: Efficient compression
- `batch.size=32768`: 32KB batches

### Streams Settings
- `processing.guarantee=exactly_once_v2`: Exactly-once semantics
- `num.stream.threads=4`: Parallel processing threads
- `cache.max.bytes.buffering=10485760`: 10MB cache

## Testing

### Unit Tests

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home mvn test
```

### Integration Tests

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home mvn verify
```

## Monitoring

### Endpoints

- **Application**: http://localhost:8090
- **Kafka UI**: http://localhost:8080
- **H2 Console**: http://localhost:8090/h2-console
- **Prometheus Metrics**: http://localhost:8090/actuator/prometheus
- **Health Check**: http://localhost:8090/actuator/health
- **Mock Web Service**: http://localhost:8090/api/salespeople
- **Mock Health Check**: http://localhost:8090/api/salespeople/health

## Troubleshooting

### Kafka Connection Issues

Ensure Kafka is running:
```bash
docker-compose ps
```

Check Kafka logs:
```bash
docker-compose logs -f kafka
```

### State Store Issues

Clean up state stores:
```bash
rm -rf /tmp/kafka-streams/*
```

### Reset Consumer Groups

```bash
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group sales-aggregation-app \
  --reset-offsets --to-earliest \
  --all-topics --execute
```

## Production Considerations

1. **External Kafka Cluster**: Update bootstrap-servers in production
2. **Database**: Replace H2 with PostgreSQL/MySQL
3. **State Stores**: Configure persistent volumes
4. **Monitoring**: Integrate with Prometheus/Grafana
5. **Security**: Enable SSL/TLS and SASL authentication
6. **Scaling**: Deploy multiple instances for horizontal scaling
7. **Data Generation**: Use production data loaders instead of sample scripts

## Architecture Benefits

✅ **Clear Separation**: Each data source handles one entity type
✅ **Scalability**: Easy to scale each ingestor independently
✅ **Maintainability**: Simple, focused services
✅ **Testability**: Mock/test each source separately
✅ **Realistic**: Mimics real-world multi-source architectures

## License

MIT License
