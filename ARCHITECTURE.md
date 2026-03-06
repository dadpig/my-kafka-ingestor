# Architecture Documentation

## System Architecture

### Overview

The Kafka Stream Ingestor is designed to ingest data from multiple heterogeneous sources, enrich the data through stream joins, and compute real-time aggregations using Apache Kafka Streams.

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Data Sources Layer                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Database   │  │  File System │  │ Web Service  │         │
│  │   Ingestor   │  │   Ingestor   │  │  Ingestor    │         │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘         │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Kafka Producer Layer                         │
├─────────────────────────────────────────────────────────────────┤
│                 KafkaProducerService                            │
│  ┌────────────────────────────────────────────────────┐        │
│  │  - Idempotent Producer                              │        │
│  │  - Compression (LZ4)                                │        │
│  │  - Batching & Async Sends                           │        │
│  └────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Kafka Topics                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                     │
│  │customers │  │ products │  │  sales   │                     │
│  │(compact) │  │(compact) │  │          │                     │
│  └──────────┘  └──────────┘  └──────────┘                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Kafka Streams Layer                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌────────────────────────────────────────────────────┐        │
│  │              Stream Enrichment                      │        │
│  │  ┌──────────────────────────────────────────────┐  │        │
│  │  │ 1. Sales Stream (KStream)                    │  │        │
│  │  │ 2. Customers Table (KTable - compacted)      │  │        │
│  │  │ 3. Products Table (KTable - compacted)       │  │        │
│  │  │ 4. Left Join: Sales + Customer               │  │        │
│  │  │ 5. Left Join: Result + Product               │  │        │
│  │  └──────────────────────────────────────────────┘  │        │
│  └────────────────────────────────────────────────────┘        │
│                           │                                     │
│                           ▼                                     │
│  ┌────────────────────────────────────────────────────┐        │
│  │         Windowed Aggregations                      │        │
│  │  ┌──────────────────────────────────────────────┐  │        │
│  │  │ - Group by Region (tumbling 5min window)     │  │        │
│  │  │ - Group by Segment (tumbling 5min window)    │  │        │
│  │  │ - Group by Category (tumbling 5min window)   │  │        │
│  │  │ - Group by Channel (tumbling 5min window)    │  │        │
│  │  │                                               │  │        │
│  │  │ Metrics: Total Sales, Revenue, Quantity,     │  │        │
│  │  │          Average Order Value                  │  │        │
│  │  └──────────────────────────────────────────────┘  │        │
│  └────────────────────────────────────────────────────┘        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
          │                  │
          ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Output Topics                                │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐                   │
│  │ sales-enriched   │  │ sales-aggregation│                   │
│  └──────────────────┘  └──────────────────┘                   │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Details

### 1. Database Ingestor

**Technology**: Spring Data JPA + H2/PostgreSQL

**Process**:
1. Scheduled polling every 5 seconds
2. Query unprocessed records (`processed = false`)
3. Batch read up to 100 records
4. Convert entities to domain models
5. Send to Kafka via producer
6. Mark records as processed in transaction

**Database Schema**:
```sql
CREATE TABLE customers (
  customer_id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  email VARCHAR(200) NOT NULL,
  segment VARCHAR(50),
  region VARCHAR(50),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  processed BOOLEAN DEFAULT FALSE,
  INDEX idx_customer_segment (segment),
  INDEX idx_customer_region (region)
);
```

**Benefits**:
- Change Data Capture (CDC) pattern
- Transactional consistency
- Batch processing for efficiency
- No message loss

### 2. File System Ingestor

**Technology**: Java NIO + Jackson JSON

**Process**:
1. Monitor watch directory every 3 seconds
2. Scan for `.json` files
3. Parse files based on naming convention:
   - `customer*.json` → Customer records
   - `product*.json` → Product records
   - `sale*.json` → Sale records
4. Send records to Kafka
5. Move processed files to archive directory

**File Format**:
```json
[
  {
    "customerId": "CUST00001",
    "name": "John Doe",
    ...
  }
]
```

**Benefits**:
- Batch file processing
- Automatic archival
- Simple integration point
- Error isolation per file

### 3. Web Service Ingestor

**Technology**: Spring WebFlux (Reactive HTTP)

**Process**:
1. Poll endpoint every 10 seconds
2. Use reactive WebClient for non-blocking I/O
3. Timeout after 5 seconds
4. Parse JSON response
5. Send records to Kafka
6. Handle errors gracefully

**API Contract**:
```http
GET /api/sales HTTP/1.1
Accept: application/json

Response: 200 OK
[
  {
    "saleId": "SALE00001",
    "customerId": "CUST00001",
    ...
  }
]
```

**Benefits**:
- Non-blocking I/O
- Timeout protection
- Retry logic
- Scalable integration

## Kafka Streams Processing

### Enrichment Pipeline

**Step 1: Sales Stream**
```java
KStream<String, Sale> salesStream =
  builder.stream("sales", Consumed.with(String, JsonSerde));
```

**Step 2: Customer Join**
```java
salesStream
  .selectKey((k, v) -> v.getCustomerId())
  .leftJoin(customersTable, (sale, customer) -> enrich(sale, customer))
```

**Step 3: Product Join**
```java
.selectKey((k, v) -> v.getProductId())
.leftJoin(productsTable, (enriched, product) -> enrich(enriched, product))
```

**Result**: `SalesEnriched` with full customer and product details

### Aggregation Pipeline

**Multi-Dimensional Aggregation**:

Each dimension (Region, Segment, Category, Channel) follows:

```java
enrichedStream
  .groupBy((k, v) -> extractDimension(v))
  .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
  .aggregate(
    SalesAggregator::new,
    (key, value, aggregate) -> aggregate.add(value),
    Materialized.as(storeName)
  )
  .toStream()
  .map((windowed, agg) -> agg.toSalesAggregation())
```

