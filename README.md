# Kafka Stream Ingestor

A comprehensive Spring Boot application demonstrating multi-source data ingestion with Apache Kafka Streams, featuring real-time data enrichment and aggregation.

## Architecture Overview

### Data Sources
1. **Relational Database (H2)**: Polls for new customers, products, and sales records
2. **File System**: Monitors directory for JSON files containing batch data
3. **Web Service**: Fetches sales data from REST API endpoints

### Kafka Streams Processing
- **Enrichment**: Joins sales with customer and product data using KTable lookups
- **Aggregation**: Computes real-time metrics by region, segment, category, and channel
- **Windowing**: 5-minute tumbling windows for time-based aggregations

### Key Features
- Exactly-once semantics (EOS v2)
- Idempotent producers
- State stores with changelog topics
- Multi-dimensional aggregations
- High-throughput batch processing

## Technology Stack

- Java 25 (with preview features)
- Spring Boot 4.0.3
- Apache Kafka 4.0.0
- Kafka Streams
- Spring Data JPA
- H2 Database
- WebFlux (for reactive HTTP client)
- Micrometer & Prometheus

## Prerequisites

- Java 25 JDK
- Maven 3.9+
- Docker & Docker Compose

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

### 2. Build the Application

```bash
mvn clean package
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or with Java directly:

```bash
java --enable-preview -jar target/kafka-stream-ingestor-1.0.0.jar
```

## Configuration

### Application Properties

Key configurations in `application.yml`:

```yaml
kafka:
  topics:
    customers: customers
    products: products
    sales: sales
    sales-enriched: sales-enriched
    sales-aggregation: sales-aggregation

ingestor:
  database:
    enabled: true
    poll-interval: 5000

  filesystem:
    enabled: true
    watch-directory: /tmp/kafka-ingestor/input
    processed-directory: /tmp/kafka-ingestor/processed

  webservice:
    enabled: true
    endpoint: http://localhost:8090/api/sales
    poll-interval: 10000
```

## Data Flow

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Database   │────▶│              │     │             │
└─────────────┘     │   Kafka      │────▶│   Kafka     │
┌─────────────┐     │  Producers   │     │   Streams   │
│ File System │────▶│              │     │             │
└─────────────┘     └──────────────┘     └─────────────┘
┌─────────────┐            │                    │
│ Web Service │────────────┘                    │
└─────────────┘                                 ▼
                                        ┌───────────────┐
                                        │  Enrichment   │
                                        │  Aggregation  │
                                        └───────────────┘
```

### Topics

1. **customers**: Customer master data (compacted)
2. **products**: Product catalog (compacted)
3. **sales**: Raw sales transactions
4. **sales-enriched**: Sales joined with customer & product data
5. **sales-aggregation**: Real-time aggregated metrics

## Monitoring

### Endpoints

- **Application**: http://localhost:8080
- **Kafka UI**: http://localhost:8080 (Docker)
- **H2 Console**: http://localhost:8080/h2-console
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Health Check**: http://localhost:8080/actuator/health
- **Status API**: http://localhost:8080/api/monitoring/status

### Monitoring Status

```bash
curl http://localhost:8080/api/monitoring/status
```

Response:
```json
{
  "customers": {"total": 50, "pending": 0},
  "products": {"total": 30, "pending": 0},
  "sales": {"total": 100, "pending": 0}
}
```

## Sample Data

### Database Ingestor

The application automatically initializes with:
- 50 customers across 5 regions and 3 segments
- 30 products in 5 categories
- 100 sales transactions

### File System Ingestor

Place JSON files in `/tmp/kafka-ingestor/input/`:

**customers.json**:
```json
[
  {
    "customerId": "CUST99999",
    "name": "John Doe",
    "email": "john@example.com",
    "segment": "Premium",
    "region": "North",
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**products.json**:
```json
[
  {
    "productId": "PROD99999",
    "name": "Laptop Pro",
    "category": "Electronics",
    "price": 1299.99,
    "manufacturer": "TechCorp",
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**sales.json**:
```json
[
  {
    "saleId": "SALE99999999",
    "customerId": "CUST99999",
    "productId": "PROD99999",
    "quantity": 2,
    "unitPrice": 1299.99,
    "totalAmount": 2599.98,
    "saleDate": "2024-01-15T10:30:00Z",
    "channel": "Online"
  }
]
```

## Kafka Streams Topology

### Enrichment Flow

```
sales-stream
  │
  ├─▶ rekey-by-customer
  │
  ├─▶ left-join(customers-table)
  │
  ├─▶ rekey-by-product
  │
  ├─▶ left-join(products-table)
  │
  └─▶ sales-enriched-topic
```

### Aggregation Flow

```
sales-enriched-stream
  │
  ├─▶ group-by-region ──▶ windowed-aggregate ──┐
  ├─▶ group-by-segment ─▶ windowed-aggregate ──┤
  ├─▶ group-by-category ▶ windowed-aggregate ──┼─▶ merge ──▶ sales-aggregation-topic
  └─▶ group-by-channel ─▶ windowed-aggregate ──┘
```

## Performance Tuning

### Producer Settings
- `acks=all`: Wait for all in-sync replicas
- `enable.idempotence=true`: Prevent duplicates
- `compression.type=lz4`: Efficient compression
- `batch.size=32768`: 32KB batches

### Consumer Settings
- `enable.auto.commit=false`: Manual offset management
- `max.poll.records=100`: Batch processing size

### Streams Settings
- `processing.guarantee=exactly_once_v2`: Exactly-once semantics
- `num.stream.threads=4`: Parallel processing threads
- `cache.max.bytes.buffering=10485760`: 10MB cache

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

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

## License

MIT License
