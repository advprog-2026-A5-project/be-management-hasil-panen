package id.ac.ui.cs.advprog.hasilpanen.persistence.repository;

import id.ac.ui.cs.advprog.hasilpanen.persistence.entity.HarvestReportEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HarvestReportJpaRepository extends JpaRepository<HarvestReportEntity, UUID> {

    @EntityGraph(attributePaths = "photos")
    Optional<HarvestReportEntity> findByBuruhIdAndHarvestDate(Long buruhId, LocalDate harvestDate);

    @EntityGraph(attributePaths = "photos")
    List<HarvestReportEntity> findByBuruhId(Long buruhId);

    @EntityGraph(attributePaths = "photos")
    List<HarvestReportEntity> findByBuruhIdIn(Collection<Long> buruhIds);

    @EntityGraph(attributePaths = "photos")
    List<HarvestReportEntity> findByStatus(String status);

    @EntityGraph(attributePaths = "photos")
    Optional<HarvestReportEntity> findByHarvestId(UUID harvestId);
}
