## ADDED Requirements

### Requirement: Record Provider Requests
The system SHALL intercept and persist details of every request made to external TTS providers (Tencent, Aliyun, etc.). Recorded details MUST include the provider identifier, request timestamp, success/failure status, and error message (if applicable).

#### Scenario: Successful Request Logging
- **WHEN** the system sends a request to the Tencent provider and receives a 200 OK response
- **THEN** a new statistic record is created for "Tencent" with status "SUCCESS"

#### Scenario: Failed Request Logging
- **WHEN** the system attempts to call the Aliyun provider and receives a 500 error or timeout
- **THEN** a new statistic record is created for "Aliyun" with status "FAILURE" and the error reason is stored

### Requirement: Statistics Retrieval API
The system SHALL provide an Admin API endpoint to retrieve aggregated usage statistics. The statistics MUST be actionable by provider and time range.

#### Scenario: Query Provider Stats
- **WHEN** an Admin requests statistics for the last 7 days
- **THEN** the system returns a breakdown of total requests, success count, and failure count for each provider

### Requirement: Statistics Dashboard
The Admin Portal SHALL include a dedicated page for visualizing provider performance statistics.

#### Scenario: View Stats Page
- **WHEN** an Admin user navigates to the "Statistics" page
- **THEN** they see a table or chart displaying request counts and success rates for all configured providers

### Requirement: Sortable Statistics
The statistics table in the Admin Portal SHALL be sortable by column headers (Provider Name, Total Requests, Success Rate, etc.) to allow admins to easily identify top-performing or problematic providers.

