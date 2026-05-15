package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryPublishableOutboxRepository implements PublishableOutboxRepository {

    private final Map<UUID, OutboxEvent> store = new ConcurrentHashMap<>();

    @Override
    public List<OutboxEvent> findPending() {
        List<OutboxEvent> pending = new ArrayList<>();
        for (OutboxEvent event : store.values()) {
            if ("PENDING".equals(event.status())) {
                pending.add(event);
            }
        }
        return pending;
    }

    @Override
    public Optional<OutboxEvent> findById(UUID eventId) {
        return Optional.ofNullable(store.get(eventId));
    }

    @Override
    public void save(OutboxEvent event) {
        store.put(event.eventId(), event);
    }
}
