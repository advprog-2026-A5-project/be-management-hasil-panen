package id.ac.ui.cs.advprog.hasilpanen.service;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID eventId,
        UUID aggregateId,
        String aggregateType,
        String eventType,
        String payload,
        String status,
        Instant createdAt,
        Instant publishedAt,
        int retryCount) {
}
