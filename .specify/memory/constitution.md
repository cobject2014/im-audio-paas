<!-- SYNC IMPACT REPORT
Version: 1.0.0 (Initial)
Modified Principles:
- Defined I. Test-First Engineering
- Defined II. Technology Stack Standards
- Defined III. K8s & Container Readiness
- Defined IV. API Specifications
- Defined V. Security by Design
- Defined VI. Code Quality & Style
- Defined VII. Configuration Management
Templates requiring updates:
- .specify/templates/tasks-template.md (✅ updated: enforced test-first)
-->
# IM Audio PaaS Constitution

## Core Principles

### I. Test-First Engineering
Test-first development is mandatory for all major components. Testing must aim for at least **80% coverage** on core business logic.
Local integration tests must use **Testcontainers** to ensure container-friendly verification and environmental consistency.

### II. Technology Stack Standards
**Backend**: Use **Java 21** as the primary language with **Spring Boot**. Utilize modern features like Virtual Threads, Record types, and Pattern Matching to simplify code and improve concurrency.
**Frontend**: Use **TypeScript** as the primary language with the **React** framework.
**Database**: **SQLite/H2** for local/test environments. Production persistence must use **Spring Data JPA** to ensure vendor independence. Avoid database-specific SQL/dialects unless explicitly approved.

### III. K8s & Container Readiness
All backend architecture design must be **Kubernetes-friendly**.
Requirements:
- **Graceful Shutdown**
- **Liveness/Readiness probes**
- **Externalized log management** (Standard Out)
- Local tests must be container-friendly.

### IV. API Specifications
All RESTful APIs must strictly follow the [`spec/restful_api_guideline_1.0.1.md`](spec/restful_api_guideline_1.0.1.md) specification.
(Note: Ensure this specification file is present in the repository).

### V. Security by Design
**No hardcoded secrets** allowed in the codebase.
**Authentication**: Use OAuth2/JWT.
**Transport**: All sensitive data in transit must use TLS 1.3.
**Secrets**: Sensitive configuration must be managed via K8s Secrets or a Centralized Vault. (Tests may use partial exceptions).

### VI. Code Quality & Style
**Conventions**: Follow **Google’s coding conventions**.
**Comments**: Comments should be concise and placed only where strictly needed (no more, no less).
**UI**: The user interface must be responsive and modern in style.

### VII. Configuration Management
Use **centralized configuration** for production environments.
Local configuration files are permitted for test cases.

## Governance

This Constitution supersedes all other technical practices.
Amendments require documentation, team approval, and a migration plan.
All PRs and code reviews must verify compliance with these principles.

**Version**: 1.0.0 | **Ratified**: 2026-02-03 | **Last Amended**: 2026-02-03
