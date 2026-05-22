# Profiling Notes (Hasil Panen)

Targeted hotspots:
- Harvest history filtering for Buruh/Mandor lists.
- Approval path (state transition + outbox insert).
- Multipart upload handling and metadata persistence.

Optimizations implemented:
- Added DB indexes for status/date/kebun/mandor and outbox status+created_at.
- Kept payroll dispatch asynchronous via outbox publisher.
- Stored photo metadata in relational table instead of serialized string.

Future measurements:
- Compare query latency on large harvest table before/after index rollout.
- Track outbox publish throughput and retry counts in staging.
