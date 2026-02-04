---
description: "Task map for TTS Gateway Service Core"
---

# Tasks: TTS Gateway Service Core

**Input**: Design documents from `/specs/001-tts-service-core/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: **MANDATORY**. All business logic must be tested first.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/imaudiopaas/tts/`
- **Backend Tests**: `backend/src/test/java/com/imaudiopaas/tts/`
- **Frontend**: `frontend/src/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Initialize Spring Boot project (Web, JPA, Validation, Security) in `backend/`
- [x] T002 Initialize React project (Vite, MUI, Axios) in `frontend/`
- [x] T003 [P] Configure Checkstyle/Spotless for Java in `backend/pom.xml`
- [x] T004 [P] Configure ESLint/Prettier for TypeScript in `frontend/package.json`
- [x] T005 Setup Testcontainers configuration in `backend/src/test/java/com/imaudiopaas/tts/AbstractIntegrationTest.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Setup PostgreSQL/H2 database configuration in `backend/src/main/resources/application.yml`
- [x] T007 Implement AES Encryption Converter for JPA in `backend/src/main/java/com/imaudiopaas/tts/core/security/AesEncryptConverter.java`
- [x] T008 Create `ProviderConfig` entity in `backend/src/main/java/com/imaudiopaas/tts/model/ProviderConfig.java`
- [x] T009 Create `VoiceDefinition` entity in `backend/src/main/java/com/imaudiopaas/tts/model/VoiceDefinition.java`
- [x] T010 [P] Define `TtsProvider` interface in `backend/src/main/java/com/imaudiopaas/tts/core/TtsProvider.java`
- [x] T011 [P] Define `TtsRequest` and `TtsResponse` domain models in `backend/src/main/java/com/imaudiopaas/tts/core/domain/`
- [x] T012 Configure Global Exception Handling in `backend/src/main/java/com/imaudiopaas/tts/api/GlobalExceptionHandler.java`

**Checkpoint**: Database ready, Security ready, Domain interfaces defined.

---

## Phase 3: User Story 1 - Unified Cloud TTS API (Priority: P1) 🎯 MVP

**Goal**: Standardized API endpoint delegating to Aliyun, Tencent, AWS.

**Independent Test**: Call `/v1/audio/speech` with provider-specific parameters and get audio.

### Tests for User Story 1 (MANDATORY - Test First) ⚠️

- [x] T013 [P] [US1] Create integration test for Cloud Routing in `backend/src/test/java/com/imaudiopaas/tts/api/SpeechApiIntegrationTest.java`
- [x] T014 [P] [US1] Create unit test for Aliyun Provider in `backend/src/test/java/com/imaudiopaas/tts/infrastructure/aliyun/AliyunTtsProviderTest.java`
- [x] T015 [P] [US1] Create unit test for AWS Provider in `backend/src/test/java/com/imaudiopaas/tts/infrastructure/aws/AwsTtsProviderTest.java`

### Implementation for User Story 1

- [x] T016 [P] [US1] Implement `AliyunTtsProvider` using SDK in `backend/src/main/java/com/imaudiopaas/tts/infrastructure/aliyun/AliyunTtsProvider.java`
- [x] T017 [P] [US1] Implement `AwsTtsProvider` (Polly) in `backend/src/main/java/com/imaudiopaas/tts/infrastructure/aws/AwsTtsProvider.java`
- [x] T018 [P] [US1] Implement `TencentTtsProvider` in `backend/src/main/java/com/imaudiopaas/tts/infrastructure/tencent/TencentTtsProvider.java`
- [x] T019 [US1] Implement `ProviderRoutingService` to select provider based on request model in `backend/src/main/java/com/imaudiopaas/tts/service/ProviderRoutingService.java`
- [x] T020 [US1] Implement REST Controller `POST /v1/audio/speech` in `backend/src/main/java/com/imaudiopaas/tts/api/OpenAiSpeechController.java`
- [x] T020.1 [US1] Implement Debug API to list valid provider names (e.g. `GET /v1/debug/providers`) in `backend/src/main/java/com/imaudiopaas/tts/api/DebugController.java`
- [x] T021 [US1] Implement Bearer Token extraction filter in `backend/src/main/java/com/imaudiopaas/tts/config/security/ApiTokenFilter.java`
- [x] T021.1 [US1] Implement "Smart Default" resolution logic in `ProviderRoutingService.java` (Added per FR-010)

**Checkpoint**: Core TTS API working for cloud providers.

---

## Phase 4: User Story 2 - Admin Management Console (Priority: P1)

**Goal**: Dynamic management of provider credentials via Web UI.

**Independent Test**: Add a new provider via UI, immediately use it in API.

### Tests for User Story 2 (MANDATORY - Test First) ⚠️

- [x] T022 [P] [US2] Integration test for Provider CRUD API in `backend/src/test/java/com/imaudiopaas/tts/api/AdminProviderControllerTest.java`

### Implementation for User Story 2

