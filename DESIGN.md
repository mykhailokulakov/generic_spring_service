# generic-spring-service — Design Document

A reference Spring Boot microservice. Forked as the starting point for new services. Layer-sliced, opinionated, production-shaped, decomposed for reuse.

This document is the source of truth for *what* we are building and *why*. The Claude Code prompt sequence in `PROMPTS.md` builds it commit by commit.

---

## 1. Stack (verified May 2026)

| Component | Version | Notes |
|---|---|---|
| Java | 25 (LTS) | Released Sept 2025 |
| Spring Boot | 4.0.6 | Latest stable, April 2026 |
| Spring Framework | 7.0.x | Brought in transitively |
| Jakarta EE | 11 | Servlet 6.1, JPA 3.2, Bean Validation 3.1 |
| Hibernate ORM | 7.x | Boot-managed |
| PostgreSQL | 17 | Production & Testcontainers |
| Keycloak | 26.x | External; Testcontainers for integration tests |
| Maven | 3.9.x | |
| MapStruct | 1.6.x | |
| Lombok | 1.18.x | + lombok-mapstruct-binding |
| springdoc-openapi | 3.x | OpenAPI 3.1 + Swagger UI. 3.x targets Spring Boot 4; 2.x is for Spring Boot 3. |
| Testcontainers | 1.20.x | Postgres + Keycloak modules |
| RestAssured | 5.5.x | Integration tests |
| ArchUnit | 1.3.x | Architectural rule enforcement |
| JaCoCo | 0.8.x | Coverage with 80% gate |
| Spotless | 2.x | Formatter (google-java-format) |
| git-cliff | 2.x | Changelog generation in release |

Versions are pinned in `pom.xml`. Every dependency must be re-verified against its release page before committing the manifest. The prompt sequence includes this step explicitly.

---

## 2. Project coordinates

| | |
|---|---|
| GitHub repo | `github.com/mykhailokulakov/generic_spring_service` |
| `groupId` | `io.github.mykhailokulakov` |
| `artifactId` | `generic-spring-service` |
| Base package | `io.github.mykhailokulakov.genericspringservice` |
| Container image | `ghcr.io/mykhailokulakov/generic-spring-service` |
| Default branch | `main` |
| License headers | None |

---

## 3. Decisions and non-decisions

This section records explicit choices we made and the rationale. Future contributors who want to revisit a decision must engage with the rationale here, not relitigate from scratch.

### 3.1 We do NOT use `AbstractPersistable` from Spring Data JPA

`org.springframework.data.jpa.domain.AbstractPersistable<PK>` exists and provides `@Id` + `equals`/`hashCode`. We deliberately do not extend it.

- It only covers identity. We need identity + audit timestamps + optimistic locking + soft delete. We'd still write our own superclass on top.
- Its `equals`/`hashCode` is id-based, which has known footguns for transient entities whose id is still null.
- The ~6 lines we'd save are not worth inheriting equality semantics we'd then have to reason around forever.

We ship our own four-class persistence chain (section 6.1). It is deliberate, not an oversight.

### 3.2 We do NOT use a service interface for the example service

`ExampleService` is a concrete `@Service` class. No interface. The "always extract an interface" reflex is a 2010 Java habit that no longer earns its keep:

- Spring proxies don't require interfaces (CGLIB is the default).
- Mockito mocks concrete classes fine.
- A second implementation is YAGNI until it isn't, and when it is, "Extract Interface" is a 5-second IDE refactor.

