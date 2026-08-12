# PowerShell script to verify Kafka topics and consumer groups inside docker container
# Make sure the 'chat_kafka' docker container is running before executing this.

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   Aura Chat Platform - Kafka QA Diagnostics" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Check Kafka container status
$container = docker ps -f "name=chat_kafka" --format "{{.Names}}"
if (-not $container) {
    Write-Error "Container 'chat_kafka' is NOT running. Please start it using 'docker compose up -d' first."
    exit
}

# 2. List all topics on the KRaft broker
Write-Host "`n1. Listing Kafka Topics:" -ForegroundColor Yellow
docker exec chat_kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# 3. List active Consumer Groups
Write-Host "`n2. Listing Active Consumer Groups:" -ForegroundColor Yellow
docker exec chat_kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# 4. Describe Consumer Group details and offset lags (e.g. notification-service, search-service, realtime-service groups)
Write-Host "`n3. Checking consumer group lags (e.g. 'notification-service-group'):" -ForegroundColor Yellow
docker exec chat_kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-service-group

Write-Host "`nChecking consumer group lags for 'search-service-group':" -ForegroundColor Yellow
docker exec chat_kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group search-service-group

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "Diagnostics complete." -ForegroundColor Cyan
