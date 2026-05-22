package id.ac.ui.cs.advprog.hasilpanen.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.service.OutboxEvent;
import id.ac.ui.cs.advprog.hasilpanen.storage.StoredFile;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JpaAdaptersIntegrationTest {

    @Autowired
    private id.ac.ui.cs.advprog.hasilpanen.persistence.repository.HarvestReportJpaRepository harvestReportJpaRepository;
    @Autowired
    private id.ac.ui.cs.advprog.hasilpanen.persistence.repository.OutboxEventJpaRepository outboxEventJpaRepository;

    private JpaHarvestRepositoryAdapter harvestAdapter;
    private JpaOutboxRepositoryAdapter outboxAdapter;

    @BeforeEach
    void setUp() {
        outboxEventJpaRepository.deleteAll();
        harvestReportJpaRepository.deleteAll();
        harvestAdapter = new JpaHarvestRepositoryAdapter(harvestReportJpaRepository);
        outboxAdapter = new JpaOutboxRepositoryAdapter(outboxEventJpaRepository);
    }

    @Test
    void harvestAdapterMapsDomainRoundTripIncludingPhotosAndSnapshotFields() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.submit(
                harvestId,
                21L,
                31L,
                "KB001",
                "KEBUN-1",
                LocalDate.of(2026, 5, 22),
                BigDecimal.valueOf(120.5),
                "panen blok A",
                List.of("https://cdn/1.jpg", "https://cdn/2.jpg"));
        report.setBuruhNameSnapshot("Buruh Test");

        harvestAdapter.save(report);
        HarvestReport saved = harvestAdapter.findById(harvestId).orElseThrow();

        assertThat(saved.getBuruhAuthId()).isEqualTo(21L);
        assertThat(saved.getMandorIdSnapshot()).isEqualTo(31L);
        assertThat(saved.getKebunCodeSnapshot()).isEqualTo("KB001");
        assertThat(saved.getKebunIdSnapshot()).isEqualTo("KEBUN-1");
        assertThat(saved.getBuruhNameSnapshot()).isEqualTo("Buruh Test");
        assertThat(saved.getPhotos()).containsExactly("https://cdn/1.jpg", "https://cdn/2.jpg");
        assertThat(harvestAdapter.findByBuruhId(21L)).hasSize(1);
        assertThat(harvestAdapter.findByBuruhId(saved.getBuruhId())).hasSize(1);
        assertThat(harvestAdapter.findByBuruhIdAndHarvestDate(saved.getBuruhId(), LocalDate.of(2026, 5, 22))).isPresent();
    }

    @Test
    void harvestAdapterUpdatePhotoMetadataReplacesOldPhotosAndStoresMetadata() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.submit(
                harvestId,
                21L,
                31L,
                "KB001",
                null,
                LocalDate.of(2026, 5, 22),
                BigDecimal.valueOf(100),
                "panen",
                List.of("https://old/1.jpg"));
        harvestAdapter.save(report);

        harvestAdapter.updatePhotoMetadata(harvestId, List.of(
                new StoredFile("https://new/1.jpg", "p1", "one.jpg", "image/jpeg", 100L),
                new StoredFile("https://new/2.jpg", "p2", "two.jpg", "image/jpeg", 120L)));

        HarvestReport updated = harvestAdapter.findById(harvestId).orElseThrow();
        assertThat(updated.getPhotos()).containsExactly("https://new/1.jpg", "https://new/2.jpg");
    }

    @Test
    void harvestAdapterUpdatePhotoMetadataThrowsForMissingHarvest() {
        assertThatThrownBy(() -> harvestAdapter.updatePhotoMetadata(UUID.randomUUID(), List.of(
                new StoredFile("https://new/1.jpg", "p1", "one.jpg", "image/jpeg", 100L))))
                .isInstanceOf(id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException.class);
    }

    @Test
    void harvestAdapterFindApprovedHarvestsReturnsOnlyApproved() {
        HarvestReport approved = HarvestReport.submit(
                UUID.randomUUID(),
                22L,
                31L,
                "KB001",
                null,
                LocalDate.of(2026, 5, 22),
                BigDecimal.valueOf(100),
                "ok",
                List.of("https://a.jpg"));
        approved.approve(31L);

        HarvestReport pending = HarvestReport.submit(
                UUID.randomUUID(),
                23L,
                31L,
                "KB001",
                null,
                LocalDate.of(2026, 5, 22),
                BigDecimal.valueOf(90),
                "ok",
                List.of("https://b.jpg"));

        harvestAdapter.save(approved);
        harvestAdapter.save(pending);

        assertThat(harvestAdapter.findApprovedHarvests())
                .extracting(HarvestReport::getHarvestId)
                .containsExactly(approved.getHarvestId());
        assertThat(harvestAdapter.findAllByBuruhIdsLong(Set.of())).isEmpty();
    }

    @Test
    void outboxAdapterSavesAndFindsPendingAndFailedInCreatedOrder() {
        OutboxEvent pending = new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HarvestReport",
                "harvest.approved.v1",
                "{\"event\":\"pending\"}",
                "PENDING",
                Instant.parse("2026-05-22T00:00:00Z"),
                null,
                0);
        OutboxEvent failed = new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HarvestReport",
                "harvest.approved.v1",
                "{\"event\":\"failed\"}",
                "FAILED",
                Instant.parse("2026-05-22T01:00:00Z"),
                null,
                2);
        OutboxEvent sent = new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HarvestReport",
                "harvest.approved.v1",
                "{\"event\":\"sent\"}",
                "SENT",
                Instant.parse("2026-05-22T02:00:00Z"),
                Instant.parse("2026-05-22T03:00:00Z"),
                0);

        outboxAdapter.save(sent);
        outboxAdapter.save(failed);
        outboxAdapter.save(pending);

        List<OutboxEvent> pendingEvents = outboxAdapter.findPending();
        assertThat(pendingEvents).hasSize(2);
        assertThat(pendingEvents.get(0).status()).isEqualTo("PENDING");
        assertThat(pendingEvents.get(1).status()).isEqualTo("FAILED");
        assertThat(outboxAdapter.findById(sent.eventId())).isPresent();
        assertThat(outboxAdapter.findAll()).hasSize(3);
    }

    @Test
    void harvestReportEntityPhotoRelationshipMaintainsBackReference() {
        var entity = new id.ac.ui.cs.advprog.hasilpanen.persistence.entity.HarvestReportEntity();
        entity.setHarvestId(UUID.randomUUID());
        entity.setBuruhId(2L);
        entity.setMandorId(3L);
        entity.setKebunCode("KB001");
        entity.setHarvestDate(LocalDate.of(2026, 5, 22));
        entity.setKilogram(BigDecimal.TEN);
        entity.setReportText("ok");
        entity.setStatus("PENDING");
        entity.setVersion(0L);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        var photo = new id.ac.ui.cs.advprog.hasilpanen.persistence.entity.HarvestPhotoEntity();
        photo.setHarvestPhotoId(UUID.randomUUID());
        photo.setPhotoUrl("https://cdn/proof.jpg");
        photo.setCreatedAt(OffsetDateTime.now());
        entity.setPhotos(List.of(photo));

        assertThat(entity.getPhotos()).hasSize(1);
        assertThat(entity.getPhotos().getFirst().getHarvest()).isEqualTo(entity);
    }
}
