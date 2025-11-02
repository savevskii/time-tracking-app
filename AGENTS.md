# Repository Guidelines

## Project Structure & Module Organization
- `time-tracking-application/`: Spring Boot API; controllers/services in `src/main/java/com/fsavevsk/timetracking/**`, configuration in `.../configuration`.
- `time-tracking-persistence/`: JPA entities, repositories, and Flyway migrations under `src/main/resources/db/migration`.
- `time-tracking-integration-tests/`: Testcontainers suites mirroring API packages; suffix classes with `IT`.
- `time-tracking-frontend/`: React + Vite client (`src/pages`, `src/components`, `src/auth`, `src/test`); bundled into the backend build.
- `infra/helm/`: Helm charts and values for the app, Keycloak, and PostgreSQL; update with config changes.

## Build, Test, and Development Commands
- `mvn -B clean verify`: root build running compilation, unit tests, and Failsafe ITs.
- `mvn spring-boot:run -pl time-tracking-application -Dspring-boot.run.profiles=dev`: starts the API locally; provide PostgreSQL and Keycloak settings via env vars or profile overrides.
- `npm install && npm run dev` within `time-tracking-frontend`: launches the Vite dev server (5173); use `npm run lint`, `npm run test`, and `npm run build` for checks and production assets.
- `mvn -pl time-tracking-frontend -am package`: packages the built web assets for Spring Boot distribution.

## Coding Style & Naming Conventions
- Java uses 4-space indentation, Lombok for boilerplate, and constructor injection; place REST controllers in `.../controller`, services in `.../service`, and keep MapStruct mappers in `.../mapper` with immutable DTOs.
- TypeScript follows the repo ESLint rules; keep views in `src/components` or `src/pages`, share hooks from `src/hooks`, and order Tailwind utility classes by layout → spacing → color.

## Testing Guidelines
- Unit tests live in each module's `src/test/java` with the `*Test.java` suffix; rely on JUnit 5 and Mockito, and aim for ~80% coverage on new backend code.
- Integration tests stay in `time-tracking-integration-tests` with `*IT.java` names; run them via `mvn -pl time-tracking-integration-tests verify` and prefer deterministic timestamps when asserting persistence.
- Frontend specs use Vitest + Testing Library; colocate `*.test.tsx` files or use `src/test` utilities and run `npm run test -- --watch` while developing.

## Commit & Pull Request Guidelines
- Write concise, imperative commit subjects (e.g., `Fix security config`) and keep changes focused.
- Before pushing, run `mvn clean verify` plus the frontend lint/test scripts, and include Jenkins run links in the PR when available.
- Pull requests should describe scope, affected modules, linked Jira/Bitbucket issues, and share screenshots or API samples for visible changes; never modify historical Flyway migrations or commit secrets.

## Security & Configuration Tips
- Store secrets outside Git (env vars, secret managers) and mirror new configuration in both Spring profile files and `infra/helm/time-tracking-app` values to keep GitOps deployments consistent.

## Diploma Thesis Context
- Project underpins diploma thesis titled "Automation of the web application testing and delivery process".
- CI will move to GitHub with GitHub Actions after migration from Bitbucket (in progress).
- CD follows GitOps principles via Kubernetes and Argo CD using the `time-tracking-deployment` repository.
- Exploring modern CI/CD pipelines that incorporate AI agents; primary app code lives in `time-tracking-app`.
