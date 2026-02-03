# Research & Technical Decisions: TTS Gateway Service Core

**Feature**: `001-tts-service-core`
**Date**: 2026-02-03

## 1. Cloud Provider Integration
**Decision**: Use official Java SDKs for each cloud provider but wrap them in a consistent `TtsProvider` interface.

### sdk-details
| Provider | SDK Artifact | Notes |
|----------|--------------|-------|
| **Aliyun** | `com.aliyun:aliyun-java-sdk-core`, `com.aliyun:aliyun-java-sdk-nls-cloud-meta` | Intelligent Speech (NLS) API for CosyVoice/Sambert. |
| **Tencent** | `com.tencentcloudapi:tencentcloud-sdk-java-tts` | Standard Tencent Cloud SDK v3. |
| **AWS** | `software.amazon.awssdk:polly` | AWS SDK v2 for Java (Polly). |

**Rationale**: Official SDKs handle signing, retries, and authentication complexity better than raw HTTP calls.

## 2. Self-Hosted Model Integration (VibeVoice / Qwen3)
**Decision**: Treat self-hosted models as external HTTP services.
**Assumption**: The Python models are deployed with an API wrapper (e.g., FastAPI or Gradio API) that accepts JSON and returns Audio.
**Integration**: Use `RestClient` (Spring Boot 3) to forward requests.

### API Contract (Internal Proxy)
Since VibeVoice/Qwen3 are research models, their native APIs vary (Gradio/Python). The Java service will expect a standardized adapter running alongside them or will map the OpenAI-compatible request to their specific format if stable.
*Fallback*: If they run standard Gradio, we can use the Gradio client API or reverse-engineer the `/api/predict` JSON payload.

## 3. Database & Secrets
**Decision**: Store provider configuration in `ProviderConfig` entity.
**Security**:
- `accessKey` and `secretKey` columns must be encrypted at rest (using a `@Convert` JPA converter or Spring Vault if available).
- For this feature validation, we will use a simple AES encryption converter in the application to comply with "No hardcoded secrets".
- **Constitution Check**: Sensitive configuration managed in DB (for dynamic rotation).

## 4. API Specification (OpenAI Compatibility)
**Decision**: Expose `POST /v1/audio/speech`.
**Request Body**:
```json
{
  "model": "aliyun/cosyvoice-v1",
  "input": "Hello world",
  "voice": "alloy", 
  "response_format": "mp3",
  "speed": 1.0,
  "extra_body": { "emotion": "happy" }
}
```
**Rationale**: Allows drop-in replacement for existing OpenAI clients while supporting our multi-provider backend via `model` parsing (prefix-based or mapped).

## 5. Technology Stack Validation
- **Java 21**: Confirmed.
- **Spring Boot 3.2+**: Confirmed (needed for `RestClient` and Virtual Threads).
- **Virtual Threads**: Enable for high-throughput I/O (calling downstream providers).
- **Testcontainers**: Use `LocalStack` for AWS mock, `GenericContainer` for DB, and `MockServer` for external HTTP APIs.

## 6. Frontend
**Decision**: Single React SPA with `react-router`.
- `/demo`: Public demo page.
- `/admin`: Protected admin console.
**UI Library**: Ant Design or MUI (Modern style requirement). Selected: **MUI** (standard, robust).
