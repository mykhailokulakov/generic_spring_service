# generic-spring-service

Reference Spring Boot microservice. Fork as the starting point for new services.

The shape of the code, the test infrastructure, the build, and the release
pipeline are all the answer to a single question: what should a new service
look like on day one so that day-one decisions don't have to be re-litigated
every time. Decisions are recorded in [DESIGN.md](DESIGN.md); this README is
the operator's guide.

> [!WARNING]
> **The credentials, client secrets, and realm export shipped in this
> repository are non-production.** They exist so a developer can `docker
> compose up` and have a working local stack in under a minute. They are
> public in the git history, baked into a JSON file checked into the repo,
> and MUST NOT be used in any deployed environment. See [Default credentials
> (LOCAL ONLY)](#default-credentials-local-only--non-production) below.

## What's inside

- **REST + OpenAPI 3.1** — `springdoc-openapi`, Swagger UI at `/swagger-ui.html`.
- **JPA + PostgreSQL 17 + Flyway** — Hibernate 7, four-class additive
  persistence chain (`Identifiable` → `Auditable` → `Versioned` →
  `SoftDeletable`), `@SoftDelete` on the superclass, `@Version` optimistic
  locking with `If-Match` for `PUT`/`PATCH`.
- **Keycloak OAuth2 Resource Server** — JWT validation against the realm
  issuer URI. `Role`/`RoleTier` enums + `@RequiresUser`/`@RequiresAdmin`
  meta-annotations; ArchUnit forbids raw `@PreAuthorize` outside the security
  package.
- **i18n error handling** — RFC 9457 `application/problem+json`, every
  exception carries a message key, locale resolved from `Accept-Language`,
  new languages drop in as a `messages_<locale>.properties` file.
- **Observability** — Actuator + Micrometer Prometheus registry, OTLP
  traces/metrics/logs via `spring-boot-starter-opentelemetry`, JSON logs
  with MDC `traceId`/`spanId` propagated by Logback.
- **Testcontainers, composable** — Postgres and Keycloak each behind a
  meta-annotation (`@WithPostgres`, `@WithKeycloak`); `@IntegrationTest`
  composes both; `@WithSeededExamples` seeds data per-test.
- **80% line + branch coverage gate** enforced by JaCoCo in `verify`.
- **Layered ArchUnit rules** — package access, no field injection, no
  `System.out`, no cycles, `@PreAuthorize` confined to the annotation
  package, entities never reached from `..web..`, controllers/services/
  entities in their declared packages, DTOs are records, OpenAPI annotations
  confined to the annotation package.
- **Release workflow** — manual `workflow_dispatch`, semver bump, git-cliff
  changelog, tag, GitHub Release, image push to GHCR, snapshot-bump commit.

## Quickstart

Prereqs: **Java 25**, **Docker** (Compose v2), and the bundled Maven Wrapper.

```bash
# 1. Start Postgres + Keycloak (seeded realm imported on first boot)
docker compose -f docker/docker-compose.yml up -d

# 2. Run the service against them
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 3. Open Swagger UI
open http://localhost:8080/swagger-ui.html
```

Calling the API requires a JWT from the local Keycloak. The convenience
script takes a seeded username + password and prints just the access token,
so the standard pattern is one line:

```bash
TOKEN=$(docker/get-token.sh admin admin)

# Same thing as a raw curl, no script:
TOKEN=$(curl -sS -f -X POST \
  http://localhost:9080/realms/generic/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_id=generic-spring-service' \
  --data-urlencode 'client_secret=generic-spring-service-secret' \
  --data-urlencode 'username=admin' \
  --data-urlencode 'password=admin' \
  | jq -r .access_token)

# Create an example
curl -s -X POST http://localhost:8080/api/v1/examples \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"hello","status":"ACTIVE"}'

# List examples
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/examples
```

Keycloak's admin console is at `http://localhost:9080` with `admin`/`admin`
(again — non-production; see below).

### Optional: local tracing

Copy [`docker-compose.override.yml.example`](docker-compose.override.yml.example)
to `docker-compose.override.yml`, uncomment either the Jaeger or the
OpenTelemetry Collector block, and re-run `docker compose up -d` with both
files. The application already reads `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT`;
export it to point at the collector on `:4318` and traces start flowing.

## Default credentials (LOCAL ONLY — non-production)

| Realm     | Username | Password | Realm roles    |
|-----------|----------|----------|----------------|
| `generic` | `admin`  | `admin`  | `ADMIN`, `USER`|
| `generic` | `user`   | `user`   | `USER`         |

The Keycloak admin user is also `admin` / `admin`. The OIDC client secret
(`generic-spring-service-secret`) is baked into
[`docker/keycloak/realm-export.json`](docker/keycloak/realm-export.json) and
into the convenience script.

**None of these are production credentials.** They live in plain text in
this repository for the sole purpose of letting `docker compose up` work
without further configuration. They must not be reused, imported, or
referenced in any environment that handles real data. For a real
deployment: provision a fresh realm, mint a fresh client secret, set a
fresh admin password, and never check any of them into source control.

## PATCH semantics

`null` field = leave unchanged. Explicit value = set. **There is no way to
clear a field via PATCH in this template's API.** The alternative
(RFC 7396 JSON Merge Patch, where `null` clears the field) requires a
custom Jackson deserializer per patch DTO to distinguish "field absent"
from "field present and null." If a fork genuinely needs clearing
semantics, see [DESIGN.md §3.4](DESIGN.md) and add the deserializer there;
otherwise the template's behaviour stands.

## Build & test

```bash
./mvnw verify                         # full build: tests, coverage gate, format check, ArchUnit
./mvnw test                           # unit tests only
./mvnw -Dtest='*IT' failsafe:integration-test failsafe:verify  # IT only
./mvnw spotless:apply                 # auto-format Java to google-java-format
./mvnw spring-boot:build-image        # produce the OCI image via buildpacks
```

The `verify` goal fails if line OR branch coverage drops below 80%
(`jacoco-maven-plugin`'s `check` execution). The same goal also runs
Spotless, the Maven Enforcer (Java version + dependency convergence + no
duplicate dependency versions), and the integration test suite — every IT
spins up real Postgres and real Keycloak via Testcontainers.

## Architecture

The full design — including the rationale for each non-trivial choice and
the deliberate non-choices — is in [DESIGN.md](DESIGN.md). One-line
summary of the layout:

```
common.persistence → entity → repository → service → web
                                                ↑
                                            mapper
```

ArchUnit enforces every arrow at build time; cross-layer access that
violates the rules fails `verify`.

## Forking this as a new service

This template is designed to be forked, then renamed in place. A
mechanical checklist for the rename:

1. **Maven coordinates** — in [`pom.xml`](pom.xml):
   - `<groupId>` from `io.github.mykhailokulakov` to your group.
   - `<artifactId>` from `generic-spring-service` to your artifact.
   - `<name>` and `<description>` accordingly.
2. **Java package** — move
   `src/{main,test}/java/io/github/mykhailokulakov/genericspringservice` to
   your base package. Update the `package` declaration in every file and
   the `packages` argument in
   `src/test/.../architecture/ArchitectureTest.java`.
3. **Application properties** — in
   `src/main/resources/application.yml` and `application-local.yml`:
   - `spring.application.name`
   - `spring.datasource.url`/`username`/`password` defaults (and the
     matching values in [`docker/docker-compose.yml`](docker/docker-compose.yml))
   - `spring.security.oauth2.resourceserver.jwt.issuer-uri` default
     (realm name)
   - Problem-detail `type` URI base in
     `src/main/java/.../exception/GlobalExceptionHandler.java`
4. **Container image name** — in `pom.xml`'s
   `spring-boot-maven-plugin` `<image><name>` and in the GHCR refs of
   `.github/workflows/release.yml` + `.github/workflows/publish-image.yml`.
5. **Keycloak realm** — rename `generic` everywhere it appears:
   - [`docker/keycloak/realm-export.json`](docker/keycloak/realm-export.json) (`realm`, role names if you want, users)
   - [`docker/get-token.sh`](docker/get-token.sh) defaults
   - issuer URI in `application.yml`
   - test realm at `src/test/resources/keycloak/test-realm.json` and the
     `KeycloakExtension` default
6. **Domain entity** — `ExampleEntity` + `Example` + `ExampleService` +
   `ExampleController` + the DTOs in `web/dto` + the Flyway migrations
   (`V2__example.sql`) + every test under `..web.ExampleControllerIT`,
   `..service.ExampleServiceTest`, `..mapper.Example*MapperTest`,
   `..repository.ExampleRepositoryIT`, the fixtures package and the
   `WithSeededExamples` annotation. Keep the test count discipline: ≥ 5
   positive + 5 negative per endpoint.
7. **README** — replace the description, swap the GHCR badge, prune this
   forking checklist (it has done its job once the rename is complete).
8. **CODEOWNERS** and [`.github/CODEOWNERS`](.github/CODEOWNERS) ownership.

Smoke test the rename by running `./mvnw verify` — the enforcer plus
ArchUnit will catch anything that didn't move with the package.

## Releasing

GitHub → **Actions** → **Release** → **Run workflow** → choose `patch`,
`minor`, or `major`. The workflow:

1. Verifies the trigger ref is `main` (a misfired dispatch from a topic
   branch fails in seconds).
2. Runs the full CI suite via `workflow_call`.
3. Computes the next version from the highest existing `vX.Y.Z` tag.
4. Sets the pom version, commits `chore(release): vX.Y.Z [skip ci]`, pushes.
5. Generates a release-notes Markdown file with `git-cliff` using the
   commit-grouping rules in [`cliff.toml`](cliff.toml).
6. Tags `vX.Y.Z` and pushes the tag — **point of no return**.
7. Builds the release jar, creates the GitHub Release with the changelog
   body and the jar attached.
8. Builds and pushes `ghcr.io/.../generic-spring-service:<version>` and
   `:latest` to GHCR.
9. Sets the pom to the next patch `-SNAPSHOT`, commits
   `chore(release): back to snapshot [skip ci]`, pushes.

Steps before the tag push are reversible. After the tag push, recovery is
a new patch release, not editing the tag.

## Tech choices, briefly

- [§3.1](DESIGN.md) — why our own persistence chain, not `AbstractPersistable`.
- [§3.2](DESIGN.md) — `ExampleService` has no interface, on purpose.
- [§3.3](DESIGN.md) — role policy is pure Java; promote to YAML if a fork
  needs per-environment policy.
- [§3.4](DESIGN.md) — PATCH `null` = leave unchanged.
- [§3.5](DESIGN.md) — Testcontainers integration tests *are* our E2E.
- [§3.7](DESIGN.md) — layer-sliced packages, ArchUnit-enforced.
- [§3.8](DESIGN.md) — which modern Java features we use and which we don't.
- [§3.9](DESIGN.md) — `var` policy.
- [§3.10](DESIGN.md) — Lombok-vs-modern-Java boundary.
- [§4.5](DESIGN.md) — security via meta-annotations.
- [§4.10](DESIGN.md) — OpenAPI via meta-annotations.
- [§4.8](DESIGN.md) — test infrastructure: container extensions + three
  small helpers, no base class, no DSL.
