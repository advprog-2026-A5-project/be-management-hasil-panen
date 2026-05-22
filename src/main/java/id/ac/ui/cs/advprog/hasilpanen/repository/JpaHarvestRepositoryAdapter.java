package id.ac.ui.cs.advprog.hasilpanen.repository;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.persistence.entity.HarvestPhotoEntity;
import id.ac.ui.cs.advprog.hasilpanen.persistence.entity.HarvestReportEntity;
import id.ac.ui.cs.advprog.hasilpanen.persistence.repository.HarvestReportJpaRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.ApprovalRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestHistoryRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorHarvestRepository;
import id.ac.ui.cs.advprog.hasilpanen.storage.StoredFile;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaHarvestRepositoryAdapter implements HarvestReportRepository,
        HarvestHistoryRepository,
        MandorHarvestRepository,
        ApprovalRepository,
        HarvestPhotoMetadataRepository {

    private final HarvestReportJpaRepository jpaRepository;

    public JpaHarvestRepositoryAdapter(HarvestReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<HarvestReport> findByBuruhIdAndHarvestDate(UUID buruhId, java.time.LocalDate harvestDate) {
        Long buruhAuthId = id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.uuidToLong(buruhId);
        return jpaRepository.findByBuruhIdAndHarvestDate(buruhAuthId, harvestDate).map(this::toDomain);
    }

    @Override
    public HarvestReport save(HarvestReport report) {
        HarvestReportEntity saved = jpaRepository.save(toEntity(report));
        return toDomain(saved);
    }

    @Override
    public Optional<HarvestReport> findById(UUID harvestId) {
        return jpaRepository.findByHarvestId(harvestId).map(this::toDomain);
    }

    @Override
    public List<HarvestReport> findByBuruhId(UUID buruhId) {
        Long buruhAuthId = id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.uuidToLong(buruhId);
        return jpaRepository.findByBuruhId(buruhAuthId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HarvestReport> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<HarvestReport> findByBuruhId(Long buruhId) {
        return jpaRepository.findByBuruhId(buruhId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HarvestReport> findAllByBuruhIdsLong(java.util.Set<Long> buruhIds) {
        if (buruhIds == null || buruhIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByBuruhIdIn(buruhIds).stream().map(this::toDomain).toList();
    }

    @Override
    public void updatePhotoMetadata(UUID harvestId, List<StoredFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        HarvestReportEntity entity = jpaRepository.findByHarvestId(harvestId)
                .orElseThrow(() -> new id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException("harvest not found"));

        entity.getPhotos().clear();
        OffsetDateTime now = OffsetDateTime.now();
        for (StoredFile file : files) {
            HarvestPhotoEntity photo = new HarvestPhotoEntity();
            photo.setHarvestPhotoId(UUID.randomUUID());
            photo.setPhotoUrl(file.url());
            photo.setPublicId(file.key());
            photo.setOriginalFilename(file.originalFilename());
            photo.setContentType(file.contentType());
            photo.setSizeBytes(file.size());
            photo.setCreatedAt(now);
            entity.addPhoto(photo);
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        jpaRepository.save(entity);
    }

    public List<HarvestReport> findApprovedHarvests() {
        return jpaRepository.findByStatus(HarvestStatus.APPROVED.name()).stream().map(this::toDomain).toList();
    }

    private HarvestReport toDomain(HarvestReportEntity entity) {
        List<String> photos = entity.getPhotos().stream()
                .sorted(Comparator.comparing(HarvestPhotoEntity::getCreatedAt))
                .map(HarvestPhotoEntity::getPhotoUrl)
                .toList();

        return HarvestReport.restore(
                entity.getHarvestId(),
                entity.getBuruhId(),
                entity.getMandorId(),
                entity.getKebunCode(),
                entity.getKebunId(),
                entity.getBuruhName(),
                entity.getHarvestDate(),
                entity.getKilogram(),
                entity.getReportText(),
                photos,
                HarvestStatus.valueOf(entity.getStatus()),
                entity.getRejectionReason(),
                entity.getApprovedBy(),
                entity.getApprovedAt(),
                entity.getRejectedBy(),
                entity.getRejectedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private HarvestReportEntity toEntity(HarvestReport report) {
        HarvestReportEntity entity = new HarvestReportEntity();
        entity.setHarvestId(report.getHarvestId());
        entity.setBuruhId(report.getBuruhAuthId());
        entity.setBuruhName(report.getBuruhNameSnapshot());
        entity.setMandorId(report.getMandorIdSnapshot());
        entity.setKebunCode(report.getKebunCodeSnapshot());
        entity.setKebunId(report.getKebunIdSnapshot());
        entity.setHarvestDate(report.getHarvestDate());
        entity.setKilogram(report.getKilogram());
        entity.setReportText(report.getReportText());
        entity.setStatus(report.getStatus().name());
        entity.setRejectionReason(report.getRejectionReason());
        entity.setApprovedBy(report.getApprovedBy());
        entity.setApprovedAt(report.getApprovedAt());
        entity.setRejectedBy(report.getRejectedBy());
        entity.setRejectedAt(report.getRejectedAt());

        OffsetDateTime createdAt = report.getCreatedAt() == null ? OffsetDateTime.now() : report.getCreatedAt();
        OffsetDateTime updatedAt = report.getUpdatedAt() == null ? OffsetDateTime.now() : report.getUpdatedAt();
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        List<HarvestPhotoEntity> photos = report.getPhotos().stream().map(url -> {
            HarvestPhotoEntity photo = new HarvestPhotoEntity();
            photo.setHarvestPhotoId(UUID.randomUUID());
            photo.setPhotoUrl(url);
            photo.setCreatedAt(createdAt);
            return photo;
        }).toList();
        entity.setPhotos(photos);
        return entity;
    }
}
