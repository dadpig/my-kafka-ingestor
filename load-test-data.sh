#!/bin/bash

echo "=========================================="
echo "Kafka Stream Ingestor - Load Test Data"
echo "=========================================="
echo ""

if [ ! -f "data/load-customers-full.sql" ]; then
    echo "⚠️  Test data files not found. Generating now..."
    ./generate-data.sh
    echo ""
fi

echo "Step 1: Ensure application is running"
echo "Checking if application is running on port 8080..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is running"
else
    echo "❌ Application is not running!"
    echo ""
    echo "Please start the application first:"
    echo "  ./run-app.sh"
    exit 1
fi

echo ""
echo "Step 2: Load Database Data"
echo "=========================================="
echo ""
echo "Please follow these steps:"
echo ""
echo "1. Open H2 Console: http://localhost:8080/h2-console"
echo ""
echo "2. Connection Settings:"
echo "   - JDBC URL: jdbc:h2:mem:testdb"
echo "   - User Name: sa"
echo "   - Password: (leave empty)"
echo ""
echo "3. Execute the following SQL files:"
echo "   a) Load 1000 customers:"
echo "      @$(pwd)/data/load-customers-full.sql"
echo ""
echo "   b) Load 10,000 sales:"
echo "      @$(pwd)/data/load-sales-full.sql"
echo ""
echo "   Or copy and paste SQL from files directly"
echo ""
echo "=========================================="

echo ""
echo "Step 3: Load File System Data"
echo "=========================================="
echo ""

mkdir -p /tmp/kafka-ingestor/input

if [ -f "data/customers-bulk.json" ]; then
    echo "Copying customers-bulk.json to watch directory..."
    cp data/customers-bulk.json /tmp/kafka-ingestor/input/customers-$(date +%s).json
    echo "✅ Customers file copied"
else
    echo "⚠️  customers-bulk.json not found, skipping"
fi

if [ -f "data/products-bulk.json" ]; then
    echo "Copying products-bulk.json to watch directory..."
    cp data/products-bulk.json /tmp/kafka-ingestor/input/products-$(date +%s).json
    echo "✅ Products file copied"
else
    echo "⚠️  products-bulk.json not found, skipping"
fi

if [ -f "data/sales-bulk.json" ]; then
    echo "Copying sales-bulk.json to watch directory..."
    cp data/sales-bulk.json /tmp/kafka-ingestor/input/sales-$(date +%s).json
    echo "✅ Sales file copied"
else
    echo "⚠️  sales-bulk.json not found, skipping"
fi

echo ""
echo "Step 4: Verify Web Service Ingestor"
echo "=========================================="
echo ""

echo "Testing mock web service endpoint..."
RESPONSE=$(curl -s http://localhost:8080/api/sales?limit=1)

if [ $? -eq 0 ]; then
    echo "✅ Mock web service is responding"
    echo ""
    echo "Sample response:"
    echo "$RESPONSE" | head -c 200
    echo "..."
else
    echo "❌ Mock web service is not responding"
fi

echo ""
echo ""
echo "=========================================="
echo "Data Loading Summary"
echo "=========================================="
echo ""
echo "✅ File system data copied (will be processed in ~3 seconds)"
echo "⏳ Database data - manual SQL execution required"
echo "✅ Web service - automatically polls every 10 seconds"
echo ""
echo "Monitor Processing:"
echo "  tail -f logs/kafka-ingestor.log | grep -E 'Ingesting|Processing|Fetched'"
echo ""
echo "Check Status:"
echo "  curl http://localhost:8080/api/monitoring/status | jq"
echo ""
echo "View Topics (Kafka UI):"
echo "  http://localhost:8080"
echo ""
echo "=========================================="
