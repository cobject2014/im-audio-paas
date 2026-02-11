## 1. Backend Core

- [x] 1.1 Create `ProviderRequestLog` JPA Entity with fields (id, providerName, timestamp, isSuccess, errorMessage, latencyMs).
- [x] 1.2 Create `ProviderRequestLogRepository` interface.
- [x] 1.3 Define `ProviderRequestEvent` class extending `ApplicationEvent`.
- [x] 1.4 Implement `ProviderRequestEventListener` with `@Async` support to save logs to the repository.
- [x] 1.5 Enable Async support in Spring Boot configuration (`@EnableAsync`).

## 2. Backend Integration

- [x] 2.1 Refactor TTS orchestration logic to measure latency and capture success/failure outcomes.
- [x] 2.2 Publish `ProviderRequestEvent` from the TTS service layer upon request completion.

## 3. Backend API

- [x] 3.1 Create `StatisticsDTO` to transport aggregated data to the frontend.
- [x] 3.2 Implement `StatisticsService` to aggregate data from `ProviderRequestLogRepository` (count total, success, failure by provider).
- [x] 3.3 Implement `StatisticsController` exposing `GET /api/admin/statistics`.

## 4. Frontend Implementation

- [x] 4.1 Update `src/api/adminClient.ts` to include the `getStatistics` method.
- [x] 4.2 Create `src/pages/StatisticsPage.tsx` with a table displaying provider stats.
- [x] 4.3 Add the "Statistics" link to the navigation menu and register the route in `App.tsx`.
- [x] 4.4 Implement sorting for the statistics table in `StatisticsPage.tsx`.
- [x] 4.5 Set default sorting to "Total Requests" (Descending).




