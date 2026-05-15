package id.ac.ui.cs.advprog.hasilpanen.service;

import java.time.Instant;

public class OutboxPublisher {

    private final PublishableOutboxRepository repository;
    private final EventPublisherTransport transport;

    public OutboxPublisher(PublishableOutboxRepository repository, EventPublisherTransport transport) {
        this.repository = repository;
        this.transport = transport;
    }

    public void publishPending() {
        for (OutboxEvent event : repository.findPending()) {
            try {
                transport.publish(event);
                repository.save(new OutboxEvent(
                        event.eventId(),
                        event.aggregateId(),
                        event.aggregateType(),
                        event.eventType(),
                        event.payload(),
                        "SENT",
                        event.createdAt(),
                        Instant.now(),
                        event.retryCount()));
            } catch (Exception ex) {
                repository.save(new OutboxEvent(
                        event.eventId(),
                        event.aggregateId(),
                        event.aggregateType(),
                        event.eventType(),
                        event.payload(),
                        "FAILED",
                        event.createdAt(),
                        event.publishedAt(),
                        event.retryCount() + 1));
            }
        }
    }
}