Interfaces appear where there are multiple implementations or where the interface is the architectural seam (e.g. `ExampleRepository extends JpaRepository<...>`, which is Spring's seam, not ours).

### 3.3 Role policy is pure Java, not YAML

`Role` and `RoleTier` are enums. No `@ConfigurationProperties`, no yaml binding. Role-tier policy doesn't legitimately change per environment for a generic reference template. Every yaml knob is configuration that has to be tested in every environment, and that cost only earns its keep if the flexibility is actually used.

A forked service that genuinely needs per-environment role policy can promote `RoleTier` to a properties class. The current shape makes that promotion easy when the need arrives, without paying the tax in the meantime.

### 3.4 Patch semantics: `null` means "not provided"

`PatchExampleRequest` maps directly to the service. `null` field = leave unchanged. Explicit value = set. There is no way for a PATCH to clear a field via this template's API.

The alternative (RFC 7396 JSON Merge Patch, where `null` clears the field) requires a custom Jackson deserializer per patch DTO to distinguish "field absent" from "field present and null." Real cost, real complexity, not worth it for a reference template. README points to JSON Merge Patch as the path forward if a fork needs clearing semantics.

### 3.5 E2E test scope: in-process integration tests only

The integration test suite (50+ tests covering every endpoint, 5+ positive and 5+ negative each) runs the full Spring context with real Postgres and real Keycloak via Testcontainers. This *is* our E2E. We do not ship a separate test set against the packaged container image — the CI `build` job builds and Trivy-scans the image, which proves it builds and is free of known CVEs; we go no further.

We do not ship dormant cross-service test infrastructure (WireMock for outbound HTTP, contract tests, etc.). A fork that adds an outbound HTTP call adds the test infrastructure for it at that time. Shipping infrastructure for needs we don't have is the speculative scaffolding the project rules forbid.

### 3.6 Maven archetype: deferred

A scaffolding tool that takes a new entity name and generates Entity + Repository + Service + Controller + DTOs + tests is real value, and a parallel project. We do not build it in v1. A clean `v0.1.0` of the reference is the right gate before starting the archetype.

### 3.7 Layer-sliced packages, ArchUnit-enforced

Top-level packages are layers, not features. Layer slicing only works if cross-layer leaks are mechanically prevented. ArchUnit enforces every rule listed in section 6.13. CI fails on violation.

### 3.8 Modern Java features — what we use and what we don't

The Java 25 feature set is generous; that is not a license to use everything. Each feature below is opt-in or opt-out for explicit reasons.

- **Records** — used for DTOs and domain models. Required. Lombok `@Value` is not used (records are the idiomatic replacement).
- **Sealed types** — used wherever a closed hierarchy is design intent. The `DomainException` family is sealed, permitting exactly `NotFoundException`, `ConflictException`, `ValidationException`, `ForbiddenException`. Required where it applies.
- **Pattern matching for `instanceof`** — required wherever a cast follows an `instanceof` check. No `if (x instanceof Foo) { Foo f = (Foo) x; ... }`.
- **Pattern matching for `switch`** — **NOT** used to replace `@ExceptionHandler` dispatch. Spring's mechanism is the idiomatic dispatch and a switch-over-type would fight it. Use it where it would otherwise be an `if`/`else` chain over types.
- **Text blocks** — required for any multi-line string literal (SQL, JSON, expected test bodies). No `String s = "line1\n" + "line2\n" + ...`.
- **Virtual threads** — enabled at the platform level via `spring.threads.virtual.enabled=true` (see section 8). Nothing application-level to add.
- **Unnamed variables (`_`)** — use in catch blocks where the exception is unused (`catch (NumberFormatException _)`) and in lambda parameters that are unused (`(a, _) -> a`). Improves readability by signalling "this binding is intentionally ignored."
- **String templates** — **NOT** used. JEP 459 was withdrawn and the feature is not in JDK 25. Do not reach for it.
- **Stream gatherers** — not used in this template. Reach for them only if a fork has a genuine sequential-stream-with-state need.

### 3.9 `var` policy

Use `var` when the type is obvious from the right-hand side:

- Constructor calls: `var users = new ArrayList<User>();`
- Factory methods whose name encodes the type: `var users = List.of(u1, u2);`
- Builder chains and fluent APIs where the result type appears at the end: `var http = HttpRequest.newBuilder().uri(...).GET().build();`
- Method-local, short-lived variables (< ~10 lines from declaration to last use).

Do NOT use `var`:

- For primitives — write `int`, `long`, `double`, `boolean` explicitly.
- When the right-hand side is a method call whose name doesn't reveal the type (`var data = process(input)` is forbidden — read `data` of what?).
- When the inferred generic type would be misleading or wider than intended. Example: `realm.getOrDefault("roles", List.of())` infers `Object` because `getOrDefault` is `<V> V getOrDefault(K, V)` on a `Map<String, Object>`. Spell the type.
- On fields — Java doesn't allow `var` on fields, and the principle (declared state deserves an explicit type) applies wherever it might.

### 3.10 Lombok-vs-modern-Java boundary

Lombok pre-dates many features in modern Java. Where the language now covers a Lombok responsibility, the language wins.

| Lombok feature | Status | Replacement |
|---|---|---|
| `@Value` | **NOT** used | `record` |
| `val` | **NOT** used | `var` |
| `@Data` | **NOT** used | — (too broad; generates `equals`/`hashCode`/`toString` in places they shouldn't exist, especially entities) |
| `@SneakyThrows` | **NOT** used | Exceptions are part of the contract; don't hide them. |
| `@Accessors(chain/fluent)` | **NOT** used | Incompatible with Jackson, MapStruct, and JPA conventions. |
| `@Cleanup` | **NOT** used | `try`-with-resources |

Lombok keeps these responsibilities — the language does not have an equivalent:

- `@Slf4j` — SLF4J logger declaration.
- `@RequiredArgsConstructor` — constructor injection on services/controllers.
- `@Getter` / `@Setter` — on JPA entities (records can't be entities).
- `@SuperBuilder` — on the persistence chain.
- `@NoArgsConstructor(access = PROTECTED)` — on `@MappedSuperclass` and `@Entity` classes (JPA requirement).

### 3.11 Design vs argument

DESIGN.md records decisions that have been reasoned through. Each entry captures what was chosen, what was rejected, and the rationale behind both. It is the source of truth for settled architectural and engineering choices in this project.

It is not a shield against new arguments. It does not preempt review. It does not authorize dismissing a concern by citation alone. "DESIGN.md says so" is never a sufficient response to a substantive objection — from a human, from an automated tool, from a future contributor, or from Claude Code itself. A decision is only as good as the reasoning that produced it, and that reasoning is always open to a better one.

When a reviewer raises a concern that conflicts with the current design, engage with the argument on its merits. The question is whether the new argument is stronger than the reasoning DESIGN.md already records. If it is, update DESIGN.md and the code together, in the same commit — the design changes because the reasoning changed. If the existing design is stronger, record the rejection here, with the counter-argument and why it didn't carry; the next reviewer should find the analysis already done and not have to re-litigate from scratch. If it is genuinely unclear which is stronger, that means the design hasn't been thought through enough yet — pause, think it through, decide, document.

Disagreement is the work, not friction. A reviewer raising a real concern has already done part of the design work: they have identified a weakness worth examining. Engaging is required; capitulating is not. The goal is the right answer, not consensus.

Automated reviewers generate noise alongside signal. The right response to noise is to acknowledge and reject with reasoning, not to mute. If the same bot raises the same noise repeatedly, that is itself a signal worth investigating — either the bot is right and we are underweighting it, or the rule the bot encodes belongs in our own enforcement (lint config, ArchUnit rule, CI check) where we control it and the noise stops at the source.

---

## 4. Architectural overview

### 4.1 Persistence — four-class additive chain

`Identifiable` → `Auditable` → `Versioned` → `SoftDeletable`. Each is a `@MappedSuperclass`, each adds exactly one concern. A future entity that doesn't need soft delete extends `Versioned`. A future entity that doesn't need optimistic locking extends `Auditable`. Linear, additive, no flags. `ExampleEntity extends SoftDeletable`.

### 4.2 Three-DTO model

- **`ExampleEntity`** — JPA entity. Lives in `domain.entity`. Never serialized.
- **`Example`** — domain model (Java `record`). Lives in `domain.model`. What the service operates on.
- **API DTOs** — `ExampleResponse`, `CreateExampleRequest`, `UpdateExampleRequest`, `PatchExampleRequest`, `ExampleFilter`. Live in `web.dto`. Validation annotations, Swagger schema annotations.

MapStruct handles all three boundaries.

### 4.3 Search via Spring Data JPA Specifications + EntityGraph

`ExampleSpecifications` composes `Specification<ExampleEntity>` predicates from `ExampleFilter`. The metamodel (`ExampleEntity_`) is generated by the Hibernate JPA metamodel annotation processor — all field references are compile-time-checked. No string field names.

N+1 prevention:

- All associations default to `LAZY`. No exceptions.
- The search repository method uses `@EntityGraph` with the `tags` collection so the tag fetch joins to the page-of-IDs query.
- Hibernate statistics enabled in the integration test profile.
- A dedicated integration test asserts paging 20 entities with tags executes a bounded number of queries. A regression fails the build.

### 4.4 Error handling

- Domain exception hierarchy in `exception`. Every exception carries `(messageKey, args...)`, never a message string.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps each type to an HTTP status and produces an RFC 9457 `ProblemDetail` (`application/problem+json`).
- Message resolution via `MessageSource` against the request's `Accept-Language` header.
- Validation errors aggregate into a single 400 with a `violations` array, each localized.
- Adding a new language is dropping a `messages_<locale>.properties` file. Zero code change.

### 4.5 Security — pure-Java role policy + meta-annotations

- `Role` enum: `ADMIN`, `USER`. The data.
- `RoleTier` enum: `USER_TIER (USER, ADMIN)`, `ADMIN_TIER (ADMIN)`. The policy.
- `RoleHierarchyResolver` — `@Component("roles")`, exposes tier role names to SpEL.
- Meta-annotations `@RequiresUser`, `@RequiresAdmin` — what controllers actually use.
- ArchUnit rule: `@PreAuthorize` may appear only inside `..security.annotation..`. Raw `@PreAuthorize` anywhere else is a build failure.

OAuth2 Resource Server pattern. Service does not manage users. JWT validated against Keycloak issuer URI from config. `JwtAuthConverter` extracts `realm_access.roles` into `ROLE_*` authorities. Local dev: docker-compose boots Keycloak + Postgres with a seeded realm; README documents the seeded users prominently as non-production.

### 4.6 Soft delete + optimistic locking

`SoftDeletable` is annotated with Hibernate 7's `@SoftDelete(strategy = TIMESTAMP, columnName = "deleted_at")`. The annotation lives on the mapped superclass once and applies to every subclass: Hibernate auto-adds the equivalent of `WHERE deleted_at IS NULL` to all queries, and translates `repository.delete*()` into `UPDATE <table> SET deleted_at = current_timestamp WHERE id = ? AND version = ?`. Subclasses do not redeclare `@SQLRestriction` or `@SQLDelete`. `DELETE` is therefore safe by default — there is no way to call a repository delete method and have it bypass the soft-delete contract. No restore endpoint. `Versioned` provides `@Version` for optimistic locking; the generated soft-delete UPDATE includes the version check.

### 4.7 Observability

- Actuator: `health`, `info`, `metrics`, `prometheus`, `loggers`. `health` is the only endpoint exposed without auth.
- Micrometer with Prometheus registry.
- `spring-boot-starter-opentelemetry` (new in Boot 4) → OTLP export of traces, metrics, logs.
- Logback with `logstash-logback-encoder` for JSON logs. MDC propagates `traceId`/`spanId`.

### 4.8 Test infrastructure — container extensions and contract tests

#### 4.8.1 Container extensions

Each container is its own JUnit 5 extension behind a meta-annotation. Each annotation is repeatable with a `name()` parameter for the multi-container case. The 90% test wraps `@SpringBootTest` + the default containers in a single `@IntegrationTest` composition annotation. Test fixtures (e.g. seeded data) follow the same extension pattern.

#### 4.8.2 Test helpers

Three small, single-purpose utilities sit beside the container extensions. They are extracted because the same handful of patterns appear in every integration test; they are deliberately narrow, with no shared base class and no DSL on top.

- **`RestAssuredAuth`** (`test/support/auth/`) — `asAdmin()`, `asUser()`, `asUnauthenticated()`, `withToken(String)`. Each returns a configured `RequestSpecification` so the call site is one line: `asUser().get("/api/v1/examples")`. The JWT factory is wired by `KeycloakExtension` via a package-internal static setter and cleared in `AfterAllCallback` — static helpers are not Spring-aware, and the extension owns the lifecycle.
- **`ProblemDetailAssert`** + **`Assertions`** (`test/support/assertions/`) — an AssertJ custom assertion over a RestAssured `Response` for RFC 9457 ProblemDetail bodies. Fluent: `hasStatus`, `hasProblemJsonContentType`, `hasCode`, `hasTitle`/`hasTitleMatching`, `hasDetail`, `hasType`, `hasViolation(field, code)`, `hasViolationCount`. `Assertions` re-exports AssertJ's standard `assertThat` overloads (by extending `org.assertj.core.api.Assertions`) and adds the `Response` overload so a single static import covers both. The assertion does **not** look up `MessageSource` itself — tests resolve localized strings from the autowired bean and pass them in. Slightly more verbose at the call site, but no spooky-action-at-a-distance from a global.
- **`DatabaseStateHelper`** (`test/support/db/`) — `@TestComponent`, constructor-injected `EntityManager`. `truncateAll()` / `truncate(String)` (native SQL with `CASCADE` and identity reset, excluding `flyway_schema_history`); `countIncludingDeleted(Class<?>)` / `countWhereDeleted(Class<?>)` (native counts that bypass Hibernate's `@SoftDelete` filter so soft-delete behavior is testable). All methods are `@Transactional(propagation = REQUIRES_NEW)` so they commit immediately. Because it is `@TestComponent` it never appears in the production application context.

Design principle: extract a helper only when duplication has paid for the abstraction. We deliberately do **NOT** ship:

- a generic CRUD round-trip helper (`createThenFetch`, `updateThenFetch`, …) — each endpoint has its own validation, headers, and edge cases; a one-size helper hides them and makes negative tests harder, not easier;
- a base integration test class — the meta-annotation `@IntegrationTest` already composes what's needed; a base class would invite creep and couple tests we want independent;
- a JSON assertion DSL on top of RestAssured — RestAssured + the ProblemDetail assertion are enough; a third layer would be a homegrown framework with its own learning curve.

Future helpers earn their place the same way: extracted only after the duplication is real and the call site is demonstrably better with them than without.

#### 4.8.3 Contract test bases

Three abstract test bases (`support/contract/`) capture the cross-entity invariants that every entity inheriting the persistence chain must satisfy. A contract base may only assert invariants true of every entity in the system — persistence semantics, mapping round-trip, soft-delete behavior. Business-specific assertions stay in the hand-written per-entity test. This is the line that stops the layer becoming a framework: if you want to assert something true of `Example` but not of an arbitrary entity, it does not belong in the base.

- **`AbstractRepositoryContractIT<E extends SoftDeletable>`** — asserts `save_assignsIdAndAuditTimestamps`, `findById_returnsPersistedEntity`, `update_incrementsVersion`, `delete_softDeletesAndExcludesFromQueries`, `delete_keepsRowInTable`, `count_excludesSoftDeleted`. Additionally exercises three `SoftDeletable` association test entities (`ParentEntity`/`ChildEntity`, `OwnerEntity`/`ProfileEntity`, `LeftEntity`/`RightEntity`) under `support/testentities/` to verify that soft-deleting a `@OneToMany` parent does not hard-delete or orphan children (since `@SoftDelete` issues an UPDATE, any `ON DELETE CASCADE` never fires), that owning `@ManyToOne`/`@OneToOne` foreign keys survive reload, and that `@ManyToMany` join-table associations survive reload.
- **`AbstractMapperContractIT<E extends SoftDeletable, M>`** — asserts `toModel_copiesAuditFieldsFromEntity`, `toEntity_leavesManagedFieldsUnset`, `toModelList_mapsEveryElement`, `toEntityList_mapsEveryElement`, `applyPatch_updatesMappedFieldsOnly`, `roundTrip_preservesMappedFields`. Round-trip comparisons use the declared mapped-field set only (never whole-object equality) via `assertMappedFields(E entity, M model)` — the single per-entity hook each subclass implements.
- **`AbstractControllerContractIT`** — forward-referenced; will follow the same pattern for controller-layer cross-entity invariants.

**One fixture per entity.** `RepoFixture<E>` (provides `entityType()` and `newPersistable()`) and `ModelFixture<M>` (provides `modelType()` and `newModel()`) are tiny interfaces in `support/fixtures/`. An entity with a model implements both on a single fixture class (e.g. `ExampleFixtures implements RepoFixture<ExampleEntity>, ModelFixture<Example>`), so one fixture feeds everything: the contract bases, `@WithSeededExamples`, and any hand-written tests.

**Generator scope.** `RandomEntities` and `RandomModels` are thin Instancio wrappers — a configured `Instancio.of(type).withSettings(SETTINGS).ignore(<chain fields>).create()`. The generator fills only what is needed to persist and round-trip: the concrete entity's own plain fields, its `@ElementCollection`s, and its embeddables (one level deep). It must NOT populate persistence-chain fields (`id`, `createdAt`, `updatedAt`, `version`); those are framework-managed, and asserting the framework sets them is the contract's job. The ignore-set is expressed structurally — `fields().declaredIn(Identifiable.class)`, etc. — so it applies to every entity without per-entity enumeration. Past ~20 lines of logic the wrappers would be drifting back into the hand-rolled generator; they stay thin.

**Association test entities.** Six test-only entities in `support/testentities/`, all extending `SoftDeletable`: `ParentEntity`/`ChildEntity` (`@OneToMany`/`@ManyToOne`), `OwnerEntity`/`ProfileEntity` (`@OneToOne`, owning side on `OwnerEntity`), `LeftEntity`/`RightEntity` (`@ManyToMany`). Their schemas live in test-only Flyway migrations under `src/test/resources/db/migration-test/`, applied to the test schema only. They never appear in `src/main` — fork-tax stays zero. They exist so the repository contract can exercise association behavior under soft-delete: the don't-orphan-children invariant (soft-deleting a parent issues an UPDATE, so child FK survives) is a cross-entity persistence invariant that belongs in the contract base.

#### 4.8.4 Decision notes

- **`PageResponse` over Spring's `PagedModel`** — Spring's `Page` JSON representation is unstable across major versions, and `PagedModel` drags in the HATEOAS module. `PageResponse` is a plain record with stable, predictable serialization.
- **`DatabaseStateHelper`'s soft-delete-peek is genuinely custom** — no library provides a count that sees past Hibernate's `@SoftDelete` filter. The two native-SQL methods (`countIncludingDeleted`, `countWhereDeleted`) are the only way to verify that `repository.delete*()` issued an UPDATE rather than a DELETE. This is the one piece of test infrastructure that cannot be replaced by a third-party dependency.
- **Custom container extensions vs `@ServiceConnection`** — the custom container extensions (`@WithPostgres`, `@WithKeycloak`) are justified only by the multi-named-container need (each annotation is repeatable with a `name()` parameter, enabling multiple independent containers in a single test class). For the single-container case, Spring Boot's `@ServiceConnection` would suffice. This is a known trade-off, not a redo: if the multi-container need ever disappears, the extensions can be replaced by `@ServiceConnection` with no production-code changes.

### 4.9 Build & packaging

- Spring Boot Maven Plugin `build-image` goal → Paketo buildpacks → no Dockerfile maintained.
- Image tagged `ghcr.io/mykhailokulakov/generic-spring-service:<version>`. PRs build the image but only `main` and tagged releases push it.

### 4.10 API documentation via meta-annotations

Sibling pattern to 4.5 — same composition idea, different cross-cutting concern. Lives in `web/annotation/`.

- `ApiResponseDescriptions` — public static final String constants for the standard error descriptions ("Unauthenticated", "Forbidden — insufficient role", etc.). A wording change is one edit, not nine.
- `@StandardApiResponses` — stacks 401 + 403, both `application/problem+json` with `ProblemDetail` schema. List-style reads.
- `@ReadApiResponses` — stacks 401 + 403 + 404. Single-resource reads.
- `@MutatingApiResponses` — stacks 400 + 401 + 403 + 409. POST endpoints.
- `@VersionedWriteApiResponses` — stacks 400 + 401 + 403 + 404 + 409 + 412. PUT and PATCH endpoints that consume `If-Match`.
- `@DeleteApiResponses` — stacks 401 + 403 + 404. DELETE endpoints.

Controller methods compose: `@RequiresXxx` (auth, from `..security.annotation..`) + `@XxxApiResponses` (documentation, from `..web.annotation..`) + a method-level `@Operation(summary, description)` + one success `@ApiResponse` (`200`/`201`/`204`). Nothing else.

New error responses must be added to the relevant meta-annotation, never inline on a controller. A forthcoming ArchUnit rule (section 6.13) will enforce that any annotation meta-annotated with `@ApiResponse` resides in `..web.annotation..`.

---

## 5. File tree

```
generic_spring_service/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml
│   │   └── release.yml
│   ├── dependabot.yml
│   └── CODEOWNERS
├── .mvn/
│   └── wrapper/
├── docker/
│   ├── docker-compose.yml
│   └── keycloak/
│       └── realm-export.json
├── src/
│   ├── main/
│   │   ├── java/io/github/mykhailokulakov/genericspringservice/
│   │   │   ├── GenericSpringServiceApplication.java
│   │   │   ├── common/
│   │   │   │   └── persistence/
│   │   │   │       ├── Identifiable.java        # @MappedSuperclass: UUID id
│   │   │   │       ├── Auditable.java           # extends Identifiable: createdAt, updatedAt
│   │   │   │       ├── Versioned.java           # extends Auditable: @Version
│   │   │   │       └── SoftDeletable.java       # extends Versioned: deletedAt, markDeleted()
│   │   │   ├── config/
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   ├── JpaConfig.java               # @EnableJpaAuditing
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── ObservabilityConfig.java
│   │   │   │   └── WebConfig.java               # LocaleResolver
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── ExampleEntity.java
│   │   │   │   │   └── ExampleStatus.java
│   │   │   │   └── model/
│   │   │   │       └── Example.java             # record
│   │   │   ├── exception/
│   │   │   │   ├── DomainException.java
│   │   │   │   ├── NotFoundException.java
│   │   │   │   ├── ConflictException.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   ├── ForbiddenException.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── mapper/
│   │   │   │   ├── ExampleEntityMapper.java     # Entity ↔ Model
│   │   │   │   └── ExampleApiMapper.java        # Model ↔ DTOs
│   │   │   ├── repository/
│   │   │   │   ├── ExampleRepository.java
│   │   │   │   └── specification/
│   │   │   │       └── ExampleSpecifications.java
│   │   │   ├── security/
│   │   │   │   ├── Role.java                    # enum: ADMIN, USER
│   │   │   │   ├── RoleTier.java                # enum: USER_TIER, ADMIN_TIER
│   │   │   │   ├── RoleHierarchyResolver.java   # @Component("roles")
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthConverter.java
│   │   │   │   └── annotation/
│   │   │   │       ├── RequiresUser.java
│   │   │   │       └── RequiresAdmin.java
│   │   │   ├── service/
│   │   │   │   └── ExampleService.java          # concrete @Service, no interface
│   │   │   └── web/
│   │   │       ├── ExampleController.java
│   │   │       ├── annotation/
│   │   │       │   ├── ApiResponseDescriptions.java
│   │   │       │   ├── StandardApiResponses.java
│   │   │       │   ├── ReadApiResponses.java
│   │   │       │   ├── MutatingApiResponses.java
│   │   │       │   ├── VersionedWriteApiResponses.java
│   │   │       │   ├── DeleteApiResponses.java
│   │   │       │   └── package-info.java
│   │   │       └── dto/
│   │   │           ├── ExampleResponse.java
│   │   │           ├── CreateExampleRequest.java
│   │   │           ├── UpdateExampleRequest.java
│   │   │           ├── PatchExampleRequest.java
│   │   │           ├── ExampleFilter.java
│   │   │           └── PageResponse.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-test.yml
│   │       ├── db/migration/
│   │       │   ├── V1__base.sql
│   │       │   └── V2__example.sql
│   │       ├── i18n/
│   │       │   ├── messages.properties
│   │       │   ├── messages_en.properties
│   │       │   └── messages_es.properties
│   │       ├── logback-spring.xml
│   │       └── banner.txt
│   └── test/
│       ├── java/io/github/mykhailokulakov/genericspringservice/
│       │   ├── architecture/
│       │   │   └── ArchitectureTest.java
│       │   ├── support/
│       │   │   ├── IntegrationTest.java
│       │   │   ├── auth/
│       │   │   │   └── RestAssuredAuth.java
│       │   │   ├── assertions/
│       │   │   │   ├── ProblemDetailAssert.java
│       │   │   │   └── Assertions.java
│       │   │   ├── db/
│       │   │   │   └── DatabaseStateHelper.java
│       │   │   ├── containers/
│       │   │   │   ├── postgres/
│       │   │   │   │   ├── WithPostgres.java
│       │   │   │   │   └── PostgresExtension.java
│       │   │   │   └── keycloak/
│       │   │   │       ├── WithKeycloak.java
│       │   │   │       ├── KeycloakExtension.java
│       │   │   │       └── TestJwtFactory.java
│       │   │   ├── fixtures/
│       │   │   │   ├── WithSeededExamples.java
│       │   │   │   ├── SeededExamplesExtension.java
│       │   │   │   ├── ExampleFixtures.java
│       │   │   │   ├── RepoFixture.java
│       │   │   │   ├── ModelFixture.java
│       │   │   │   ├── RandomEntities.java
│       │   │   │   └── RandomModels.java
│       │   │   ├── contract/
│       │   │   │   ├── AbstractRepositoryContractIT.java
│       │   │   │   └── AbstractMapperContractIT.java
│       │   │   └── testentities/
│       │   │       ├── ParentEntity.java
│       │   │       ├── ChildEntity.java
│       │   │       ├── OwnerEntity.java
│       │   │       ├── ProfileEntity.java
│       │   │       ├── LeftEntity.java
│       │   │       └── RightEntity.java
│       │   ├── service/
│       │   │   └── ExampleServiceTest.java
│       │   ├── repository/
│       │   │   └── ExampleRepositoryIT.java
│       │   ├── mapper/
│       │   │   ├── ExampleEntityMapperTest.java
│       │   │   └── ExampleApiMapperTest.java
│       │   ├── web/
│       │   │   ├── ExampleControllerIT.java
│       │   │   └── GlobalExceptionHandlerIT.java
│       │   ├── nplusone/
│       │   │   └── ExampleSearchNPlusOneIT.java
│       │   └── i18n/
│       │       └── MessageResolutionIT.java
│       └── resources/
│           ├── application-test.yml
│           ├── db/migration-test/
│           │   └── V1000__test_association_entities.sql
│           └── keycloak/
│               └── test-realm.json
├── .gitignore
├── .gitattributes
├── .editorconfig
├── mvnw
├── mvnw.cmd
├── pom.xml
├── cliff.toml
├── DESIGN.md
└── README.md
```

---

## 6. Key class skeletons

### 6.1 Persistence chain — four classes

```java
@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Identifiable {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Auditable extends Identifiable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Versioned extends Auditable {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}

@MappedSuperclass
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SoftDeletable extends Versioned {}
```

### 6.2 `ExampleEntity`

```java
@Entity
@Table(name = "example",
       indexes = {
           @Index(name = "ix_example_name", columnList = "name"),
           @Index(name = "ix_example_status", columnList = "status"),
           @Index(name = "ix_example_occurred_at", columnList = "occurred_at")
       })
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
public class ExampleEntity extends SoftDeletable {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ExampleStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "example_tag",
        joinColumns = @JoinColumn(name = "example_id"),
        indexes = @Index(name = "ix_example_tag_value", columnList = "tag")
    )
    @Column(name = "tag", nullable = false, length = 64)
    @Builder.Default
    private Set<String> tags = new HashSet<>();
}
```

### 6.3 `Example` (domain record)

```java
public record Example(
    UUID id,
    String name,
    String description,
    Integer quantity,
    BigDecimal price,
    Instant occurredAt,
    ExampleStatus status,
    Set<String> tags,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}
```

### 6.4 `ExampleRepository`

```java
public interface ExampleRepository
    extends JpaRepository<ExampleEntity, UUID>,
            JpaSpecificationExecutor<ExampleEntity> {

    @EntityGraph(attributePaths = "tags")
    @Override
    Page<ExampleEntity> findAll(Specification<ExampleEntity> spec, Pageable pageable);

    @EntityGraph(attributePaths = "tags")
    @Override
    Optional<ExampleEntity> findById(UUID id);
}
```

### 6.5 `ExampleSpecifications`

```java
public final class ExampleSpecifications {

    private ExampleSpecifications() {}

    public static Specification<ExampleEntity> matches(ExampleFilter f) {
        return Specification.allOf(
            nameContains(f.name()),
            descriptionContains(f.description()),
            quantityBetween(f.minQuantity(), f.maxQuantity()),
            priceBetween(f.minPrice(), f.maxPrice()),
            occurredBetween(f.occurredFrom(), f.occurredTo()),
            statusIn(f.statuses()),
            hasAnyTag(f.tags())
        );
    }

    private static Specification<ExampleEntity> nameContains(String value) {
        if (!StringUtils.hasText(value)) return null;
        return (root, q, cb) -> cb.like(cb.lower(root.get(ExampleEntity_.name)),
                                        "%" + value.toLowerCase() + "%");
    }

    private static Specification<ExampleEntity> hasAnyTag(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return (root, q, cb) -> {
            q.distinct(true);
            return root.join(ExampleEntity_.tags).in(tags);
        };
    }

    // ... one private static helper per filter field, each returning null when absent.
}
```

### 6.6 `ExampleService` — concrete, no interface

```java
@Service
@Transactional
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository repository;
    private final ExampleEntityMapper mapper;

    public Example create(Example toCreate) {
        return mapper.toModel(repository.save(mapper.toEntity(toCreate)));
    }

    @Transactional(readOnly = true)
    public Example getById(UUID id) {
        return repository.findById(id)
            .map(mapper::toModel)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
    }

    @Transactional(readOnly = true)
    public Page<Example> search(ExampleFilter filter, Pageable pageable) {
        return repository.findAll(ExampleSpecifications.matches(filter), pageable)
                         .map(mapper::toModel);
    }

    public Example replace(UUID id, Long expectedVersion, Example replacement) {
        ExampleEntity entity = loadAndCheckVersion(id, expectedVersion);
        mapper.applyReplacement(replacement, entity);
        return mapper.toModel(repository.save(entity));
    }

    public Example patch(UUID id, Long expectedVersion, PatchExampleRequest patch) {
        ExampleEntity entity = loadAndCheckVersion(id, expectedVersion);
        mapper.applyPatch(patch, entity);
        return mapper.toModel(repository.save(entity));
    }

    public void softDelete(UUID id) {
        ExampleEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
        repository.delete(entity); // @SoftDelete translates this to UPDATE deleted_at = now()
    }

    private ExampleEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
        ExampleEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
        if (!Objects.equals(entity.getVersion(), expectedVersion)) {
            throw new ConflictException(ErrorCode.OPTIMISTIC_LOCK, id);
        }
        return entity;
    }
}
```

### 6.7 Role infrastructure — pure Java

```java
public enum Role {
    ADMIN, USER;
    public String authority() { return "ROLE_" + name(); }
}

public enum RoleTier {
    USER_TIER (EnumSet.of(Role.USER, Role.ADMIN)),
    ADMIN_TIER(EnumSet.of(Role.ADMIN));

    private final Set<Role> roles;
    RoleTier(Set<Role> roles) { this.roles = Collections.unmodifiableSet(roles); }
    public Set<Role> roles() { return roles; }
    public String[] names() {
        return roles.stream().map(Enum::name).toArray(String[]::new);
    }
}

@Component("roles")
public class RoleHierarchyResolver {
    public String[] userTier()  { return RoleTier.USER_TIER.names(); }
    public String[] adminTier() { return RoleTier.ADMIN_TIER.names(); }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole(@roles.userTier())")
public @interface RequiresUser {}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole(@roles.adminTier())")
public @interface RequiresAdmin {}
```

### 6.8 `ExampleController`

```java
@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Examples")
public class ExampleController {

    private final ExampleService service;
    private final ExampleApiMapper apiMapper;

    @GetMapping
    @RequiresUser
    @Operation(summary = "Search examples")
    @ApiResponse(responseCode = "200", description = "Page of matching examples.")
    @StandardApiResponses
    public PageResponse<ExampleResponse> search(
        @Valid @ParameterObject ExampleFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return PageResponse.of(service.search(filter, pageable).map(apiMapper::toResponse));
    }

    @GetMapping("/{id}")
    @RequiresUser
    @Operation(summary = "Get an example by id")
    @ApiResponse(responseCode = "200", description = "The example.")
    @ReadApiResponses
    public ExampleResponse get(@PathVariable UUID id) {
        return apiMapper.toResponse(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresAdmin
    @Operation(summary = "Create an example")
    @ApiResponse(responseCode = "201", description = "The newly-created example.")
    @MutatingApiResponses
    public ExampleResponse create(@Valid @RequestBody CreateExampleRequest req) {
        return apiMapper.toResponse(service.create(apiMapper.toModel(req)));
    }

    @PutMapping("/{id}")
    @RequiresAdmin
    @Operation(summary = "Replace an example")
    @ApiResponse(responseCode = "200", description = "The updated example.")
    @VersionedWriteApiResponses
    public ExampleResponse replace(
        @PathVariable UUID id,
        @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
        @Valid @RequestBody UpdateExampleRequest req
    ) {
        return apiMapper.toResponse(
            service.replace(id, expectedVersion, apiMapper.toModel(req)));
    }

    @PatchMapping("/{id}")
    @RequiresAdmin
    @Operation(summary = "Patch an example")
    @ApiResponse(responseCode = "200", description = "The updated example.")
    @VersionedWriteApiResponses
    public ExampleResponse patch(
        @PathVariable UUID id,
        @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
        @Valid @RequestBody PatchExampleRequest req
    ) {
        return apiMapper.toResponse(service.patch(id, expectedVersion, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresAdmin
    @Operation(summary = "Soft-delete an example")
    @ApiResponse(responseCode = "204", description = "Deleted.")
    @DeleteApiResponses
    public void delete(@PathVariable UUID id) {
        service.softDelete(id);
    }
}
```

`If-Match` is bound as `required = false` so the service's `IF_MATCH_REQUIRED` branch can run — Spring's default `required = true` would return a framework 400 before the method body executes, making the domain conflict path unreachable.

### 6.9 `GlobalExceptionHandler`

```java
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messages;

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, Locale locale) {
        return problem(HttpStatus.NOT_FOUND, ex, locale, "not-found");
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, Locale locale) {
        return problem(HttpStatus.CONFLICT, ex, locale, "conflict");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, Locale locale) {
        return problem(HttpStatus.FORBIDDEN, ex, locale, "forbidden");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, Locale locale) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("https://generic-spring-service/problems/validation"));
        pd.setTitle(messages.getMessage("error.validation.title", null, locale));
        pd.setProperty("code", ErrorCode.VALIDATION_FAILED.key());
        pd.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of(
                "field", fe.getField(),
                "code", fe.getCode(),
                "message", messages.getMessage(fe, locale)
            )).toList());
        return pd;
    }

    // Example of pattern-matching-for-instanceof where a cast would otherwise follow.
    // Spring's @ExceptionHandler is the dispatch mechanism (section 3.8 — we do NOT
    // hand-roll a switch over the DomainException hierarchy); this helper is invoked
    // by handlers that already narrowed via @ExceptionHandler.
    private ProblemDetail problem(HttpStatus status, DomainException ex, Locale locale, String slug) {
        var pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create("https://generic-spring-service/problems/" + slug));
        pd.setTitle(messages.getMessage("error." + slug + ".title", null, locale));
        pd.setDetail(messages.getMessage(ex.getMessageKey(), ex.getArgs(), locale));
        pd.setProperty("code", ex.getMessageKey());
        return pd;
    }
}
```

The exception hierarchy is sealed (section 3.8). `DomainException` permits exactly the four subclasses above; each subclass is `final`. New domain error types are added by extending the `permits` clause, not by sneaking a sibling in.

### 6.10 `SecurityConfig` + `JwtAuthConverter`

```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter)))
            .build();
    }
}

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // NOT `var realm = jwt.getClaim(...)` — the inferred type would be Object
        // (the raw claim value), wider than the Map<String,Object> we intend.
        // Per section 3.9, spell the type when var would be misleading.
        Map<String, Object> realm = jwt.getClaim("realm_access");
        Collection<String> roles = realm == null
            ? List.of()
            : (Collection<String>) realm.getOrDefault("roles", List.of());
        var authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .toList();
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
```

### 6.11 `@WithPostgres` extension

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithPostgres.List.class)
@ExtendWith(PostgresExtension.class)
public @interface WithPostgres {

    String name()  default "default";
    String image() default "postgres:17-alpine";

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(PostgresExtension.class)
    @interface List { WithPostgres[] value(); }
}

public class PostgresExtension implements BeforeAllCallback {

    private static final Map<String, PostgreSQLContainer<?>> CONTAINERS = new ConcurrentHashMap<>();

    @Override
    public void beforeAll(ExtensionContext ctx) {
        Class<?> testClass = ctx.getRequiredTestClass();
        WithPostgres[] declarations = collectAnnotations(testClass);
        if (declarations.length == 0) declarations = new WithPostgres[] { defaults() };

        for (WithPostgres decl : declarations) {
            PostgreSQLContainer<?> c = CONTAINERS.computeIfAbsent(decl.name(), n ->
                new PostgreSQLContainer<>(DockerImageName.parse(decl.image()))
                    .withLabel("tc.name", n)
                    .withReuse(true));
            if (!c.isRunning()) c.start();
            exportProperties(decl.name(), c);
        }
    }

    private void exportProperties(String name, PostgreSQLContainer<?> c) {
        if ("default".equals(name)) {
            System.setProperty("spring.datasource.url",      c.getJdbcUrl());
            System.setProperty("spring.datasource.username", c.getUsername());
            System.setProperty("spring.datasource.password", c.getPassword());
        } else {
            String prefix = "testcontainers.postgres." + name + ".";
            System.setProperty(prefix + "url",      c.getJdbcUrl());
            System.setProperty(prefix + "username", c.getUsername());
            System.setProperty(prefix + "password", c.getPassword());
        }
    }
    // collectAnnotations(): reads single + repeated declarations.
    // defaults(): a programmatic default WithPostgres instance.
}
```

`@WithKeycloak` follows the identical pattern, with `name`, `image`, and `realmImport` parameters. `realmImport` defaults to `keycloak/test-realm.json` on the test classpath.

### 6.12 `@IntegrationTest` composition + `@WithSeededExamples`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@WithPostgres
@WithKeycloak
@ExtendWith(RestAssuredExtension.class)
public @interface IntegrationTest {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SeededExamplesExtension.class)
public @interface WithSeededExamples {
    int count() default 10;
    String[] tags() default {};
    boolean truncate() default true;
}
```

`SeededExamplesExtension` implements `BeforeEachCallback`, pulls `ExampleRepository` from the Spring context (`SpringExtension.getApplicationContext(ctx)`), optionally truncates the table, and inserts `count` example entities using `ExampleFixtures` builders. The fixtures package exposes a `ExampleFixtures.builder().withRandomDefaults()` so seeding produces realistic-but-varied data.

### 6.13 `ArchitectureTest`

```java
@AnalyzeClasses(packages = "io.github.mykhailokulakov.genericspringservice",
                importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule layered = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Web").definedBy("..web..")
        .layer("Service").definedBy("..service..")
        .layer("Repository").definedBy("..repository..")
        .layer("Entity").definedBy("..domain.entity..")
        .layer("Persistence").definedBy("..common.persistence..")
        .whereLayer("Web").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web")
        .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
        .whereLayer("Entity").mayOnlyBeAccessedByLayers("Repository", "Service", "..mapper..")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Entity");

    @ArchTest
    static final ArchRule noFieldInjection = noFields()
        .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");

    @ArchTest
    static final ArchRule noStdout = noClasses()
        .should().callMethod(System.class, "out")
        .orShould().callMethod(System.class, "err");

    @ArchTest
    static final ArchRule noCycles = slices()
        .matching("..genericspringservice.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule preAuthorizeOnlyInAnnotationPackage = classes()
        .that().areAnnotatedWith("org.springframework.security.access.prepost.PreAuthorize")
        .should().resideInAPackage("..security.annotation..");

    @ArchTest
    static final ArchRule mappedSuperclassOnlyInPersistencePackage = classes()
        .that().areAnnotatedWith("jakarta.persistence.MappedSuperclass")
        .should().resideInAPackage("..common.persistence..");

    @ArchTest
    static final ArchRule entitiesNotInWeb = noClasses()
        .that().resideInAPackage("..web..")
        .should().dependOnClassesThat().resideInAPackage("..domain.entity..");

    @ArchTest
    static final ArchRule controllersInWeb = classes()
        .that().areAnnotatedWith(RestController.class)
        .should().resideInAPackage("..web..");

    @ArchTest
    static final ArchRule servicesInService = classes()
        .that().areAnnotatedWith(Service.class)
        .should().resideInAPackage("..service..");

    @ArchTest
    static final ArchRule entitiesInEntityPackage = classes()
        .that().areAnnotatedWith(Entity.class)
        .should().resideInAPackage("..domain.entity..");

    @ArchTest
    static final ArchRule dtosAreRecords = classes()
        .that().resideInAPackage("..web.dto..")
        .should().beRecords();

    // Lands in prompt 18. Mirrors `preAuthorizeOnlyInAnnotationPackage` for the
    // OpenAPI documentation cross-cut (section 4.10).
    @ArchTest
    static final ArchRule apiResponseOnlyInAnnotationPackage = classes()
        .that().areAnnotatedWith("io.swagger.v3.oas.annotations.responses.ApiResponses")
        .or().areAnnotatedWith("io.swagger.v3.oas.annotations.responses.ApiResponse")
        .should().resideInAPackage("..web.annotation..");
}
```

### 6.14 OpenAPI meta-annotation example

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(
        responseCode = "401",
        description = ApiResponseDescriptions.UNAUTHENTICATED,
        content = @Content(
            mediaType = "application/problem+json",
            schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "403",
        description = ApiResponseDescriptions.FORBIDDEN,
        content = @Content(
            mediaType = "application/problem+json",
            schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface StandardApiResponses {}
```

A controller method using both cross-cuts (auth + documentation) reads as:

```java
@PutMapping("/{id}")
@RequiresAdmin
@Operation(summary = "Replace an example")
@ApiResponse(responseCode = "200", description = "The updated example.")
@VersionedWriteApiResponses
public ExampleResponse replace(
    @PathVariable UUID id,
    @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
    @Valid @RequestBody UpdateExampleRequest req) { ... }
```

Two cross-cuts (`@RequiresAdmin`, `@VersionedWriteApiResponses`), one operation summary, one success response. The six error responses (400/401/403/404/409/412) come from the meta-annotation. Adding a new error response is one edit to `VersionedWriteApiResponses` — every PUT/PATCH endpoint picks it up.

---

## 7. API surface

```
GET    /api/v1/examples              USER_TIER    Pageable + ExampleFilter
GET    /api/v1/examples/{id}         USER_TIER
POST   /api/v1/examples              ADMIN_TIER   CreateExampleRequest
PUT    /api/v1/examples/{id}         ADMIN_TIER   If-Match: <version> + UpdateExampleRequest
PATCH  /api/v1/examples/{id}         ADMIN_TIER   If-Match: <version> + PatchExampleRequest
DELETE /api/v1/examples/{id}         ADMIN_TIER   204, soft delete

GET    /actuator/health              public
GET    /actuator/prometheus          authenticated
GET    /v3/api-docs                  public
GET    /swagger-ui.html              public
```

Pagination: `?page=0&size=20&sort=createdAt,desc`. Filter: any combination of `name`, `description`, `minPrice`, `maxPrice`, `minQuantity`, `maxQuantity`, `occurredFrom`, `occurredTo`, `statuses`, `tags`.

PATCH semantics: `null` field = leave unchanged. There is no way to clear a field via PATCH (see section 3.4).

---

## 8. `application.yml`

```yaml
spring:
  application:
    name: generic-spring-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/generic}
    username: ${DB_USERNAME:generic}
    password: ${DB_PASSWORD:generic}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 5000
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc.time_zone: UTC
        format_sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: false
    deserialization:
      fail-on-unknown-properties: true
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/generic}
  messages:
    basename: i18n/messages
    encoding: UTF-8
    fallback-to-system-locale: false
  web:
    locale-resolver: accept-header

server:
  port: 8080
  shutdown: graceful
  error:
    include-message: never
    include-binding-errors: never
    include-stacktrace: never

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when-authorized
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: ${OTEL_EXPORTER_OTLP_TRACES_ENDPOINT:http://localhost:4318/v1/traces}
  otlp:
    metrics:
      export:
        url: ${OTEL_EXPORTER_OTLP_METRICS_ENDPOINT:http://localhost:4318/v1/metrics}

springdoc:
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    root: INFO
    io.github.mykhailokulakov: INFO
    org.hibernate.SQL: WARN
```

---

## 9. `pom.xml` — structure & dependency sections

Dependencies grouped by vendor, separated by comment lines. Section order is fixed.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/>
    </parent>

    <groupId>io.github.mykhailokulakov</groupId>
    <artifactId>generic-spring-service</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <name>generic-spring-service</name>
    <description>Reference Spring Boot microservice</description>

    <properties>
        <java.version>25</java.version>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
        <springdoc.version>3.0.3</springdoc.version>
        <testcontainers.version>1.20.6</testcontainers.version>
        <testcontainers-keycloak.version>3.7.0</testcontainers-keycloak.version>
        <restassured.version>5.5.7</restassured.version>
        <archunit.version>1.3.2</archunit.version>
        <logstash-logback-encoder.version>8.1</logstash-logback-encoder.version>
        <spotless-maven-plugin.version>2.44.5</spotless-maven-plugin.version>

        <jacoco.minimum.line.coverage>0.80</jacoco.minimum.line.coverage>
        <jacoco.minimum.branch.coverage>0.80</jacoco.minimum.branch.coverage>
    </properties>

    <dependencies>
        <!-- ============================================================ -->
        <!-- Spring Boot — web, data, security, validation                 -->
        <!-- ============================================================ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <!-- ============================================================ -->
        <!-- Spring Boot — observability                                  -->
        <!-- ============================================================ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-opentelemetry</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>${logstash-logback-encoder.version}</version>
        </dependency>

        <!-- ============================================================ -->
        <!-- Persistence — PostgreSQL, Flyway, JPA metamodel               -->
        <!-- ============================================================ -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-jpamodelgen</artifactId>
            <version>${hibernate.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- ============================================================ -->
        <!-- API documentation — springdoc OpenAPI                         -->
        <!-- ============================================================ -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- ============================================================ -->
        <!-- Mapping — MapStruct + Lombok                                  -->
        <!-- ============================================================ -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- ============================================================ -->
        <!-- Tests — JUnit, AssertJ, Spring Test, Testcontainers,         -->
        <!--         RestAssured, ArchUnit                                 -->
        <!-- ============================================================ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.github.dasniko</groupId>
            <artifactId>testcontainers-keycloak</artifactId>
            <version>${testcontainers-keycloak.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>${restassured.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit-junit5</artifactId>
            <version>${archunit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot — packaging + image build via buildpacks -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <image>
                        <name>ghcr.io/mykhailokulakov/generic-spring-service:${project.version}</name>
                    </image>
                </configuration>
            </plugin>

            <!-- Compiler — annotation processors for Lombok, MapStruct, JPA metamodel -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>${lombok-mapstruct-binding.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.hibernate.orm</groupId>
                            <artifactId>hibernate-jpamodelgen</artifactId>
                            <version>${hibernate.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <!-- Surefire — unit tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <excludes><exclude>**/*IT.java</exclude></excludes>
                </configuration>
            </plugin>

            <!-- Failsafe — integration tests (*IT.java) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <executions>
                    <execution>
                        <goals><goal>integration-test</goal><goal>verify</goal></goals>
                    </execution>
                </executions>
            </plugin>

            <!-- JaCoCo — coverage + 80% gate -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <executions>
                    <execution><id>prepare-agent</id><goals><goal>prepare-agent</goal></goals></execution>
                    <execution><id>prepare-agent-integration</id><goals><goal>prepare-agent-integration</goal></goals></execution>
                    <execution>
                        <id>report</id><phase>verify</phase>
                        <goals><goal>report</goal><goal>report-integration</goal></goals>
                    </execution>
                    <execution>
                        <id>check</id><phase>verify</phase>
                        <goals><goal>check</goal></goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit><counter>LINE</counter><value>COVEREDRATIO</value><minimum>${jacoco.minimum.line.coverage}</minimum></limit>
                                        <limit><counter>BRANCH</counter><value>COVEREDRATIO</value><minimum>${jacoco.minimum.branch.coverage}</minimum></limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- Spotless — format check with google-java-format -->
            <plugin>
                <groupId>com.diffplug.spotless</groupId>
                <artifactId>spotless-maven-plugin</artifactId>
                <version>${spotless-maven-plugin.version}</version>
                <configuration>
                    <java>
                        <googleJavaFormat>
                            <version>1.35.0</version>
                        </googleJavaFormat>
                        <removeUnusedImports/>
                        <importOrder/>
                        <trimTrailingWhitespace/>
                        <endWithNewline/>
                    </java>
                </configuration>
                <executions>
                    <execution><phase>verify</phase><goals><goal>check</goal></goals></execution>
                </executions>
            </plugin>

            <!-- Enforcer — Java version, no SNAPSHOTs in release, no duplicate deps -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-enforcer-plugin</artifactId>
                <executions>
                    <execution>
                        <id>enforce</id><goals><goal>enforce</goal></goals>
                        <configuration>
                            <rules>
                                <requireJavaVersion><version>[25,)</version></requireJavaVersion>
                                <dependencyConvergence/>
                                <banDuplicatePomDependencyVersions/>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 10. GitHub Actions — `ci.yml`

Jobs (parallel where possible, `fail-fast: true`):

| Job | Purpose | Reports |
|---|---|---|
| `format` | `mvn spotless:check` | — |
| `architecture` | ArchUnit tests only (fast) | — |
| `unit-test` | `mvn test` | JUnit XML, JaCoCo unit |
| `integration-test` | `mvn verify -DskipUnitTests` | JUnit XML, JaCoCo IT |
| `coverage` | Merge JaCoCo reports, gate at 80%, comment on PR | HTML artifact |
| `build` | `mvn package` + `spring-boot:build-image` | Image artifact (no push on PR) |
| `security` | CodeQL Java | SARIF → Security tab |
| `image-scan` | Trivy on built image | SARIF → Security tab |
| `dependency-audit` | OWASP dependency-check + license check | HTML artifact, SARIF |

Concurrency: `group: ${{ github.workflow }}-${{ github.ref }}, cancel-in-progress: true`. All actions pinned to commit SHAs. Runner: `ubuntu-24.04`.

---

## 11. GitHub Actions — `release.yml`

```
on:
  workflow_dispatch:
    inputs:
      bump:
        type: choice
        required: true
        options: [patch, minor, major]
```

Steps:

1. Verify trigger ref is `main`.
2. Run the full CI suite (reusable workflow_call).
3. Compute next version from latest tag.
4. `mvn versions:set`, commit `chore(release): vX.Y.Z [skip ci]`.
5. `git-cliff` between previous tag and HEAD → release body.
6. Tag `vX.Y.Z`, push.
7. Create GitHub Release with changelog body, attach jar.
8. Build & push image to ghcr.io.
9. Bump to next SNAPSHOT, commit `chore(release): back to snapshot [skip ci]`.

Failure before step 6 → no half-released state. Failure after tag → fix is a new patch release.

---

## 12. README outline

```
# generic-spring-service

Reference Spring Boot microservice. Fork as the starting point for new services.

## What's inside
Brief feature list — REST + OpenAPI, JPA + Postgres + Flyway, Keycloak OAuth2,
i18n error handling, observability, Testcontainers with composable extensions,
80% coverage gate, layered ArchUnit rules, release workflow.

## Quickstart
- Prereqs: Java 25, Docker, Maven Wrapper (bundled).
- `docker compose -f docker/docker-compose.yml up -d`
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
- Open http://localhost:8080/swagger-ui.html

## Default credentials (LOCAL ONLY — non-production)
| Realm | User | Password | Roles |
|-------|------|----------|-------|
| generic | admin | admin | ADMIN, USER |
| generic | user  | user  | USER |

These are baked into the local Keycloak realm import. They are NOT
production credentials. They MUST NOT be reused in any real deployment.

## PATCH semantics
`null` field = leave unchanged. There is no way to clear a field via PATCH.
If a fork needs clearing semantics, see DESIGN.md section 3.4.

## Build & test
- `./mvnw verify` — full build, tests, coverage gate, format check.
- `./mvnw spring-boot:build-image` — produces the container image.

## Architecture
Pointer to DESIGN.md.

## Forking this as a new service
Checklist — rename Maven coordinates, rename Java package, update README,
update docker image name, update Keycloak realm name, replace ExampleEntity
with your real entity.

## Releasing
GitHub → Actions → Release → Run workflow → choose bump.

## Tech choices, briefly
Bullet list linking to DESIGN.md sections.
```

---

## 13. Definition of Done (template-specific)

A change to this template is done when:

- Feature works, covered by unit + integration tests where applicable.
- Coverage ≥ 80% line and branch (gated in CI).
- All CI jobs green.
- ArchUnit rules pass.
- No new code smells in touched files; existing smells in touched files fixed in a separate commit.
- README updated if user-visible behavior changes.
- Conventional Commit format on every commit.
