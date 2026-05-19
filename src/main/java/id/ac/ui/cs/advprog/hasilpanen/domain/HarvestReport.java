package id.ac.ui.cs.advprog.hasilpanen.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge;

public class HarvestReport {

    private final UUID harvestId;
    private final Long buruhId;
    private final Long mandorIdSnapshot;
    private final String kebunCodeSnapshot;
    private final String kebunIdSnapshot;
    private String buruhNameSnapshot;
    private final LocalDate harvestDate;
    private final BigDecimal kilogram;
    private final String reportText;
    private final List<String> photos;
    private HarvestStatus status;
    private String rejectionReason;
    private Long approvedBy;
    private OffsetDateTime approvedAt;
    private Long rejectedBy;
    private OffsetDateTime rejectedAt;

    private HarvestReport(
            UUID harvestId,
            Long buruhId,
            Long mandorIdSnapshot,
            String kebunCodeSnapshot,
            String kebunIdSnapshot,
            LocalDate harvestDate,
            BigDecimal kilogram,
            String reportText,
            List<String> photos) {
        this.harvestId = harvestId;
        this.buruhId = buruhId;
        this.mandorIdSnapshot = mandorIdSnapshot;
        this.kebunCodeSnapshot = kebunCodeSnapshot;
        this.kebunIdSnapshot = kebunIdSnapshot;
        this.harvestDate = harvestDate;
        this.kilogram = kilogram;
        this.reportText = reportText;
        this.photos = new ArrayList<>(photos);
        this.status = HarvestStatus.PENDING;
    }

    public static HarvestReport submit(
            UUID harvestId,
            Long buruhId,
            Long mandorIdSnapshot,
            String kebunCodeSnapshot,
            String kebunIdSnapshot,
            LocalDate harvestDate,
            BigDecimal kilogram,
            String reportText,
            List<String> photos) {
        HarvestValidationPolicy.validateSubmission(kilogram, reportText, photos);
        return new HarvestReport(harvestId, buruhId, mandorIdSnapshot, kebunCodeSnapshot, kebunIdSnapshot, harvestDate, kilogram, reportText, photos);
    }

    // Backward-compatible bridge for legacy UUID-based call sites.
    public static HarvestReport submit(
            UUID harvestId,
            UUID buruhId,
            LocalDate harvestDate,
            BigDecimal kilogram,
            String reportText,
            List<String> photos) {
        return submit(
                harvestId,
                uuidToLong(buruhId),
                null,
                null,
                null,
                harvestDate,
                kilogram,
                reportText,
                photos);
    }

    public HarvestStatus getStatus() {
        return status;
    }

    public UUID getHarvestId() {
        return harvestId;
    }

    public UUID getBuruhId() {
        return LegacyIdBridge.longToUuid(buruhId);
    }

    public Long getBuruhAuthId() { return buruhId; }

    public Long getMandorIdSnapshot() { return mandorIdSnapshot; }

    public String getKebunCodeSnapshot() { return kebunCodeSnapshot == null ? "" : kebunCodeSnapshot; }

    public String getKebunIdSnapshot() { return kebunIdSnapshot; }

    public String getBuruhNameSnapshot() {
        return buruhNameSnapshot == null ? "" : buruhNameSnapshot;
    }

    public void setBuruhNameSnapshot(String buruhNameSnapshot) {
        this.buruhNameSnapshot = buruhNameSnapshot;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public BigDecimal getKilogram() {
        return kilogram;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public Long getRejectedBy() {
        return rejectedBy;
    }

    public OffsetDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void approve(Long approvedBy) {
        if (status != HarvestStatus.PENDING) {
            throw new IllegalStateException("only pending harvest can be approved");
        }
        this.status = HarvestStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = OffsetDateTime.now();
    }

    public void approve(UUID approvedBy) {
        approve(uuidToLong(approvedBy));
    }

    public void updateSubmissionByBuruh(BigDecimal newKilogram, String newReportText, List<String> newPhotos) {
        throw new IllegalStateException("submitted harvest cannot be modified by buruh");
    }

    public void reject(Long rejectedBy, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("rejection reason is required");
        }
        if (status != HarvestStatus.PENDING) {
            throw new IllegalStateException("only pending harvest can be rejected");
        }

        this.status = HarvestStatus.REJECTED;
        this.rejectionReason = reason;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = OffsetDateTime.now();
    }

    public void reject(UUID rejectedBy, String reason) {
        reject(uuidToLong(rejectedBy), reason);
    }

    private static Long uuidToLong(UUID id) {
        return LegacyIdBridge.uuidToLong(id);
    }
}
