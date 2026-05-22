package id.ac.ui.cs.advprog.hasilpanen.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisher {

    private final PublishableOutboxRepository repository;
    private final EventPublisherTransport transport;

    public OutboxPublisher(PublishableOutboxRepository repository, EventPublisherTransport transport) {
        this.repository = repository;
        this.transport = transport;
    }

    @Scheduled(fixedDelayString = "${payroll.outbox.publish-delay-ms:${PAYROLL_OUTBOX_PUBLISH_DELAY_MS:5000}}")
    public void publishPending() {
        for (OutboxEvent event : repository.findPending()) {
            try {
                transport.publish(event);
                repository.save(OutboxStateTransition.markSent(event));
            } catch (Exception ex) {
                repository.save(OutboxStateTransition.markFailed(event));
            }
        }
    }
}
