package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxPublisherTest {

    @Test
    void testOutboxPublisherPublishesPendingEvents() {
        InMemoryPublishableOutboxRepository repository = new InMemoryPublishableOutboxRepository();
        OutboxEvent event = event("PENDING", 0, null);
        repository.save(event);

        RecordingEventPublisherTransport transport = new RecordingEventPublisherTransport(false);
        OutboxPublisher publisher = new OutboxPublisher(repository, transport);

        publisher.publishPending();

        assertThat(transport.publishedEvents()).hasSize(1);
    }

    @Test
    void testOutboxPublisherMarksSentAfterSuccess() {
        InMemoryPublishableOutboxRepository repository = new InMemoryPublishableOutboxRepository();
        OutboxEvent event = event("PENDING", 0, null);
        repository.save(event);

        RecordingEventPublisherTransport transport = new RecordingEventPublisherTransport(false);
        OutboxPublisher publisher = new OutboxPublisher(repository, transport);

        publisher.publishPending();

        OutboxEvent updated = repository.findById(event.eventId()).orElseThrow();
        assertThat(updated.status()).isEqualTo("SENT");
        assertThat(updated.publishedAt()).isNotNull();
    }

    @Test
    void testOutboxPublisherIncrementsRetryAfterFailure() {
        InMemoryPublishableOutboxRepository repository = new InMemoryPublishableOutboxRepository();
        OutboxEvent event = event("PENDING", 0, null);
        repository.save(event);

        RecordingEventPublisherTransport transport = new RecordingEventPublisherTransport(true);
        OutboxPublisher publisher = new OutboxPublisher(repository, transport);

        publisher.publishPending();

        OutboxEvent updated = repository.findById(event.eventId()).orElseThrow();
        assertThat(updated.status()).isEqualTo("FAILED");
        assertThat(updated.retryCount()).isEqualTo(1);
    }

    @Test
    void testOutboxPublisherDoesNotRetryFailedEventFromInMemoryRepository() {
        InMemoryPublishableOutboxRepository repository = new InMemoryPublishableOutboxRepository();
        OutboxEvent failed = event("FAILED", 2, null);
        repository.save(failed);

        RecordingEventPublisherTransport transport = new RecordingEventPublisherTransport(true);
        OutboxPublisher publisher = new OutboxPublisher(repository, transport);

        publisher.publishPending();

        OutboxEvent updated = repository.findById(failed.eventId()).orElseThrow();
        assertThat(updated.status()).isEqualTo("FAILED");
        assertThat(updated.retryCount()).isEqualTo(2);
        assertThat(transport.publishedEvents()).isEmpty();
    }

    @Test
    void testOutboxPublisherDoesNotRepublishSentEvents() {
        InMemoryPublishableOutboxRepository repository = new InMemoryPublishableOutboxRepository();
        OutboxEvent sent = event("SENT", 0, Instant.now());
        OutboxEvent pending = event("PENDING", 0, null);
        repository.saveAll(List.of(sent, pending));

        RecordingEventPublisherTransport transport = new RecordingEventPublisherTransport(false);
        OutboxPublisher publisher = new OutboxPublisher(repository, transport);

        publisher.publishPending();

        assertThat(transport.publishedEvents()).hasSize(1);
        assertThat(transport.publishedEvents().getFirst().eventId()).isEqualTo(pending.eventId());
    }

    private OutboxEvent event(String status, int retryCount, Instant publishedAt) {
        return new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HarvestReport",
                "PAYROLL_TRIGGERED",
                "{}",
                status,
                Instant.now(),
                publishedAt,
                retryCount);
    }
}
