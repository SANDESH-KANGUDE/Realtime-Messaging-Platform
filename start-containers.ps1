# Powershell script to run the 4 infrastructure containers directly via docker run
# Bypasses any Docker Compose mounting or path resolution hangs.

Write-Host "Stopping and removing any old container instances if they exist..." -ForegroundColor Cyan
docker rm -f chat_postgres chat_mongodb chat_redis chat_kafka 2>$null

# Ensure the cdac-net network exists
$networkCheck = docker network ls --filter "name=cdac-net" -q
if (-not $networkCheck) {
    Write-Host "Creating cdac-net network..." -ForegroundColor Yellow
    docker network create cdac-net
}

Write-Host "`n1. Starting PostgreSQL (chat_postgres)..." -ForegroundColor Yellow
docker run -d `
  --name chat_postgres `
  --network cdac-net `
  -p 5432:5432 `
  -e POSTGRES_DB=postgres `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=postgrespassword `
  -v chat_postgres_data:/var/lib/postgresql/data `
  postgres:16-alpine

Write-Host "`n2. Starting MongoDB (chat_mongodb)..." -ForegroundColor Yellow
docker run -d `
  --name chat_mongodb `
  --network cdac-net `
  -p 27017:27017 `
  -e MONGO_INITDB_ROOT_USERNAME=mongo `
  -e MONGO_INITDB_ROOT_PASSWORD=mongopassword `
  -v chat_mongo_data:/data/db `
  mongo:7.0

Write-Host "`n3. Starting Redis (chat_redis)..." -ForegroundColor Yellow
docker run -d `
  --name chat_redis `
  --network cdac-net `
  -p 6379:6379 `
  -v chat_redis_data:/data `
  redis:7.2-alpine

Write-Host "`n4. Starting Kafka KRaft (chat_kafka)..." -ForegroundColor Yellow
docker run -d `
  --name chat_kafka `
  --network cdac-net `
  -p 9092:9092 `
  -p 9093:9093 `
  -e KAFKA_NODE_ID=1 `
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://chat_kafka:29092,PLAINTEXT_HOST://localhost:9092 `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 `
  -e KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0 `
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 `
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 `
  -e KAFKA_PROCESS_ROLES=broker,controller `
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@chat_kafka:29093 `
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:29092,CONTROLLER://0.0.0.0:29093,PLAINTEXT_HOST://0.0.0.0:9092 `
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT `
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER `
  -e KAFKA_LOG_DIRS=/tmp/kraft-combined-logs `
  -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk `
  -v chat_kafka_data:/var/lib/kafka/data `
  confluentinc/cp-kafka:7.6.0

Write-Host "`nAll containers started successfully! Verify them in Docker Desktop." -ForegroundColor Green
docker ps
