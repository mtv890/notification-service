# Cobre Notification Service

Event-driven notification and webhook delivery service built with Spring Boot 3 and hexagonal architecture.

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Security](#security)

## Architecture

The service follows **Hexagonal Architecture** (Ports & Adapters) with clear separation of concerns:

```
notification-service/
├── domain/              # Core business logic
│   ├── model/           # Domain entities
│   ├── port/            # Interfaces (Use Cases & Repositories)
│   └── service/         # Domain services
├── application/         # Application layer
│   └── rest/            # REST controllers & DTOs
└── infrastructure/      # External implementations
    ├── persistence/     # JPA repositories
    ├── http/            # HTTP clients
    └── config/          # Configuration
```

### Key Components
1. **Notification Dispatcher** - Validates and enriches events
2. **Webhook Delivery Worker** - Executes deliveries with retry logic
3. **Query Service** - REST API for self-service
4. **Event Store** - PostgreSQL with partitioning

## Features
**Event Notification Delivery**
- HTTPS webhook delivery
- Exponential backoff retry strategy (7 attempts over 24h)
- Circuit breaker pattern
- HMAC signature verification
- Idempotency support

**Self-Service API**
- Query notifications with filters (date, status)
- View notification details
- Replay failed deliveries
- OAuth2/JWT authentication

**Observability**
- Prometheus metrics
- Distributed tracing (Jaeger)
- Structured JSON logging
- Real-time alerting

**Security**
- OAuth2 Resource Server
- Client isolation
- SSRF protection
- Rate limiting
- Input validation

## Prerequisites
- Java 17+
- Gradle 8.5+ (included via Gradle Wrapper)
- PostgreSQL 14+
- Apache Kafka 3.x (optional for event bus)
- Docker & Kubernetes (for deployment)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/cobre/notification-service.git
cd notification-service
```

### 2. Configure Database

```bash
# Create database
createdb cobre_notifications

# Update application.yml
vim src/main/resources/application.yml
```

Set your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cobre_notifications
    username: your_username
    password: your_password
```

### 3. Load Test Data

Place `notification_events.json` in `src/main/resources/`:

```bash
cp notification_events.json src/main/resources/
```

### 4. Build and Run

```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Or use the convenience script
make build
make run
```

The service will start on `http://localhost:8080`

### 5. Verify Health

```bash
curl http://localhost:8080/actuator/health
```

## API Documentation

### Authentication

All endpoints require JWT Bearer token with `client_id` claim:

```bash
Authorization: Bearer <your-jwt-token>
```

### Endpoints

#### 1. Query Notification Events

```http
GET /api/v1/notification_events?eventDateFrom=2024-01-01T00:00:00Z&deliveryStatus=DELIVERED&page=0&size=20
```

**Query Parameters:**
- `eventDateFrom` (optional) - ISO 8601 timestamp
- `eventDateTo` (optional) - ISO 8601 timestamp
- `deliveryStatus` (optional) - PENDING, RETRYING, DELIVERED, FAILED
- `page` (default: 0)
- `size` (default: 20)

**Response:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "clientId": "client-123",
      "eventType": "account.balance_updated",
      "eventData": "{\"balance\": 1000}",
      "createdAt": "2024-12-20T10:30:00Z",
      "webhookUrl": "https://webhook.example.com",
      "deliveryStatus": "DELIVERED",
      "deliveryAttempts": 1,
      "deliveredAt": "2024-12-20T10:30:05Z",
      "responseCode": 200
    }
  ],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5
}
```

#### 2. Get Notification Event Details

```http
GET /api/v1/notification_events/{notification_event_id}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "clientId": "client-123",
  "eventType": "payment.completed",
  "eventData": "{\"amount\": 100, \"currency\": \"USD\"}",
  "createdAt": "2024-12-20T10:30:00Z",
  "webhookUrl": "https://webhook.example.com/payments",
  "deliveryStatus": "DELIVERED",
  "deliveryAttempts": 1,
  "lastAttemptAt": "2024-12-20T10:30:05Z",
  "deliveredAt": "2024-12-20T10:30:05Z",
  "responseCode": 200
}
```

#### 3. Replay Failed Notification

```http
POST /api/v1/notification_events/{notification_event_id}/replay
```

**Requirements:**
- Notification must be in `FAILED` status
- Client must own the notification

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "deliveryStatus": "PENDING",
  "deliveryAttempts": 0
}
```

