# Time Tracking Application – Diploma Thesis Project

## 📖 Overview
This repository contains the implementation of the diploma thesis project:
**“Automation of the testing, build, and delivery process of a web application.”**

The project is a **full-stack Time Tracking application** designed to demonstrate:
- **Modern web architecture** (Spring Boot backend + React frontend)
- **Automated build, test, and delivery (CI/CD)** with GitHub Actions + GitOps
- **Security with Keycloak** (OAuth2 / OIDC, JWT resource server)
- **Infrastructure as Code & GitOps** on AWS EKS (Docker, Helm, ArgoCD)
- **Database migrations** with Flyway and robust **integration testing** with Testcontainers

The research goal is a **reliable, fast, and secure delivery pipeline** for web apps.

---

## 🏗️ Architecture

- **Backend**
    - Spring Boot (Java 21)
    - Multi-module Maven:
        - `time-tracking-application` – REST API (controllers, DTOs, services)
        - `time-tracking-persistence` – Entities, repositories, Flyway migrations
        - `time-tracking-integration-tests` – Integration tests with Testcontainers

- **Frontend**
    - React + Vite + TypeScript + TailwindCSS (maintained in the separate [`time-tracking-frontend`](https://github.com/savevskii/time-tracking-frontend) repository)

- **Security**
    - Keycloak (IAM), Spring Security OAuth2 Resource Server (JWT)

- **Database**
    - PostgreSQL with Flyway versioned migrations

- **Infrastructure**
    - Dockerized services
    - AWS EKS, Helm charts, GitOps deployments via ArgoCD
    - Ingress with AWS ALB (public and mTLS-protected paths as needed)

- **CI/CD**
    - GitHub Actions workflows
    - Unit + integration tests (JUnit5, Mockito, Testcontainers)
    - Static code analysis (e.g., SonarQube/Checkstyle/SpotBugs)
    - Docker image build & push (GitHub Container Registry)
    - Helm deploy via ArgoCD

---

## 🚀 Features

- Authentication & role-based authorization (Keycloak)
- Projects & time entries (CRUD)
- Admin reports (overview and per-project)
- REST API secured with JWT
- Automated pipeline from commit → deploy

---

## ⚙️ Local Development

### Prerequisites
- Java 21
- Maven 3.9+
- Docker (and optionally Docker Compose)
- PostgreSQL (local or via container)

### Backend (Spring Boot)

    cd application
    mvn clean install
    mvn spring-boot:run

### Frontend
See the dedicated [`time-tracking-frontend`](https://github.com/savevskii/time-tracking-frontend) repository for setup, linting, and tests.

### Database (PostgreSQL)
You can run a local PostgreSQL or use Compose:

    docker compose up postgres

Flyway migrations run automatically on app startup (depending on profile).

---

## 🔧 Configuration

### Spring Profiles
- `dev` – local development
- `test` – integration tests (Testcontainers)
- `prod` – production (AWS)

### Example application properties (local)
Adjust to your environment (do not commit secrets):

    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/timetracker
        username: time-tracking-admin
        password: topsecret
      security:
        oauth2:
          resource-server:
            jwt:
              issuer-uri: http://localhost:8080/realms/dev

For Kubernetes: externalize secrets to AWS Secrets Manager or K8s Secrets and mount via environment or files.

---

## 🧪 Testing

- **Unit tests**

  mvn test

- **Integration tests (Failsafe + Testcontainers)**

  mvn verify -DskipUnitTests

Notes:
- Repository/integration tests use PostgreSQL Testcontainers.
- Prefer deterministic timestamps (truncate to micros) to avoid flaky assertions.

---

## 🛠️ CI/CD Pipeline (GitHub Actions → GitOps)

**Stages (typical):**
1. Backend build & unit/integration tests (`mvn -B clean verify`)
2. Security scans (Trivy filesystem, Snyk, OWASP Dependency-Check)
3. Docker build & push (GitHub Container Registry)
4. Release metadata for `time-tracking-deployment` Helm chart updates (PR automation + ArgoCD sync)

Frontend CI/CD runs in the separate repository and publishes its own build artefacts or container.

**Maven tips:**
- Full build without integration tests:

  mvn -B -ntp clean install -DskipITs

- Run only integration-tests module with dependencies:

  mvn -pl time-tracking-integration-tests -am verify

**GitOps flow:**
- App and infra changes are declared in Git.
- ArgoCD continuously reconciles the desired state to the cluster.

---

## ☸️ Kubernetes & Helm

- **Charts** live under `deployment/`
- **Ingress** via AWS ALB:
    - Public endpoints (no mTLS)
    - Private/mTLS endpoints (client cert verification) when required
- **Secrets**:
    - Source from AWS Secrets Manager (preferred) or K8s Secrets
- **Environments**:
    - Separate values files per environment (dev/uat/prod)

Basic rollout:

    helm upgrade --install time-tracking ./deployment \
      -f deployment/values-dev.yaml \
      --namespace timetracking

ArgoCD watches the Git repo containing the charts/values and syncs automatically.

---

## 🔒 Security

- Keycloak for identity and access management
- Resource server validates JWTs (issuer/realm configured per env)
- mTLS for sensitive ingress paths (ALB listener + annotations)
- Principle of least privilege in IAM (IRSA for service accounts)

---

## 🧹 Code Quality

- Checkstyle/SpotBugs (Java)
- ESLint/TypeScript checks live in the frontend repository
- SonarQube (quality gate in CI)
- Conventional testing pyramid: fast unit tests → targeted integration tests

---

## 🧭 Contributing (internal)

- Review [`AGENTS.md`](AGENTS.md) for repository guidelines before starting new work.
- Create feature branches from `main` (or `develop`, if used)
- Open GitHub Pull Requests with:
    - Green unit + integration tests
    - Passing quality gate
    - Clear description and scope
- Keep Flyway migrations incremental and immutable

---

## 📚 Thesis Context

This repository supports the diploma thesis at the **Faculty of Computer Science & Engineering**.
It demonstrates how **automated testing, building, and delivery** improve quality, reliability, and time-to-market for web applications.

---

## 👤 Author

**Filip Savevski**  
Faculty of Computer Science & Engineering – Diploma Thesis 2025

---

## ⚖️ License

Unless stated otherwise, this work is provided for **educational and academic purposes**. All rights reserved.
