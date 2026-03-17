#!/bin/bash

echo "Building application..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "Starting application..."
    java --enable-preview -jar target/kafka-stream-ingestor-1.0.0.jar
else
    echo "Build failed!"
    exit 1
fi
