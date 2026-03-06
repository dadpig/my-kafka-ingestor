#!/bin/bash

echo "Starting Kafka infrastructure..."
docker-compose up -d

echo "Waiting for Kafka to be ready..."
sleep 10

echo "Checking Kafka status..."
docker-compose ps

echo ""
echo "Kafka infrastructure is ready!"
echo ""
echo "Services:"
echo "  - Kafka Broker: localhost:9092"
echo "  - Zookeeper: localhost:2181"
echo "  - Schema Registry: http://localhost:8081"
echo "  - Kafka UI: http://localhost:8080"
echo ""
echo "To stop: docker-compose down"
echo "To view logs: docker-compose logs -f"
