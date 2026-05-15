package id.ac.ui.cs.advprog.hasilpanen.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HarvestReport {

    private final UUID harvestId;
    private final UUID buruhId;
    private String buruhNameSnapshot;
    private final LocalDate harvestDate;
    private final BigDecimal kilogram;
    private final String reportText;
    private final List<String> photos;
    private HarvestStatus status;
    private String rejectionReason;

    private HarvestReport(
            UUID harvestId,
            UUID buruhId,
            LocalDate harvestDate,
            BigDecimal kilogram,
            String reportText,
            List<String> photos) {
        this.harvestId = harvestId;
        this.buruhId = buruhId;
        this.harvestDate = harvestDate;
        this.kilogram = kilogram;
        this.reportText = reportText;
        this.photos = new ArrayList<>(photos);
        this.status = HarvestStatus.PENDING;
    }

    public static HarvestReport submit(
            UUID harvestId,
            UUID buruhId,
            LocalDate harvestDate,
            BigDecimal kilogram,
            String reportText,
            List<String> photos) {
        HarvestValidationPolicy.validateSubmission(kilogram, reportText, photos);
        return new HarvestReport(harvestId, buruhId, harvestDate, kilogram, reportText, photos);
    }

    public HarvestStatus getStatus() {
        return status;
    }

    public UUID getHarvestId() {
        return harvestId;
    }

    public UUID getBuruhId() {
        return buruhId;
    }

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

    public void updateSubmissionByBuruh(BigDecimal newKilogram, String newReportText, List<String> newPhotos) {
        throw new IllegalStateException("submitted harvest cannot be modified by buruh");
    }

    public void reject(UUID rejectedBy, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("rejection reason is required");
        }

        this.status = HarvestStatus.REJECTED;
        this.rejectionReason = reason;
    }
}
