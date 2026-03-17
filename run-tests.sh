#!/bin/bash

echo "Running Kafka Stream Ingestor Tests..."
echo ""

echo "=========================================="
echo "1. Unit Tests"
echo "=========================================="
mvn test -Dtest=*ServiceTest,*RepositoryTest

echo ""
echo "=========================================="
echo "2. Controller Tests"
echo "=========================================="
mvn test -Dtest=*ControllerTest

echo ""
echo "=========================================="
echo "3. Kafka Streams Tests"
echo "=========================================="
mvn test -Dtest=*StreamProcessorTest

echo ""
echo "=========================================="
echo "4. Integration Tests"
echo "=========================================="
mvn test -Dtest=*IntegrationTest

echo ""
echo "=========================================="
echo "5. Test Coverage Report"
echo "=========================================="
mvn jacoco:report

echo ""
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo ""
echo "✅ All tests completed!"
echo ""
echo "Coverage report available at:"
echo "  target/site/jacoco/index.html"
echo ""
echo "View report:"
echo "  open target/site/jacoco/index.html"
