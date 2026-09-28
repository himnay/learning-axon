# <span style="color:hsl(60,80%,50%)">Axon 5 migration — not started: blocked, evidence and recommendation</span>

**Verdict: stay on Axon Framework 4.13 for now.** This branch (`axon5-migration`) holds no Axon 5 code: it
builds with `axon-bom` 4.13.3 (which manages the 4.13.2 framework modules) on Spring Boot 4.1.1 and Java 27.
Re-checked on 2026-09-28 against Maven Central and the Axon Framework 5.3.2 reference guide: Axon Framework 5
is GA (`5.3.2`), but its core no longer has three things this repository is built around (sagas, the deadline
manager and scatter-gather queries), and the AMQP extension still has no 5.x release. Migrating means
rewriting the saga service and replacing the transport, not bumping a version.

> Correction history: the 2026-07-12 version of this document said Axon 5 was milestone-only (wrong: it is
> GA). The 2026-09-24 version said the migration was blocked only by the AMQP extension (incomplete: see the
> feature table below).

## <span style="color:hsl(198,80%,58%)">What does Axon 5 no longer have that this repository uses?</span>

| This repository (Axon 4)                                                                               | Axon Framework 5.3.2                                                                                                                                                                                                                                                                     |
|--------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Saga` `AccountManagementSagaOrchestrator`, tested with `SagaTestFixture`                             | "Axon Framework 5 has no `Saga` construct." An `axon-legacy` module, meant only to let running Axon 4 sagas finish, is "scheduled for 5.4.0". New processes are written as plain event handlers with your own state, or as Workflows (Axoniq Framework: commercial, currently a preview) |
| `DeadlineManager` + `@DeadlineHandler` in the saga service's `AccountAggregate`                        | Not in the core either (same `axon-legacy` note). The replacement is a projection of outstanding work plus a scheduled sweep that sends a command, or a Workflow `sleep(...)`                                                                                                            |
| Scatter-gather (`QueryBus.scatterGather`, two `@QueryHandler(queryName = "scatter-gather")` handlers)  | Not supported: one handler per query name, a second registration throws `DuplicateQueryHandlerSubscriptionException`                                                                                                                                                                     |
| `axon-amqp` (`SpringAMQPPublisher` / `SpringAMQPMessageSource`) between command and query service      | No 5.x release; the latest is 4.12.0 (2025-07-25)                                                                                                                                                                                                                                        |
| Snapshots through `EventCountSnapshotTriggerDefinition`                                                | Restored in 5.1, but "no longer centered around `SnapshotTriggerDefinition`"                                                                                                                                                                                                             |
| `org.axonframework:axon-server-connector` (excluded here, but the way to run the saga across services) | Moved to the commercial Axoniq Framework as `io.axoniq.framework:axon-server-connector`                                                                                                                                                                                                  |

## <span style="color:hsl(335,80%,58%)">What was checked on Maven Central?</span>

Read `maven-metadata.xml` on `repo1.maven.org` for every coordinate involved:

| Artifact                                                                     | Latest 4.x                          | Latest 5.x                 | Note                                           |
|------------------------------------------------------------------------------|-------------------------------------|----------------------------|------------------------------------------------|
| `org.axonframework:axon-bom`                                                 | 4.13.3 (manages the 4.13.2 modules) | —                          | Axon 5 has a new BOM, next row                 |
| `org.axonframework:axon-framework-bom`                                       | —                                   | **5.3.2**                  | GA, 2026-09-10                                 |
| `org.axonframework:axon-messaging` / `axon-eventsourcing` / `axon-modelling` | 4.13.2                              | **5.3.2**                  | GA                                             |
| `org.axonframework.extensions.spring:axon-spring-boot-starter`               | —                                   | **5.3.2**                  | New coordinate for 5.x                         |
| `org.axonframework:axon-spring-boot-starter`                                 | 4.13.2                              | `5.0.0-preview` (dead end) | Old coordinate; use the extensions one for 5.x |
| `org.axonframework.extensions.amqp:axon-amqp`                                | **4.12.0**                          | *(none)*                   | **Blocker** — no 5.x                           |

## <span style="color:hsl(113,80%,58%)">What would the migration take?</span>

<ul>

- **Transport first.** Replace RabbitMQ with Axon Server (its connector is part of the commercial Axoniq
  Framework in 5.x), or hand-roll Spring AMQP listeners that republish into the Axon 5 event bus.
- **Rewrite the saga and its deadline.** Turn `AccountManagementSagaOrchestrator` into an event-handling
  process manager with explicit state (a JPA table or an event-sourced entity), and turn the 60-second
  inactivity deadline into a projection plus a scheduled sweep, or use Workflows.
- **Drop or replace the scatter-gather demo** (for example with a streaming or subscription query).
- **Port the aggregates and tests** to the Axon 5 entity model and `AxonTestFixture`; the framework ships
  OpenRewrite recipes for the mechanical part.

</ul>

## <span style="color:hsl(250,80%,58%)">What was the "Spring Boot 4 incompatibility" — and is it fixed?</span>

The integration tests used to be `@Disabled`, blaming an Axon 4 `javax.persistence` vs Boot 4
`jakarta.persistence` mismatch. **That rationale was false** — Axon 4.13 is Jakarta-based. The services
failed to start, or silently misbehaved, for unrelated reasons, all fixed on this branch without Axon 5:

| Symptom                                                                      | Real cause                                                                                                                                               | Fix                                                                                                             |
|------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `NoClassDefFoundError: com/fasterxml/jackson/datatype/jsr310/JavaTimeModule` | Boot 4 ships Jackson 3 (`tools.jackson`); Axon 4's `JacksonSerializer` is Jackson 2                                                                      | Explicit `jackson-datatype-jsr310` in `axon-shared`                                                             |
| `"key" is null` creating `accountAggregateFactory`                           | Hand-built repository / aggregate factory                                                                                                                | `@Aggregate(snapshotTriggerDefinition = …, cache = …)` + trigger bean                                           |
| H2 `Unknown data type: BLOB`                                                 | `MODE=PostgreSQL` on the H2 URL                                                                                                                          | Removed the mode                                                                                                |
| No `AxonConfiguration` bean for the deadline manager                         | Legacy Spring `AxonConfiguration` bean is gone in 4.6+                                                                                                   | Inject `org.axonframework.config.Configuration`                                                                 |
| `ForbiddenClassException` reading events (tests only)                        | Test profile used XStream, whose allow-list rejects domain classes                                                                                       | Test profile uses Jackson, like main                                                                            |
| Command service never published an event to RabbitMQ (found 2026-09-28)      | `axon-amqp` 4.12's `@AutoConfigureAfter` names Boot 3's `RabbitAutoConfiguration` package, so its `@ConditionalOnBean(ConnectionFactory)` runs too early | `AmqpPublisherConfig` declares the `SpringAMQPPublisher`; both services declare the exchange, queue and binding |

## <span style="color:hsl(28,80%,58%)">Recommendation</span>

1. **Keep Axon 4.13.x** — maintained and, as shown above, working on Boot 4 and Java 27.
2. Re-evaluate when `axon-legacy` ships (announced for 5.4.0) and there is a transport story for 5.x
   ([extension-amqp releases](https://github.com/AxonFramework/extension-amqp/releases)).
3. To move to Axon 5 sooner, budget for the saga, deadline and scatter-gather rewrites above, not only
   for the transport.

## <span style="color:hsl(165,80%,58%)">Further reading</span>

- [Axon Framework 5 Migration Guide](https://docs.axoniq.io/axon-framework-reference/5.0/migration/)
- [Prerequisites and system requirements](https://docs.axoniq.io/axon-framework-reference/5.0/migration/prerequisites/)
- [Saga migration path (5.3.2 source)](https://github.com/AxonIQ/AxonFramework/blob/axon-5.3.2/docs/reference-guide/modules/migration/pages/paths/sagas.adoc)
- [Snapshotting migration path (5.3.2 source)](https://github.com/AxonIQ/AxonFramework/blob/axon-5.3.2/docs/reference-guide/modules/migration/pages/paths/snapshotting.adoc)
- [Query dispatchers — no scatter-gather in 5.x](https://docs.axoniq.io/axon-framework-reference/5.0/queries/query-dispatchers/)
- [AxonFramework releases](https://github.com/AxonIQ/AxonFramework/releases)
- [AMQP extension repo](https://github.com/AxonFramework/extension-amqp)
