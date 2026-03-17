#!/bin/bash

echo "=========================================="
echo "Kafka Stream Ingestor - Load Data"
echo "Refactored Architecture (March 2026)"
echo "=========================================="
echo ""

# Check if data files exist
if [ ! -f "data/products-500.sql" ] || [ ! -f "data/sales-10k.sql" ] || [ ! -f "data/customers-1k.json" ]; then
    echo "⚠️  Data files not found. Generating now..."
    python3 data/generate-refactored-data.py
    echo ""
fi

echo "Step 1: Verify Application is Running"
echo "=========================================="
echo "Checking application on port 8090..."
if curl -s http://localhost:8090/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is running"
else
    echo "❌ Application is not running!"
    echo ""
    echo "Please start the application first:"
    echo "  ./run-app.sh"
    echo "  OR"
    echo "  mvn spring-boot:run"
    exit 1
fi

echo ""
echo "Step 2: Load Database Data (Manual)"
echo "=========================================="
echo ""
echo "📊 DATABASE: Products + Sales"
echo ""
echo "Please execute these SQL files in H2 Console:"
echo ""
echo "1. Open H2 Console: http://localhost:8090/h2-console"
echo ""
echo "2. Connection Settings:"
echo "   - JDBC URL: jdbc:h2:mem:testdb"
echo "   - User Name: sa"
echo "   - Password: (leave empty)"
echo ""
echo "3. Execute SQL files:"
echo "   a) Load 500 products:"
echo "      Copy/paste contents from: data/products-500.sql"
echo ""
echo "   b) Load 10,000 sales:"
echo "      Copy/paste contents from: data/sales-10k.sql"
echo ""
echo "Press Enter when database loading is complete..."
read

echo ""
echo "Step 3: Load File System Data"
echo "=========================================="
echo ""
echo "📁 FILE_SYSTEM: Customers"
echo ""

mkdir -p /tmp/kafka-ingestor/input

if [ -f "data/customers-1k.json" ]; then
    echo "Copying customers-1k.json to watch directory..."
    cp data/customers-1k.json /tmp/kafka-ingestor/input/customers-$(date +%s).json
    echo "✅ Customers file copied (1,000 records)"
    echo "   File will be processed in ~3 seconds"
else
    echo "❌ data/customers-1k.json not found!"
fi

echo ""
echo "Step 4: Verify Web Service"
echo "=========================================="
echo ""
echo "🌐 WEB_SERVICE: Salespeople"
echo ""

echo "Testing MockWebServiceController..."
HEALTH=$(curl -s http://localhost:8090/api/salespeople/health)

if [ $? -eq 0 ]; then
    echo "✅ Mock web service is responding"
    echo ""
    echo "$HEALTH" | jq . 2>/dev/null || echo "$HEALTH"
else
    echo "❌ Mock web service is not responding"
fi

echo ""
echo ""
echo "=========================================="
echo "Data Loading Summary"
echo "=========================================="
echo ""
echo "Data Sources:"
echo "  📊 DATABASE:     Products (500) + Sales (10,000)"
echo "  📁 FILE_SYSTEM:  Customers (1,000)"
echo "  🌐 WEB_SERVICE:  Salespeople (100)"
echo ""
echo "Status:"
echo "  ✅ File system data copied"
echo "  ⏳ Database data - verify manual SQL execution"
echo "  ✅ Web service - serving 100 salespeople"
echo ""
echo "Monitor Processing:"
echo "  tail -f app.log | grep -E 'Ingesting|Processing|Fetched'"
echo ""
echo "Check Endpoints:"
echo "  # Salespeople health"
echo "  curl http://localhost:8090/api/salespeople/health | jq"
echo ""
echo "  # Application health"
echo "  curl http://localhost:8090/actuator/health | jq"
echo ""
echo "  # Get salespeople"
echo "  curl 'http://localhost:8090/api/salespeople?limit=5' | jq"
echo ""
echo "=========================================="
