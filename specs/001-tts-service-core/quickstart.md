# Quickstart: TTS Gateway

**Feature**: `001-tts-service-core`

## Prerequisites
- Java 21+
- Maven 3.9+
- Docker (for database and testing)

## Setup

1. **Clone & Build**:
   ```bash
   git checkout 001-tts-service-core
   mvn clean install
   ```

2. **Start Dependencies (Local)**:
   ```bash
   docker run -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres:15
   # Or rely on H2 for development
   ```

3. **Run Application**:
   ```bash
   mvn spring-boot:run
   ```

## Configuration
Access the Admin Console at `http://localhost:8080/admin`.
Default credentials: `admin` / `password` (Change in `application.yml`).

## Testing the API
Generate speech using curl:

```bash
curl http://localhost:8080/v1/audio/speech \
  -H "Authorization: Bearer test-token" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "aliyun/cosyvoice-v1",
    "input": "Testing the audio gateway.",
    "voice": "longxiaochun"
  }' \
  --output test.mp3
```

## Running Tests
Run full suite (including Testcontainers):
```bash
mvn test
```
