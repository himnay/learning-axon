# Axon 5 migration — blocked, evidence and recommendation

**Verdict: do not migrate yet.** As of 2026-07-12, Axon Framework 5's Spring Boot
integration and AMQP extension are not published as stable (or even milestone) artifacts
on Maven Central. This repo depends on both — the migration is not currently possible
without dropping AMQP-based cross-service event distribution and hand-rolling Spring
wiring against a pre-release milestone. Main branch is untouched; this document lives only
on `axon5-migration`.

## What was checked

Queried Maven Central's search API directly (`search.maven.org/solrsearch`) for every
artifact this project depends on, rather than trusting blog-post version numbers:

| Artifact | Latest 4.x | Latest 5.x found | Status |
|---|---|---|---|
| `org.axonframework:axon-messaging` | 4.11.2 | `5.0.0-M1` | **Milestone only**, not GA |
| `org.axonframework:axon-eventsourcing` | 4.11.2 | `5.0.0-M1` | **Milestone only**, not GA |
| `org.axonframework:axon-modelling` | 4.11.2 | `5.0.0-M1` | **Milestone only**, not GA |
| `org.axonframework:axon-test` | 4.11.2 | `5.0.0-M1` | **Milestone only**, not GA |
| `org.axonframework:axon-configuration` | 4.11.2 | *(none)* | No 5.x release at all yet |
| `org.axonframework:axon-spring-boot-starter` | 4.11.2 | — | Coordinate retired for 5.x |
| `org.axonframework.extensions.spring:axon-spring-boot-starter` | — | — | **Zero artifacts published under this group ID at all** |
| `org.axonframework.extensions.amqp:axon-amqp` | 4.11.0 | — | **No 5.x release** |
| `org.axonframework:axon-amqp` (old coordinate) | 4.9.0 (pinned here) | — | Superseded, never had a 5.x release either |

Source: the [Axon 4→5 migration guide](https://docs.axoniq.io/axon-framework-reference/5.0/migration/)
confirms the coordinate moves ([spring modules → `org.axonframework.extensions.spring`](https://discuss.axoniq.io/t/the-axon-framework-4-to-5-migration-guide-is-now-live/6579))
and states JDK 21+ and **Spring Boot 3+** as prerequisites — but the artifact that would
actually let a Spring Boot app wire up Axon 5 doesn't exist on Central under either the
old or the documented new coordinate. `5.0.0-M1` is a **milestone** (pre-release) build of
the core messaging/eventsourcing/modelling/test jars; there is no `5.0.0` GA, `5.1.x`, or
`5.2.x` published anywhere I could resolve, despite a July 2026 AxonIQ forum post
referencing "Axon and Axoniq Framework — Release 5.2.0". That post may describe the
**Axon Server** product release train or a not-yet-mirrored-to-Central build — either way,
`mvn` cannot resolve it today.

## Why this blocks *this* repository specifically

- `axon-command-service`, `axon-query-service`, `axon-saga-service`, `axon-debit-card-service`,
  `axon-cheque-book-service` are all Spring Boot apps wired via `axon-spring-boot-starter` —
  with no 5.x Spring integration published, there is no supported way to bootstrap the
  Axon 5 `Configuration`/message buses inside a Spring context at all (hand-wiring the raw
  `Configurer` API against a milestone build is possible but throws away Spring Boot
  autoconfiguration, actuator integration, and property-based config this project relies on).
- Cross-service event distribution goes over RabbitMQ (`axon.amqp.version=4.9.0` in the
  root pom) via the AMQP extension. With no 5.x AMQP extension, sagas/queries would stop
  receiving events from other services entirely unless that transport were replaced
  (e.g. hand-rolled Spring AMQP listeners re-publishing into Axon 5's event bus — a
  substantial rewrite, not a version bump).

## What *is* real about Axon 5

- The core event-sourcing/messaging API redesign (dynamic consistency boundaries via
  `EventStoreTransaction`/`AppendCondition`, declarative handler interceptors) is real and
  documented, and does target Jakarta/Spring Boot 4 per the roadmap.
- The migration guide and API-changes doc are live and detailed — AxonIQ is clearly
  building toward GA.
- `5.0.0-M1` can be pulled and experimented with standalone (no Spring, no AMQP) if you
  want to prototype the new Aggregate/EventStoreTransaction API in isolation — but that's
  a toy exercise, not a path to migrating this repo's five Spring Boot services.

## Recommendation

1. **Keep this repo on Axon 4.13.1** until AxonIQ publishes a 5.x GA (or at least a
   release candidate) of both `axon-spring-boot-starter`'s successor and the AMQP
   extension. Track https://github.com/AxonFramework/AxonFramework/releases and
   https://github.com/AxonFramework/extension-amqp/releases.
2. The documented Spring Boot 4 / JPA event-store incompatibility (why `@SpringBootTest`
   integration tests are `@Disabled` today) is a **separate, smaller problem** — it may be
   fixable by pinning `spring-boot-starter-parent` to 3.x for this repo specifically
   (accepting Boot 3 instead of chasing Boot 4/Axon 5 simultaneously), which would
   re-enable the disabled integration tests without waiting on Axon 5 at all. Worth doing
   as a follow-up if the disabled tests are a priority sooner than an Axon 5 GA.
3. Re-run the Maven Central checks above periodically (or when AxonIQ announces a GA) —
   this document should be deleted and the migration re-attempted once
   `org.axonframework.extensions.spring:axon-spring-boot-starter` and
   `org.axonframework.extensions.amqp:axon-amqp` both show a real (non-milestone) 5.x
   version.

## Further reading

- [Axon Framework 5 Migration Guide](https://docs.axoniq.io/axon-framework-reference/5.0/migration/)
- [Prerequisites and system requirements](https://docs.axoniq.io/axon-framework-reference/5.0/migration/prerequisites/)
- [API changes reference](https://github.com/AxonIQ/AxonFramework/blob/main/axon-5/api-changes/index.md)
- [AMQP extension repo](https://github.com/AxonFramework/extension-amqp)
- [Migrating from Axon 4 to 5: What We Learned — SaaSForge](https://saasforge.cz/blog/axon-framework-4-to-5-migration/)