### Swagger UI

Interactive API documentation: `http://localhost:8080/swagger-ui.html`

## Configuration

### Key Configuration Properties

```yaml
# application.yml

# Database
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cobre_notifications
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# Security
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.cobre.com
          jwk-set-uri: https://auth.cobre.com/.well-known/jwks.json

# Webhook Delivery
webhook:
  delivery:
    timeout: 30s
    max-retry-attempts: 7
    retry-delays: 60,300,900,3600,14400,43200  # seconds

# Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      webhookDelivery:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
```

### Environment Variables

```bash
DB_USERNAME=notification_user
DB_PASSWORD=your_secure_password
JWT_ISSUER_URI=https://auth.cobre.com
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Testing

### Run All Tests

```bash
./gradlew test
# Or
make test
```

### Run Integration Tests

```bash
./gradlew integrationTest
# Or
make integration-test
```

### Test Coverage

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
# Or
make test-coverage
```

### Manual Testing with cURL

```bash
# Get JWT token (example)
TOKEN="your-jwt-token"

# Query events
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/notification_events?page=0&size=10"

# Get specific event
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/notification_events/123e4567-e89b-12d3-a456-426614174000"

# Replay failed notification
curl -X POST -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/notification_events/123e4567-e89b-12d3-a456-426614174000/replay"
```

## Deployment

### Local Development with Docker Compose

```bash
# Start all services (PostgreSQL, Kafka, Prometheus, Grafana)
docker-compose up -d

# View logs
docker-compose logs -f notification-service

# Stop all services
docker-compose down -v
```

### Docker Build

```bash
docker build -t cobre/notification-service:1.0.0 .
docker push cobre/notification-service:1.0.0

# Or use Makefile
make docker-build
```

### Database Migration

Flyway migrations run automatically on startup. Manual execution:

```bash
./gradlew flywayMigrate
```

## Monitoring

### Metrics

Prometheus metrics endpoint: `/actuator/prometheus`

**Key Metrics:**
- `webhook.delivery.success` - Successful deliveries
- `webhook.delivery.failure` - Failed deliveries
- `webhook.delivery.duration` - Delivery latency
- `http.server.requests` - API requests

### Grafana Dashboard

Import dashboard: `grafana/notification-service-dashboard.json`

**Panels:**
- Delivery success rate
- Average delivery latency (p50, p95, p99)
- Retry queue depth
- API error rate
- Circuit breaker status

### Logging

Structured JSON logs with correlation IDs:

```json
{
  "timestamp": "2024-12-20T10:30:00.000Z",
  "level": "INFO",
  "correlationId": "abc-123",
  "clientId": "client-123",
  "eventId": "uuid",
  "message": "Successfully delivered notification"
}
```

### Alerting Rules

```yaml
# Prometheus alerts
- alert: HighDeliveryFailureRate
  expr: rate(webhook_delivery_failure_total[5m]) > 0.05
  annotations:
    summary: "High webhook delivery failure rate"

- alert: HighAPILatency
  expr: histogram_quantile(0.99, http_server_requests_seconds_bucket) > 5
  annotations:
    summary: "API p99 latency > 5s"
```

## Security

### OWASP Top 10 Mitigations

1. **Broken Authentication (A07:2021)**
   - OAuth2/JWT authentication
   - Client ID validation
   - Rate limiting

2. **Injection (A03:2021)**
   - Parameterized queries (JPA)
   - Input validation
   - SSRF protection for webhook URLs

3. **Security Logging (A09:2021)**
   - Structured audit logs
   - Sensitive data masking
   - Centralized log management