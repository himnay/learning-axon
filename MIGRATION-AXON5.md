# <span style="color:hsl(60,80%,50%)">Axon 5 migration — still blocked (AMQP), evidence and recommendation</span>

**Verdict: stay on Axon 4.13.x for now.** Re-checked on Maven Central on 2026-09-24 (as of 2026):
Axon Framework 5 is **GA** (`5.3.2`) and its Spring Boot starter is published under the new
coordinate — but the **AMQP extension still has no 5.x release**, and this repo's cross-service
event distribution runs over RabbitMQ. Migrating means replacing the transport, not bumping a version.

> Correction: an earlier version of this document (2026-07-12) claimed Axon 5 was milestone-only and
> the Spring starter unpublished. That was wrong — 5.x is GA and `5.3.2` is current. Only the AMQP
> blocker was real.

## <span style="color:hsl(198,80%,58%)">What was checked?</span>

Read `maven-metadata.xml` on `repo1.maven.org` for every coordinate this project depends on:

| Artifact                                                              | Latest 4.x | Latest 5.x                 | Status                                         |
|-----------------------------------------------------------------------|------------|----------------------------|------------------------------------------------|
| `org.axonframework:axon-bom` / `axon-messaging` / `axon-eventsourcing` | 4.13.3     | **5.3.2**                  | GA                                             |
| `org.axonframework.extensions.spring:axon-spring-boot-starter`        | —          | **5.3.2**                  | GA — new coordinate for 5.x                    |
| `org.axonframework:axon-spring-boot-starter`                          | 4.13.3     | `5.0.0-preview` (dead end) | Old coordinate; use the extensions one for 5.x |
| `org.axonframework.extensions.amqp:axon-amqp`                         | **4.12.0** | *(none)*                   | **Blocker** — no 5.x                           |

## <span style="color:hsl(335,80%,58%)">Why does this block *this* repository?</span>

- Services exchange events over RabbitMQ via `axon-amqp` (`SpringAMQPPublisher` + `SpringAMQPMessageSource`).
  Without a 5.x AMQP extension, sagas and projections in other services would stop receiving events.
- Options when migrating anyway:
  1. Replace RabbitMQ with **Axon Server** — the transport AxonIQ supports first-class in 5.x.
  2. Hand-roll Spring AMQP listeners that republish into the Axon 5 event bus — a sizeable rewrite.
- Axon 5 is also a real API redesign, not a drop-in: dynamic consistency boundaries
  (`EventStoreTransaction` / `AppendCondition`), new entity model, declarative interceptors.

## <span style="color:hsl(113,80%,58%)">What was the "Spring Boot 4 incompatibility" — and is it fixed?</span>

The integration tests used to be `@Disabled`, blaming an Axon 4 `javax.persistence` vs Boot 4
`jakarta.persistence` mismatch. **That rationale was false** — Axon 4.13 is Jakarta-based. The services
failed to start for unrelated reasons, all fixed on this branch without Axon 5:

| Symptom | Real cause | Fix |
|---|---|---|
| `NoClassDefFoundError: com/fasterxml/jackson/datatype/jsr310/JavaTimeModule` | Boot 4 ships Jackson 3 (`tools.jackson`); Axon 4's `JacksonSerializer` is Jackson 2 | Explicit `jackson-datatype-jsr310` in `axon-shared` |
| `"key" is null` creating `accountAggregateFactory` | Hand-built repository / aggregate factory | `@Aggregate(snapshotTriggerDefinition = …, cache = …)` + trigger bean |
| H2 `Unknown data type: BLOB` | `MODE=PostgreSQL` on the H2 URL | Removed the mode |
| No `AxonConfiguration` bean for the deadline manager | Legacy Spring `AxonConfiguration` bean is gone in 4.6+ | Inject `org.axonframework.config.Configuration` |
| `ForbiddenClassException` reading events (tests only) | Test profile used XStream, whose allow-list rejects domain classes | Test profile uses Jackson, like main |

## <span style="color:hsl(250,80%,58%)">Recommendation</span>

1. **Keep Axon 4.13.x** — maintained and, as shown above, Boot 4 compatible.
2. Track [extension-amqp releases](https://github.com/AxonFramework/extension-amqp/releases); re-attempt when a 5.x ships.
3. To move to Axon 5 sooner, pick the transport first (Axon Server is the lowest-friction choice).

## <span style="color:hsl(28,80%,58%)">Further reading</span>

- [Axon Framework 5 Migration Guide](https://docs.axoniq.io/axon-framework-reference/5.0/migration/)
- [Prerequisites and system requirements](https://docs.axoniq.io/axon-framework-reference/5.0/migration/prerequisites/)
- [AxonFramework releases](https://github.com/AxonFramework/AxonFramework/releases)
- [AMQP extension repo](https://github.com/AxonFramework/extension-amqp)
