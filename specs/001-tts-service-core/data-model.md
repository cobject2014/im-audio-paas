# Data Model: TTS Gateway

**Feature**: `001-tts-service-core`
**Input**: Requirements from `spec.md`

## Entities

### ProviderConfig
Represents a configured external TTS provider.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | UUID | Yes | Primary Key |
| `name` | String | Yes | Display name (e.g. "Aliyun Prod") |
| `providerType` | Enum | Yes | `ALIYUN`, `TENCENT`, `AWS`, `VIBEVOICE`, `QWEN` |
| `baseUrl` | String | No | URL for self-hosted or region-specific endpoint |
| `accessKey` | String | No | Encrypted access key / ID |
| `secretKey` | String | No | Encrypted secret key |
| `isActive` | Boolean | Yes | Whether this provider is enabled |
| `metadata` | JSON | No | Additional config (appKey, regions, etc.) |
| `createdAt` | DateTime | Yes | Audit |
| `updatedAt` | DateTime | Yes | Audit |

**Validation Rules**:
- `name` must be unique.
- At least one active provider for each `providerType` is recommended (but not enforced by specific constraint).

### VoiceDefinition
Registry of available voices mapped to providers.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | String | Yes | PK (e.g. "aliyun-xiaoyun") |
| `providerType` | Enum | Yes | Link to provider type |
| `nativeVoiceId` | String | Yes | ID used by the downstream provider |
| `displayName` | String | Yes | Human readable name |
| `gender` | Enum | Yes | `MALE`, `FEMALE`, `NEUTRAL` |
| `styles` | String[] | No | List of supported emotions/styles |

## Database Schema (SQL)

```sql
CREATE TABLE provider_configs (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    provider_type VARCHAR(50) NOT NULL,
    base_url VARCHAR(255),
    access_key VARCHAR(255), -- Encrypted
    secret_key VARCHAR(255), -- Encrypted
    is_active BOOLEAN DEFAULT TRUE,
    metadata TEXT, -- JSON
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE voice_definitions (
    id VARCHAR(100) PRIMARY KEY,
    provider_type VARCHAR(50) NOT NULL,
    native_voice_id VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    gender VARCHAR(20),
    styles TEXT -- JSON array or comma-separated
);
```
