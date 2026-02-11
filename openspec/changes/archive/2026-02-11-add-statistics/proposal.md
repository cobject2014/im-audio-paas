## Why

The system currently lacks visibility into the performance and reliability of external TTS providers (Tencent, Aliyun Standard, Aliyun Cosy). Administrators need to track request volumes, success/failure rates, and specific failure reasons to monitor costs and reliability.

## What Changes

-  **Backend**: Intercept and record all TTS requests to external providers.
-  **Data Model**: Store request logs with provider ID, status, and failure reason in the database (H2/MySQL).
-  **API**: New Admin API endpoints to query aggregated statistics (e.g., success rate per provider).
-  **Frontend**: New "Statistics" page in the Admin Portal to visualize request counts and outcomes.
-  **Sorting**: The table should support column sorting. The *default* sorting must be by "Total Requests" in descending order (highest volume first).

## Capabilities

### New Capabilities
- `provider-statistics`: Tracking, storage, and retrieval of TTS provider usage and reliability metrics.

### Modified Capabilities
<!-- No existing functional requirements are changing, just adding observation. -->

## Impact

- **Backend**: New JPA entities, Repository, and Service logic. Potential AOP or Service changes to hook into the TTS flow.
- **Frontend**: New UI components and Route.
- **Database**: New tables for storing statistics.
