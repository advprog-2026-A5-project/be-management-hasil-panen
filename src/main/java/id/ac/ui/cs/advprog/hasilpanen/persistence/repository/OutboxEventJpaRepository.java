package id.ac.ui.cs.advprog.hasilpanen.persistence.repository;

import id.ac.ui.cs.advprog.hasilpanen.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