- [x] T023 [P] [US2] Implement `ProviderConfigService` (CRUD logic) in `backend/src/main/java/com/imaudiopaas/tts/service/ProviderConfigService.java`
- [x] T024 [P] [US2] Implement Admin REST API in `backend/src/main/java/com/imaudiopaas/tts/api/AdminProviderController.java`
- [x] T025 [P] [US2] Configure Basic Auth for `/admin/**` endpoints in `backend/src/main/java/com/imaudiopaas/tts/config/SecurityConfig.java`
- [x] T026 [This task has strictly frontend path] [US2] Create API client in `frontend/src/api/adminClient.ts`
- [x] T027 [This task has strictly frontend path] [US2] Create Provider List/Edit Form in `frontend/src/pages/AdminPage.tsx`
- [x] T028 [This task has strictly frontend path] [US2] Implement Login Page in `frontend/src/pages/LoginPage.tsx`

**Checkpoint**: Administrators can manage providers securely.

---

## Phase 5: User Story 3 - Self-Hosted Model Integration (Priority: P2)

**Goal**: Support for VibeVoice and Qwen3 via HTTP connectors.

**Independent Test**: Mock external HTTP service (VibeVoice), verify backend proxies request correctly.

### Tests for User Story 3 (MANDATORY - Test First) ⚠️

- [x] T029 [P] [US3] Create connection test with MockServer in `backend/src/test/java/com/imaudiopaas/tts/infrastructure/selfhosted/VibeVoiceProviderTest.java`

### Implementation for User Story 3

- [x] T030 [P] [US3] Implement `VibeVoiceProvider` (HTTP Client) in `backend/src/main/java/com/imaudiopaas/tts/infrastructure/selfhosted/VibeVoiceProvider.java`
- [x] T031 [P] [US3] Implement `QwenTtsProvider` (HTTP Client) in `backend/src/main/java/com/imaudiopaas/tts/infrastructure/selfhosted/QwenTtsProvider.java`
- [x] T032 [US3] Register self-hosted providers in `ProviderRoutingService.java`

**Checkpoint**: Service can route to external self-hosted models.

---

## Phase 6: User Story 4 - Advanced Speech Controls (Priority: P2)

**Goal**: Support emotion and tone parameters.

**Independent Test**: Verify provider-specific parameter mapping (e.g. Aliyun JSON payload includes 'emotion').

### Tests for User Story 4 (MANDATORY - Test First) ⚠️

- [x] T033 [P] [US4] Unit test for Parameter Mapping in `backend/src/test/java/com/imaudiopaas/tts/core/ParameterMapperTest.java`

### Implementation for User Story 4

- [x] T034 [P] [US4] Update `TtsRequest` to include `extraBody` parsing in `backend/src/main/java/com/imaudiopaas/tts/core/domain/TtsRequest.java`
- [x] T035 [P] [US4] Map emotion/tone params in `AliyunTtsProvider.java`
- [x] T036 [P] [US4] Map emotion/tone params in `AwsTtsProvider.java` (SSML mapping if applicable)

**Checkpoint**: Rich speech generation enabled.

---

## Phase 7: User Story 5 - Interactive Demo App (Priority: P3)

**Goal**: Visual demo tool for testing all providers.

**Independent Test**: Play audio from browser.

### Implementation for User Story 5

- [x] T037 [This task has strictly frontend path] [US5] Create TTS Demo API client in `frontend/src/api/ttsClient.ts`
- [x] T038 [This task has strictly frontend path] [US5] Create Audio Player component in `frontend/src/components/AudioPlayer.tsx`
- [x] T039 [This task has strictly frontend path] [US5] Implement Demo Page with Provider/Voice selectors in `frontend/src/pages/DemoPage.tsx`
- [x] T044 [This task has strictly frontend path] [US5] Implement dynamic wave graph visualization in `frontend/src/components/WaveformPlayer.tsx` (replaces basic player)
- [x] T045 [This task has strictly frontend path] [US5] Implement HTTP log window component in `frontend/src/components/LogWindow.tsx` and integrate into `DemoPage.tsx`

**Checkpoint**: Full end-to-end system demoable.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [x] T040 [P] Create `Dockerfile` for Backend and Frontend
- [x] T041 [P] Add Swagger/OpenAPI UI (SpringDoc) in `backend/pom.xml`
- [x] T042 Verify all K8s probes (Health/Liveness) in `backend/src/main/resources/application.yml`
- [x] T043 Final Integration Test Suite execution

---

## Dependencies & Execution Order

1. **Setup (Ph 1) & Foundational (Ph 2)**: Must be completed first to establish the skeleton.
2. **User Story 1**: Depends on DB and shared interfaces.
3. **User Story 2**: Depends on DB Entities. Can run in parallel with US1.
4. **User Story 3**: Depends on US1 (Routing Service).
5. **User Story 4**: Depends on US1 (Provider implementations).

### Parallel Example: User Story 1
```bash
# Developer A: Aliyun Implementation
Task: T016 Implement AliyunTtsProvider...

# Developer B: AWS Implementation
Task: T017 Implement AwsTtsProvider...

# Developer C: Controller & Routing
Task: T020 Implement REST Controller...
```
