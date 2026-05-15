package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.ArrayList;
import java.util.List;

class InMemoryOutboxEventRepository implements OutboxEventRepository {

    private final List<OutboxEvent> events = java.util.Collections.synchronizedList(new ArrayList<>());

    @Override
    public void save(OutboxEvent event) {
        events.add(event);
    }

    @Override
    public List<OutboxEvent> findAll() {
        return List.copyOf(events);
    }
}
