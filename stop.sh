#!/bin/bash

# Cadoobi Transactions - Stop Script
set -e

echo "🛑 Stopping Cadoobi Transactions..."
echo ""

# Check if services are running
if ! docker ps | grep -q "cadoobi-transactions"; then
    echo "ℹ️  No services are currently running"
    exit 0
fi

# Ask what to do
echo "What would you like to do?"
echo ""
echo "  1) Stop services (keep data)"
echo "  2) Stop and remove containers (keep data)"
echo "  3) Stop and remove everything including data"
echo "  4) Cancel"
echo ""
read -p "Choose option (1-4): " -n 1 -r
echo ""
echo ""

case $REPLY in
    1)
        echo "⏸️  Stopping services..."
        docker-compose stop
        echo "✅ Services stopped (data preserved)"
        echo "   Run './start.sh' or 'docker-compose start' to restart"
        ;;
    2)
        echo "🗑️  Stopping and removing containers..."
        docker-compose down
        echo "✅ Containers removed (data preserved)"
        echo "   Run './start.sh' or 'docker-compose up -d' to restart"
        ;;
    3)
        echo "⚠️  This will DELETE all data including:"
        echo "   - Database contents"
        echo "   - All operators and transactions"
        echo "   - All gift cards"
        echo ""
        read -p "Are you sure? Type 'yes' to confirm: " CONFIRM
        if [ "$CONFIRM" = "yes" ]; then
            echo "🗑️  Removing everything..."
            docker-compose down -v
            echo "✅ All services and data removed"
            echo "   Run './start.sh' to start fresh"
        else
            echo "❌ Cancelled"
        fi
        ;;
    4)
        echo "❌ Cancelled"
        exit 0
        ;;
    *)
        echo "❌ Invalid option"
        exit 1
        ;;
esac

echo ""
