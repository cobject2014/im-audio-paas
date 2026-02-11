## Context
The TTS Gateway enables switching between multiple providers. Currently, we lack historical data on which providers are being used and their success rates. This change adds a persistence layer for provider request logs and an admin interface to view them.

## Goals / Non-Goals

**Goals:**
- Persist details of every downstream provider request.
- Ensure minimal impact on the critical path latency of TTS requests.
- Provide a clear, aggregated view of statistics to Admins.

**Non-Goals:**
- Real-time monitoring alerts (e.g., paging on failure).
- Complex custom reporting or data export (CSV/PDF) for now (though easy to add later).
- Tracking user-specific quotas (separate concern).

## Decisions

### 1. Data Capture Mechanism: Spring Application Events
We will use Spring's `ApplicationEventPublisher` to publish a `ProviderRequestEvent` after each TTS attempt.
- **Why**: Decouples the logging logic from the core business logic. Allows the logging to be processed asynchronously (`@Async` listener) to avoid adding DB write latency to the TTS response time.
- **Alternative**: AOP Aspect. Rejected because it can be brittle if method signatures change and harder to handle context-specific data explicitly. Event publishing is explicit and type-safe.

### 2. Storage: Relational Database (JPA)
We will store logs in the primary database using a new `ProviderRequestLog` entity.
- **Why**: User requirement. Simplifies infrastructure (no new DB to manage). Data volume is expected to be manageable for the target scale.
- **Schema**:
  - `id` (Long, PK)
  - `provider_name` (String)
  - `request_time` (Timestamp)
  - `is_success` (Boolean)
  - `error_message` (String, nullable)
  - `latency_ms` (Long)

### 3. Frontend: Dedicated Admin Page
A new route `/admin/statistics` in the React app.
- **Why**: Keeps the statistics feature distinct from configuration.
- **UI**: A summary table showing Provider Name | Total Requests | Success Rate | Avg Latency.

## Risks / Trade-offs
- **DB Growth**: High request volume could bloat the `provider_request_log` table.
  - **Mitigation**: We can add a scheduled job later to purge old logs (e.g., > 30 days) or implement table partitioning if MySQL is used.
- **Async Reliability**: If the application crashes before the async event is processed, the log is lost.
  - **Acceptable**: This is statistical data, 100% durability is not critical compared to system availability.

## Open Questions
- None.
