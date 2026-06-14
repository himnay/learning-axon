# Learning Axon — CQRS + Event Sourcing + Saga

A multi-module Maven project demonstrating **CQRS**, **Event Sourcing**, and the **Saga pattern** using Axon Framework 4.10.3, Spring Boot 4.1.0, and Java 25.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (Insomnia / curl)               │
└──────────────────────────────┬──────────────────────────────┘
                               │ REST
           ┌───────────────────┼────────────────────┐
           │                   │                    │
    ┌──────▼──────┐    ┌───────▼──────┐    ┌───────▼──────┐
    │  command    │    │    saga      │    │   query      │
    │  service   │    │   service    │    │   service    │
    │  :8080     │    │   :8082      │    │   :8085      │
    └──────┬──────┘    └───────┬──────┘    └──────▲───────┘
           │  Event Store       │  Axon Bus         │
           │  (JPA/H2)         │                   │ AMQP
           └────────────────────┼───────────────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
       ┌──────▼──────┐  ┌───────▼───────┐        │
       │  debit-card │  │ cheque-book   │    RabbitMQ
       │  service    │  │ service       │
       │  :8083      │  │ :8090         │
       └─────────────┘  └───────────────┘
```

### Saga Flow

```
AccountActivatedEvent
        │
        ▼
  IssueDebitCardCommand ──(fail)──► CancelIssuedDebitCardCommand
        │
        ▼ (success) DebitCardIssuedEvent
        │
        ▼
  IssueChequeBookCommand ──(fail)──► CancelIssuedChequeBookCommand
        │                                       + CancelIssuedDebitCardCommand
        ▼ (success) ChequeBookIssuedEvent
        │
        ▼
  AccountUpdateCommand → AccountUpdatedEvent → SAGA END
```

---

## Modules

| Module | Role | Port |
|--------|------|------|
| `axon-shared` | Shared library: commands, events, queries, models, enums | — |
| `axon-command-service` | CQRS command side — AccountAggregate, snapshot, replay | 8080 |
| `axon-query-service` | CQRS query side — AMQP listener, JPA projection, subscription queries | 8085 |
| `axon-saga-service` | Saga orchestrator — AccountAggregate, deadline manager | 8082 |
| `axon-debit-card-service` | Saga participant — DebitCardAggregate | 8083 |
| `axon-cheque-book-service` | Saga participant — ChequeBookAggregate | 8090 |

---

## GoF Design Patterns

| Pattern | Category | Where Used |
|---------|----------|------------|
| **Command** | Behavioral | All Axon command classes (`CreateAccountCommand`, `IssueDebitCardCommand`, …) |
| **Observer** | Behavioral | All `@EventHandler` / `@EventSourcingHandler` methods; Axon event bus |
| **Chain of Responsibility** | Behavioral | `CommandGateway → CommandBus → CommandHandler`; Saga rollback chain |
| **Template Method** | Behavioral | Service interface + impl pattern (`AccountCommandService` / `AccountCommandServiceImpl`) |
| **Builder** | Creational | Lombok `@Builder` on `IssueDebitCardCommand`, `IssueChequeBookCommand`, `AccountUpdateCommand`, … |
| **Factory Method** | Creational | `accountAggregateRepository` bean in `AxonSnapshotConfig` (creates `AccountAggregate` via `SpringPrototypeAggregateFactory`) |
| **Strategy** | Behavioral | `EventProcessingConfigurer.usingSubscribingEventProcessors()` vs `usingTrackingEventProcessors()` |
| **Singleton** | Creational | All Spring beans (`@Service`, `@Repository`, `@Component`) |

---

## Tech Stack

| Technology | Version |
|-----------|---------|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Axon Framework | 4.10.3 |
| Axon AMQP Extension | 4.10.3 |
| Maven | 3.9.x |
| H2 (embedded) | — |
| PostgreSQL | 16 (Docker) |
| RabbitMQ | 3 (Docker) |
| TestContainers | 1.21.3 |
| JUnit | 5 |
| Prometheus / Grafana | latest |

---

## Quick Start

### 1. Start Infrastructure (Docker)

```bash
cd docker
docker compose up -d rabbitmq postgres
```

### 2. Build & Run

```bash
# Build all modules
mvn clean install -DskipTests

