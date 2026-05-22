package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublishableOutboxRepository {

    List<OutboxEvent> findPending();

    Optional<OutboxEvent> findById(UUID eventId);

    void save(OutboxEvent event);

    default void saveAll(List<OutboxEvent> events) {
        for (OutboxEvent event : events) {
            save(event);
        }
    }
}
