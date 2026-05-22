package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.List;

public interface OutboxEventRepository {

    void save(OutboxEvent event);

    default List<OutboxEvent> findAll() {
        return List.of();
    }
}