# Run command service
cd axon-command-service
mvn spring-boot:run

# Run query service (new terminal)
cd axon-query-service
mvn spring-boot:run

# Run saga service (new terminal)
cd axon-saga-service
mvn spring-boot:run

# Run debit card service (new terminal)
cd axon-debit-card-service
mvn spring-boot:run

# Run cheque book service (new terminal)
cd axon-cheque-book-service
mvn spring-boot:run
```

### 3. Run Tests

```bash
mvn test
```

> **Note:** Tests use H2 in-memory and exclude AMQP auto-configuration — no Docker needed for tests.

---

## API Reference (Command Service — port 8080)

### Create Account
```http
POST /bank-accounts
Content-Type: application/json

{
  "startingBalance": 500.00,
  "currency": "USD"
}
```

### Credit Money
```http
PUT /bank-accounts/credits/{accountId}
Content-Type: application/json

{
  "creditAmount": 150.00,
  "currency": "USD"
}
```

### Debit Money
```http
PUT /bank-accounts/debits/{accountId}
Content-Type: application/json

{
  "debitAmount": 100.00,
  "currency": "USD"
}
```

### List Events (from Axon Event Store)
```http
GET /bank-accounts/{accountId}/events
```

### Trigger Replay
```http
POST /bank-accounts/replay
```

---

## API Reference (Query Service — port 8085)

### Get Account (direct JPA)
```http
GET /bank-accounts/{accountId}
```

### Get Account (Axon point-to-point query)
```http
GET /bank-accounts/{accountId}/details
```

### Real-time Credit Notifications (SSE / subscription query)
```http
GET /bank-accounts/notify/credit/{accountId}
Accept: text/event-stream
```

### Real-time Debit Notifications (SSE)
```http
GET /bank-accounts/notify/debit/{accountId}
Accept: text/event-stream
```

---

## API Reference (Saga Service — port 8082)

### Create Account (triggers full saga)
```http
POST /bank-accounts
Content-Type: application/json

{
  "startingBalance": 1000.00,
  "currency": "EUR"
}
```

---

## Axon Concepts Demonstrated

| Concept | Module |
|---------|--------|
| Aggregate + Event Sourcing | `axon-command-service`, `axon-saga-service` |
| Snapshot (threshold=3) | `axon-command-service` (AxonSnapshotConfig) |
| Tracking Event Processor (replay) | `axon-command-service` |
| Subscribing Event Processor (AMQP) | `axon-query-service` |
| Point-to-point query | `axon-query-service` |
| Subscription query (real-time) | `axon-query-service` |
| Scatter-Gather query | `axon-query-service` |
| Saga orchestration | `axon-saga-service` |
| Compensating commands (rollback) | `axon-saga-service` |
| Deadline Manager | `axon-saga-service` |
| AMQP event routing | `axon-command-service` → `axon-query-service` |

---

## Monitoring

| Service | URL |
|---------|-----|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |
| RabbitMQ UI | http://localhost:15672 (guest/guest) |
| Axon Server UI | http://localhost:8024 |
| H2 Console (command) | http://localhost:8080/h2-console |
| H2 Console (query) | http://localhost:8085/h2-console |

All services expose `/actuator/prometheus` for Prometheus scraping.

---

## Insomnia Collection

Import `insomnia-collection.json` into Insomnia to get all requests pre-configured.

Set the `account_id` environment variable after calling _Create Account_.

---

## Testing Saga Rollback

To trigger a saga rollback in the cheque-book service, set `failure = true` in `ChequeBookAggregate`:

```java
private boolean failure = true; // simulate failure
```

Restart the cheque-book service, then call `POST /bank-accounts` on the saga service.
The saga will issue a debit card, then attempt to issue a cheque book (which fails),
and automatically dispatch compensating `CancelIssuedChequeBookCommand` + `CancelIssuedDebitCardCommand`.
