package id.ac.ui.cs.advprog.hasilpanen.repository;

import id.ac.ui.cs.advprog.hasilpanen.persistence.entity.OutboxEventEntity;
import id.ac.ui.cs.advprog.hasilpanen.persistence.repository.OutboxEventJpaRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.OutboxEvent;
import id.ac.ui.cs.advprog.hasilpanen.service.OutboxEventRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.PublishableOutboxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaOutboxRepositoryAdapter implements OutboxEventRepository, PublishableOutboxRepository {

    private final OutboxEventJpaRepository jpaRepository;

    public JpaOutboxRepositoryAdapter(OutboxEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(OutboxEvent event) {
        jpaRepository.save(toEntity(event));
    }

    @Override
    public List<OutboxEvent> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<OutboxEvent> findPending() {
        return jpaRepository.findByStatusInOrderByCreatedAtAsc(List.of("PENDING", "FAILED"))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<OutboxEvent> findById(UUID eventId) {
        return jpaRepository.findById(eventId).map(this::toDomain);
    }

    private OutboxEventEntity toEntity(OutboxEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(event.eventId());
        entity.setAggregateId(event.aggregateId());
        entity.setAggregateType(event.aggregateType());
        entity.setEventType(event.eventType());
        entity.setPayload(event.payload());
        entity.setStatus(event.status());
        entity.setCreatedAt(event.createdAt());
        entity.setPublishedAt(event.publishedAt());
        entity.setRetryCount(event.retryCount());
        return entity;
    }

    private OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(
                entity.getEventId(),
                entity.getAggregateId(),
                entity.getAggregateType(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getPublishedAt(),
                entity.getRetryCount());
    }
}
