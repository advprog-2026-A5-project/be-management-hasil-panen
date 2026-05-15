package id.ac.ui.cs.advprog.hasilpanen.service;

import java.time.Instant;

final class OutboxStateTransition {

    private OutboxStateTransition() {
    }

    static OutboxEvent markSent(OutboxEvent event) {
        return new OutboxEvent(
                event.eventId(),
                event.aggregateId(),
                event.aggregateType(),
                event.eventType(),
                event.payload(),
                "SENT",
                event.createdAt(),
                Instant.now(),
                event.retryCount());
    }

    static OutboxEvent markFailed(OutboxEvent event) {
        return new OutboxEvent(
                event.eventId(),
                event.aggregateId(),
                event.aggregateType(),
                event.eventType(),
                event.payload(),
                "FAILED",
                event.createdAt(),
                event.publishedAt(),
                event.retryCount() + 1);
    }
}
