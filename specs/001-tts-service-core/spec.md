# Feature Specification: TTS Gateway Service Core

**Feature Branch**: `001-tts-service-core`
**Created**: 2026-02-03
**Status**: Draft
**Input**: User description: "Implement TTS Gateway service with Cloud (Aliyun, Tencent, AWS) and Self-hosted (VibeVoice, Qwen3) providers, Admin Console, and Demo App"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Unified Cloud TTS API (Priority: P1)

Developers can generate audio from text using a single standardized API endpoint that delegates to cloud providers (Aliyun, Tencent, AWS), avoiding vendor lock-in and simplifying integration.

**Why this priority**: Core functionality. Without the ability to route requests to cloud providers, the service has no baseline value.
**API guideline** All API must follow the guideline in "restful_api_guideline_1.0.1.md".
**API token** All API must have a "Bearer Token" field for auth purpose. But for local test, the token verification cab be ignored.
**Independent Test**: Can be tested by calling the API with `provider=aliyun` (or aws/tencent) and receiving a valid audio file.

**Acceptance Scenarios**:

1. **Given** valid Aliyun credentials configured, **When** API is called with text "Hello World" and provider "aliyun", **Then** a playable audio stream is returned.
2. **Given** valid AWS credentials, **When** API is called with text "Hello" and provider "aws", **Then** audio is returned.
3. **Given** invalid credentials, **When** API is called, **Then** a 401/500 compliant error response is returned.

---

### User Story 2 - Admin Management Console (Priority: P1)

Administrators can manage TTS provider configurations (API keys, secrets, regions) via a secure web interface, allowing credential rotation and provider updates without application restarts.

**Why this priority**: Critical for operations. Storing long-term credentials in code is forbidden by Constitution; dynamic configuration is required for a managed service.

**Independent Test**: Can be tested by logging into the console, adding a new provider config, and verifying the backend API immediately uses the new credential.

**Acceptance Scenarios**:

1. **Given** an unauthenticated user, **When** accessing the console, **Then** they are redirected to a login prompt (Basic Auth).
2. **Given** an admin user, **When** they update the Tencent Secret ID, **Then** subsequent API calls use the new Secret ID immediately.
3. **Given** a new provider configuration, **When** saved, **Then** it persists to the database (JPA).

---

### User Story 3 - Self-Hosted Model Integration (Priority: P2)

Developers can generate speech using self-hosted models (VibeVoice, Qwen3-TTS) through the same API, enabling privacy interactions and use of specialized open-source models.

**Why this priority**: Expands capabilities beyond standard cloud providers, offering cost savings and customization.

**Independent Test**: Can be tested by mocking the downstream self-hosted service endpoints and verifying the gateway correctly transforms requests.

**Acceptance Scenarios**:

1. **Given** a configured VibeVoice endpoint, **When** API is called with provider "vibevoice", **Then** the request is correctly proxied to the VibeVoice instance.
2. **Given** a Qwen3-TTS setup, **When** API is called with prompt text, **Then** audio is generated and returned.

---

### User Story 4 - Advanced Speech Controls (Priority: P2)

Developers can specify emotion, tone, and character parameters in the API request to generate expressive speech.

**Why this priority**: Differentiates the service from simple text-to-speech pipes; required for "rich" audio applications.

**Independent Test**: Verify API accepts JSON payload with `emotion` field and maps it to the specific provider's format.

**Acceptance Scenarios**:

1. **Given** Aliyun CosyVoice provider, **When** requesting with `emotion="happy"`, **Then** the downstream request includes Aliyun-specific emotion parameters.
2. **Given** a provider that doesn't support emotion, **When** requesting emotion, **Then** the parameter is gracefully ignored or a warning is returned (depending on design).

---

### User Story 5 - Interactive Demo App (Priority: P3)

End users (or developers testing the system) can use a web interface to type text, select a provider/voice, and immediately play the result.

**Why this priority**: Visual validation tool. Helps showcase capabilities but relies on the backend being functional first.

**Independent Test**: User can open web page, type "Test", click "Generate", and hear sound.

**Acceptance Scenarios**:

1. **Given** the demo app loaded, **When** user selects "AWS" and types text, **Then** the "Play" button becomes active upon success.
2. **Given** a backend error, **When** generating speech, **Then** the UI displays a readable error message.

### Edge Cases

- **Provider Timeout**: What happens when a cloud provider takes >30 seconds? (System should timeout and return 504).
- **Rate Limiting**: How does system handle downstream 429 errors? (Should propagate or retry with backoff).
- **Unsupported Parameters**: Requesting an emotion from a provider that doesn't support it (Should ignore or warn).
- **Network Failure**: Backend cannot reach provider (Return 502 Bad Gateway).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose a RESTful API compliant with OpenAI's `/v1/audio/speech` definition (POST), extended for multi-provider support.
- **FR-002**: System MUST support the following providers: Aliyun (CosyVoice/Sambert), Tencent Cloud, AWS Polly.
- **FR-003**: System MUST support integration with self-hosted VibeVoice and Qwen3-TTS instances (via HTTP connector).
- **FR-004**: API MUST accept parameters for `model` (provider/voice), `input` (text), `voice` (character), `speed`, and extended parameters `emotion`, `tone`.
- **FR-005**: System MUST persist provider configurations (Endpoint, AccessKey, SecretKey) securely.
- **FR-006**: System MUST support hot-reloading of provider configurations (no service restart required).
- **FR-007**: Admin Console MUST be protected via HTTP Basic Authentication.
- **FR-008**: Admin Console MUST provide CRUD operations for Provider Configurations.
- **FR-009**: Demo App MUST be accessible via web browser and connect to the Backend API.

### Key Entities

- **ProviderConfig**: Represents a single TTS provider's settings (Type, Name, BaseURL, EncryptedCredentials, ActiveStatus).
- **VoiceDefinition**: Represents a specific voice/character available for a provider (ID, Name, Gender, SupportedStyles).

## Success Criteria

1.  **API Compatibility**: Endpoint `/v1/audio/speech` accepts standard OpenAI-format JSON and returns `audio/mpeg` (or configured format).
2.  **Provider Coverage**: Successful audio generation verified for all 5 target sources (Aliyun, Tencent, AWS, VibeVoice, Qwen3).
3.  **Configurability**: Admin can rotate an API key in the console and the next API call succeeds with the new key.
4.  **Test Coverage**: Core business logic (Gateway routing, Config management) has >80% code coverage.
5.  **Performance**: API overhead (routing/transformation logic) is < 20ms (excluding downstream provision time).

## Assumptions

- **Self-Hosted Models**: VibeVoice and Qwen3-TTS services are assumed to be running externally; this project implements the *client/connector* to them, not the hosting of the Python models themselves.
- **OpenAI Compatibility**: Strict JSON payload compatibility is maintained for common fields (`model`, `input`, `voice`), while specific features (emotion) uses metadata or extended fields.
- **Network**: The backend has outbound internet access to call Cloud APIs.
