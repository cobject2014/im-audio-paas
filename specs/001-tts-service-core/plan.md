# Implementation Plan: TTS Gateway Service Core

**Branch**: `001-tts-service-core` | **Date**: 2026-02-03 | **Spec**: [specs/001-tts-service-core/spec.md](specs/001-tts-service-core/spec.md)
**Input**: Feature specification from `/specs/001-tts-service-core/spec.md`

## Summary

The TTS Gateway Service acts as a unified facade for multiple Text-to-Speech providers (Aliyun, Tencent, AWS, Self-Hosted VibeVoice/Qwen3). It exposes an **OpenAI-compatible API** (`POST /v1/audio/speech`) to clients, handling authentication, routing, and provider-specific parameter mapping. Configuration of providers is managed dynamically via a protected Admin Console, requiring no restarts for credential rotation.

## Technical Context

**Language/Version**: Java 21 (Backend), TypeScript (Frontend), Python (Test scripts if needed)
**Primary Dependencies**:
- Backend: Spring Boot 3.2+ (Web, Data JPA, Validation), Aliyun SDK, Tencent Cloud SDK, AWS SDK v2, Spring Security.
- Frontend: React 18, Vite, MUI (Material UI), Axios.
**Storage**: H2 (Test/Local), PostgreSQL/MySQL compatible (Production via JPA).
**Testing**: JUnit 5, Testcontainers (Postgres, MockServer), RestAssured.
**Target Platform**: Kubernetes (Docker containerized).
**Project Type**: Web application (Backend + Frontend).
**Performance Goals**: <20ms Routing Overhead.
**Constraints**: No hardcoded secrets. Strict API compliance.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Test-First**: Plan includes setup of Testcontainers and integration tests before logic.
- [x] **Tech Stack**: Java 21, Spring Boot, React confirmed.
- [x] **K8s Friendly**: Health checks and localized logging included in tasks.
- [x] **API Specs**: Contract defined in `contracts/openai-speech-api.yaml`. **Note**: User must ensure `spec/restful_api_guideline_1.0.1.md` is present (currently missing in root, check `.specify/memory/spec/`).
- [x] **Security**: Secrets managed via DB encryption (soft requirement for MVP) or Vault.

## Project Structure

### Documentation (this feature)

```text
specs/001-tts-service-core/
├── plan.md              # This file
├── research.md          # Technology decisions
├── data-model.md        # Entity definitions
├── quickstart.md        # Setup guide
├── contracts/
│   └── openai-speech-api.yaml
└── tasks.md             # Implementation tasks
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/imaudiopaas/tts/
│   ├── api/             # REST Controllers (OpenAI compliant)
│   ├── config/          # Spring Config (Security, Async)
│   ├── core/            # Domain interfaces (TtsProvider)
│   ├── infrastructure/  # Provider implementations (Aliyun, Aws...)
│   ├── service/         # Business logic (Routing, Config)
│   └── model/           # JPA Entities (ProviderConfig)
├── src/test/            # Unit & Integration Tests (Testcontainers)

frontend/
├── src/
│   ├── components/      # UI Components (ProviderForm, Player)
│   ├── pages/           # AdminPage, DemoPage
│   ├── api/             # API Client
│   └── types/           # TS Interfaces
```

**Structure Decision**: Multi-module or monorepo style with `backend` and `frontend` folders to clearly separate the Java service from the React management console.

## Complexity Tracking

N/A - Standard implementation.
