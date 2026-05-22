package id.ac.ui.cs.advprog.hasilpanen.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "harvest_reports")
public class HarvestReportEntity {

    @Id
    @Column(name = "harvest_id", nullable = false)
    private UUID harvestId;

    @Column(name = "buruh_id", nullable = false)
    private Long buruhId;

    @Column(name = "buruh_name")
    private String buruhName;

    @Column(name = "mandor_id")
    private Long mandorId;

    @Column(name = "kebun_id")
    private String kebunId;

    @Column(name = "kebun_code")
    private String kebunCode;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "kilogram", nullable = false)
    private BigDecimal kilogram;

    @Column(name = "report_text", nullable = false)
    private String reportText;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "harvest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdAt asc")
    private List<HarvestPhotoEntity> photos = new ArrayList<>();

    public UUID getHarvestId() {
        return harvestId;
    }

    public void setHarvestId(UUID harvestId) {
        this.harvestId = harvestId;
    }

    public Long getBuruhId() {
        return buruhId;
    }

    public void setBuruhId(Long buruhId) {
        this.buruhId = buruhId;
    }

    public String getBuruhName() {
        return buruhName;
    }

    public void setBuruhName(String buruhName) {
        this.buruhName = buruhName;
    }

    public Long getMandorId() {
        return mandorId;
    }

    public void setMandorId(Long mandorId) {
        this.mandorId = mandorId;
    }

    public String getKebunId() {
        return kebunId;
    }

    public void setKebunId(String kebunId) {
        this.kebunId = kebunId;
    }

    public String getKebunCode() {
        return kebunCode;
    }

    public void setKebunCode(String kebunCode) {
        this.kebunCode = kebunCode;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDate harvestDate) {
        this.harvestDate = harvestDate;
    }

    public BigDecimal getKilogram() {
        return kilogram;
    }

    public void setKilogram(BigDecimal kilogram) {
        this.kilogram = kilogram;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Long getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(Long rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public OffsetDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(OffsetDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<HarvestPhotoEntity> getPhotos() {
        return photos;
    }

    public void setPhotos(List<HarvestPhotoEntity> photos) {
        this.photos.clear();
        if (photos == null) {
            return;
        }
        for (HarvestPhotoEntity photo : photos) {
            addPhoto(photo);
        }
    }

    public void addPhoto(HarvestPhotoEntity photo) {
        photo.setHarvest(this);
        this.photos.add(photo);
    }
}
