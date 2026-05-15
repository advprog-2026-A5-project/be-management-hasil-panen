package id.ac.ui.cs.advprog.hasilpanen.service;

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
                repository.save(OutboxStateTransition.markSent(event));
            } catch (Exception ex) {
                repository.save(OutboxStateTransition.markFailed(event));
            }
        }
    }
}
