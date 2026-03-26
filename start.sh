#!/bin/bash

# Cadoobi Transactions - Quick Start Script
set -e

echo "🚀 Starting Cadoobi Transactions..."
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "   Visit: https://www.docker.com/products/docker-desktop/"
    exit 1
fi

# Check if Docker Compose is available
if ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose is not available."
    exit 1
fi

echo "✅ Docker is available"
echo ""

# Check if services are already running
if docker ps | grep -q "cadoobi-transactions"; then
    echo "⚠️  Services are already running!"
    echo ""
    read -p "Do you want to restart them? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🔄 Restarting services..."
        docker-compose down
    else
        echo "ℹ️  Keeping existing services running"
        exit 0
    fi
fi

# Start services
echo "📦 Building and starting services..."
docker-compose --profile tools up -d

# Wait for services to be ready
echo ""
echo "⏳ Waiting for services to be ready..."
sleep 5

# Check if app is healthy
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo ""
        echo "✅ Services are ready!"
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        echo ""
        echo "❌ Services failed to start within timeout"
        echo "   Check logs with: docker-compose logs"
        exit 1
    fi

    echo -n "."
    sleep 2
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 Cadoobi Transactions is running!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📡 API:      http://localhost:8081"
echo "🩺 Health:   http://localhost:8081/actuator/health"
echo "📊 PgAdmin:  http://localhost:5050"
echo "            (admin@cadoobi.com / admin123)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test API
echo "🧪 Testing API..."
if curl -s http://localhost:8081/operators | grep -q "\[\]" || curl -s http://localhost:8081/operators | grep -q "WAVE"; then
    echo "✅ API is responding correctly"
    echo ""
    echo "📋 Available operators:"
    curl -s http://localhost:8081/operators | python3 -m json.tool 2>/dev/null || echo "   (Install python3 to pretty-print JSON)"
else
    echo "⚠️  API might not be fully initialized yet"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📚 Useful commands:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  View logs:          docker-compose logs -f"
echo "  Stop services:      docker-compose stop"
echo "  Restart services:   docker-compose restart"
echo "  Remove everything:  docker-compose down -v"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
