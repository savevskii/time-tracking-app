# Repository Guidelines

## Project Structure & Module Organization
- `time-tracking-application/`: Spring Boot API; controllers/services in `src/main/java/com/fsavevsk/timetracking/**`, configuration in `.../configuration`.
- `time-tracking-persistence/`: JPA entities, repositories, and Flyway migrations under `src/main/resources/db/migration`.
- `time-tracking-integration-tests/`: Testcontainers suites mirroring API packages; suffix classes with `IT`.
- Frontend now lives in the separate `time-tracking-frontend` repository; integrate via APIs rather than bundling static assets here.
- `infra/helm/`: Helm charts and values for the app, Keycloak, and PostgreSQL; update with config changes.

## Build, Test, and Development Commands
- `mvn -B clean verify`: root build running compilation, unit tests, and Failsafe ITs.
- `mvn spring-boot:run -pl time-tracking-application -Dspring-boot.run.profiles=dev`: starts the API locally; provide PostgreSQL and Keycloak settings via env vars or profile overrides.

## Coding Style & Naming Conventions
- Java uses 4-space indentation, Lombok for boilerplate, and constructor injection; place REST controllers in `.../controller`, services in `.../service`, and keep MapStruct mappers in `.../mapper` with immutable DTOs.

## Testing Guidelines
- Unit tests live in each module's `src/test/java` with the `*Test.java` suffix; rely on JUnit 5 and Mockito, and aim for ~80% coverage on new backend code.
- Integration tests stay in `time-tracking-integration-tests` with `*IT.java` names; run them via `mvn -pl time-tracking-integration-tests verify` and prefer deterministic timestamps when asserting persistence.

## Commit & Pull Request Guidelines
- Write concise, imperative commit subjects (e.g., `Fix security config`) and keep changes focused.
- Before pushing, run `mvn clean verify` and include relevant GitHub Actions run links in the PR when available.
- Pull requests should describe scope, affected modules, linked Jira/Bitbucket issues, and share screenshots or API samples for visible changes; never modify historical Flyway migrations or commit secrets.

## Security & Configuration Tips
- Store secrets outside Git (env vars, secret managers) and mirror new configuration in both Spring profile files and `infra/helm/time-tracking-app` values to keep GitOps deployments consistent.

## Diploma Thesis Context
- Project underpins diploma thesis titled "Automation of the web application testing and delivery process".
- CI now runs on GitHub Actions in this repository after migrating from Bitbucket.
- CD follows GitOps principles via Kubernetes and Argo CD using the `time-tracking-deployment` repository.
- Exploring modern CI/CD pipelines that incorporate AI agents; primary app code lives in `time-tracking-app`.

## CI/CD Pipeline Goals
- `time-tracking-app` (CI) should trigger on PRs and pushes; run `mvn -B clean verify`, aggregate coverage, and block merges on failures.
- Successful main-branch builds should produce versioned backend Docker images, publish build artifacts, and surface test/security reports.
- Integrate container/image scanning (Snyk/Trivy) plus dependency/license checks before release artifacts are published.
- On release tagging, push images to the container registry and emit metadata (digest, version, changelog) for the CD repo to consume.
- `time-tracking-deployment` (CD) should watch for new image metadata, update Helm chart values or Kustomize overlays with the fresh tag, run chart/unit tests, and open automated PRs for review.
- Argo CD syncs the approved manifest changes into Kubernetes; ensure post-deploy smoke tests run, report status back to GitHub, and support automated rollback on failure signals.
- Evaluate inserting AI agents for pipeline assistance (e.g., summarize CI findings, generate deployment PR descriptions, suggest remediation steps) while keeping human approvals in the loop.

## CI/CD Tooling Decisions
- Container images live in GitHub Container Registry (GHCR); follow GitHub best practices for naming (`ghcr.io/<org>/time-tracking-app:<semver>`).
- Adopt SemVer tagging; release workflow tags the repo and propagates the version to images and Helm chart updates.
- Security checks: run Trivy (image/file system), Snyk (SCA), and OWASP Dependency-Check on every build before publishing artifacts.
- Test coverage: use JaCoCo uploads sent to Codecov for PR diff reporting.
- Helm remains the packaging mechanism in `time-tracking-deployment`; workflows will update chart values with new image tags.
- Post-deploy smoke tests deferred for now; revisit when CD automation matures.
- AI agent integration points to be defined later once baseline pipelines are stable.

## GitHub Actions Workflow Design
- `ci.yml` (PRs to `main`, pushes to `main`):
  - Jobs: `backend-build-test` (Maven verify + JaCoCo upload), `security-scans` (Trivy fs scan, Snyk SCA, OWASP Dependency-Check), `docker-build` (buildx build; push to GHCR only on `main`), `quality-gate` (final status aggregation, uploads reports/artifacts). Each job uses caching (`actions/setup-java`) and uploads JUnit/coverage artifacts.
  - Coverage upload via `codecov/codecov-action` in the backend job; PR comments disabled, rely on status checks.
  - Secrets: `GHCR_PAT` (container push), `SNYK_TOKEN`, `CODECOV_TOKEN`, optional `TRIVY_*` if required.
- `release.yml` (trigger on SemVer tag + manual dispatch):
  - Re-run full build/test matrix, reuse artifacts, build and push release images with tag + `latest`.
  - Generate provenance (digest + version JSON) and upload release assets; create GitHub Release notes.
  - Commit or PR metadata files into `time-tracking-deployment` (Helm values bump) using deployment repo token.
- `nightly-security.yml` (scheduled):
  - Nightly cron to run Trivy, Snyk, and OWASP scans against main branch to detect drift; opens issues or posts Slack/Teams notifications on findings.
- `deployment-sync.yml` (workflow_run on successful `release.yml` or manual):
  - Validates updated Helm chart (lint + `helm unittest`/CT), opens PR in `time-tracking-deployment` with new image tag and changelog snippet, labels for reviewers.
- Shared practices:
  - Use concurrency groups (`ci-${{ github.ref }}`) to cancel superseded runs.
  - Store test results/coverage as artifacts + expose via `actions/upload-artifact` for 90 days.
  - Enforce required checks in GitHub (backend, security, Codecov, docker build) before merge.
  - Reuse composite actions or `reusable-workflow.yml` later for multi-repo consistency.
