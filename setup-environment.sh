#!/bin/bash

echo "=========================================="
echo "Kafka Stream Ingestor - Environment Setup"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Step 1: Fix Java Version
echo "Step 1: Configure Java 25"
echo "=========================================="

JAVA_25_HOME=$(/usr/libexec/java_home -v 25 2>/dev/null)

if [ -n "$JAVA_25_HOME" ]; then
    echo -e "${GREEN}✅ Java 25 found at: $JAVA_25_HOME${NC}"

    # Detect shell
    SHELL_NAME=$(basename "$SHELL")

    if [ "$SHELL_NAME" = "zsh" ]; then
        RC_FILE="$HOME/.zshrc"
    else
        RC_FILE="$HOME/.bashrc"
    fi

    echo ""
    echo "Updating $RC_FILE..."

    # Remove old JAVA_HOME settings
    grep -v "JAVA_HOME.*openjdk" "$RC_FILE" > "${RC_FILE}.tmp" 2>/dev/null || touch "${RC_FILE}.tmp"

    # Add new JAVA_HOME
    echo "" >> "${RC_FILE}.tmp"
    echo "# Java 25 for Kafka Stream Ingestor" >> "${RC_FILE}.tmp"
    echo "export JAVA_HOME=\"$JAVA_25_HOME\"" >> "${RC_FILE}.tmp"
    echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\"" >> "${RC_FILE}.tmp"

    mv "${RC_FILE}.tmp" "$RC_FILE"

    # Apply to current session
    export JAVA_HOME="$JAVA_25_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"

    echo -e "${GREEN}✅ Java 25 configured in $RC_FILE${NC}"
    echo ""
    echo "Java version:"
    java --version | head -1
else
    echo -e "${RED}❌ Java 25 not found. Installing...${NC}"
    brew install openjdk@25
    JAVA_25_HOME=$(/usr/libexec/java_home -v 25)
fi

echo ""

# Step 2: Install Docker Compose
echo "Step 2: Install Docker Compose"
echo "=========================================="

if docker compose version &>/dev/null; then
    echo -e "${GREEN}✅ Docker Compose (plugin) already installed${NC}"
    docker compose version
elif command -v docker-compose &>/dev/null; then
    echo -e "${GREEN}✅ Docker Compose (standalone) already installed${NC}"
    docker-compose --version
else
    echo -e "${YELLOW}Installing Docker Compose...${NC}"

    # Install via Homebrew
    if command -v brew &>/dev/null; then
        brew install docker-compose
        echo -e "${GREEN}✅ Docker Compose installed${NC}"
    else
        echo -e "${RED}❌ Homebrew not found. Please install Homebrew first.${NC}"
        echo "Visit: https://brew.sh"
        exit 1
    fi
fi

echo ""

# Step 3: Start Docker (Colima)
echo "Step 3: Start Docker Daemon"
echo "=========================================="

if docker ps &>/dev/null; then
    echo -e "${GREEN}✅ Docker daemon is already running${NC}"
    docker ps | head -2
else
    echo -e "${YELLOW}Docker daemon not running. Starting Colima...${NC}"

    if command -v colima &>/dev/null; then
        echo "Starting Colima with 4 CPUs and 8GB RAM..."
        colima start --cpu 4 --memory 8 --disk 50

        # Wait for Docker to be ready
        echo "Waiting for Docker to be ready..."
        for i in {1..30}; do
            if docker ps &>/dev/null; then
                echo -e "${GREEN}✅ Docker is ready${NC}"
                break
            fi
            echo -n "."
            sleep 2
        done

        if ! docker ps &>/dev/null; then
            echo -e "${RED}❌ Docker failed to start. Please check Colima status.${NC}"
            exit 1
        fi
    else
        echo -e "${YELLOW}⚠️  Colima not detected. Installing...${NC}"
        brew install colima
        colima start --cpu 4 --memory 8 --disk 50
    fi
fi

echo ""

# Step 4: Verify Everything
echo "Step 4: Verify Installation"
echo "=========================================="
echo ""

echo "✓ Java Version:"
java --version | head -1
echo ""

echo "✓ Maven Version:"
mvn --version | head -1
echo ""

echo "✓ Docker Version:"
docker --version
echo ""

echo "✓ Docker Compose:"
if docker compose version &>/dev/null; then
    docker compose version
elif command -v docker-compose &>/dev/null; then
    docker-compose --version
fi
echo ""

echo "✓ Docker Status:"
docker ps | head -1
echo ""

echo "=========================================="
echo "Environment Setup Complete!"
echo "=========================================="
echo ""
echo -e "${GREEN}✅ All dependencies are ready!${NC}"
echo ""
echo "Next Steps:"
echo "  1. Source your shell configuration:"
echo "     source ~/.zshrc"
echo ""
echo "  2. Start Kafka infrastructure:"
echo "     ./start-kafka.sh"
echo ""
echo "  3. Run the application:"
echo "     ./run-app.sh"
echo ""
echo "  4. Run tests:"
echo "     ./run-tests.sh"
echo ""
echo "=========================================="