**Computed Metrics**:
- Total Sales Count
- Total Revenue (SUM)
- Total Quantity (SUM)
- Average Order Value (Revenue / Count)

**Window Characteristics**:
- Type: Tumbling Window
- Size: 5 minutes
- Grace Period: 0 (no late arrivals)
- Retention: 24 hours

## State Management

### State Stores

**1. Customer Store**
- Type: Key-Value Store (Persistent)
- Compaction: Enabled (changelog topic)
- Purpose: Latest customer state per ID

**2. Product Store**
- Type: Key-Value Store (Persistent)
- Compaction: Enabled (changelog topic)
- Purpose: Latest product state per ID

**3. Aggregation Stores**
- Type: Window Store (Persistent)
- Windows: 5-minute tumbling
- Retention: 24 hours
- Purpose: Time-based aggregates

**Changelog Topics**:
- Auto-created by Kafka Streams
- Naming: `<app-id>-<store-name>-changelog`
- Replication: Same as application config
- Used for state recovery

## Guarantees & Semantics

### Exactly-Once Semantics (EOS v2)

**Configuration**:
```yaml
processing.guarantee: exactly_once_v2
enable.idempotence: true
```

**How it Works**:
1. Transactional producer writes
2. Consumer reads committed only
3. Atomic state updates + output
4. Idempotent producer prevents duplicates

**Benefits**:
- No duplicate processing
- No lost messages
- Consistent state
- Better performance than EOS v1

### Message Ordering

**Per-Partition Ordering**:
- Guaranteed within a partition
- Key-based partitioning ensures related records co-locate
- Join operations preserve ordering

**Cross-Partition**:
- No ordering guarantees
- Windowing helps align timing

## Performance Optimizations

### Producer Optimizations

1. **Batching**:
   - `linger.ms=10`: Wait 10ms for batch accumulation
   - `batch.size=32768`: 32KB batch size
   - Reduces network overhead

2. **Compression**:
   - `compression.type=lz4`: Fast compression
   - ~50% size reduction
   - Low CPU overhead

3. **In-Flight Requests**:
   - `max.in.flight.requests.per.connection=5`
   - Pipeline multiple requests
   - Maintains ordering with idempotence

### Streams Optimizations

1. **Threading**:
   - `num.stream.threads=4`: Parallel processing
   - One thread per partition for scalability

2. **Caching**:
   - `cache.max.bytes.buffering=10485760`: 10MB cache
   - Reduces duplicate processing
   - Batches state updates

3. **Commit Interval**:
   - `commit.interval.ms=1000`: 1 second commits
   - Balance latency vs throughput

## Scalability

### Horizontal Scaling

**Application Instances**:
- Deploy multiple instances
- Kafka Streams auto-balances partitions
- Maximum instances = partition count

**Partition Strategy**:
- Customers: 3 partitions (low volume, compacted)
- Products: 3 partitions (low volume, compacted)
- Sales: 6 partitions (high volume)
- Sales Enriched: 6 partitions
- Aggregations: 3 partitions

### Vertical Scaling

**Resources**:
- CPU: Stream threads benefit from more cores
- Memory: State stores, caches, buffers
- Disk: State store persistence
- Network: High throughput requirements

## Monitoring & Observability

### Metrics (Micrometer + Prometheus)

**Kafka Streams Metrics**:
- `kafka.stream.task.process.latency.avg`
- `kafka.stream.task.commit.latency.avg`
- `kafka.stream.thread.poll.rate`

**Producer Metrics**:
- `kafka.producer.record.send.rate`
- `kafka.producer.record.error.rate`
- `kafka.producer.compression.rate.avg`

**Consumer Metrics**:
- `kafka.consumer.records.consumed.rate`
- `kafka.consumer.records.lag.max`

### Health Checks

**Spring Boot Actuator**:
- `/actuator/health`: Application health
- `/actuator/prometheus`: Prometheus metrics
- `/actuator/metrics`: Detailed metrics

**Custom Monitoring**:
- `/api/monitoring/status`: Database ingestor status
- Tracks pending records count

## Fault Tolerance

### Application Failures

**Kafka Streams**:
- State stores backed by changelog topics
- On restart: Restore state from changelog
- Processing resumes from last commit

**Producers**:
- Retry on transient errors
- Log permanent failures
- Dead letter queue pattern (future)

### Kafka Broker Failures

**Replication**:
- Topic replication factor: 1 (dev), 3 (prod)
- In-sync replicas: Automatic failover
- Leader election: Transparent to application

### Network Partitions

**Streams**:
- Rebalancing on partition
- State recovery on rejoin
- Exactly-once guarantees maintained

## Security Considerations

### Current Implementation (Development)
- No authentication
- No encryption
- Local H2 database

### Production Recommendations

1. **Kafka Security**:
   - SSL/TLS for encryption
   - SASL for authentication
   - ACLs for authorization

2. **Database**:
   - Connection pooling
   - Encrypted credentials
   - Network isolation

3. **API Security**:
   - OAuth 2.0 / JWT
   - Rate limiting
   - Input validation

## Future Enhancements

1. **Schema Registry Integration**:
   - Avro schema validation
   - Schema evolution
   - Backward compatibility

2. **Dead Letter Queue**:
   - Handle poison messages
   - Retry policies
   - Error analysis

3. **Custom Metrics Dashboard**:
   - Grafana dashboards
   - Real-time visualization
   - Alert management

4. **Advanced Windowing**:
   - Hopping windows
   - Session windows
   - Late arrival handling

5. **Stream-Stream Joins**:
   - Join multiple event streams
   - Temporal joins
   - Out-of-order handling
