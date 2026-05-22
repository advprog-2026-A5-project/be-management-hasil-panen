package id.ac.ui.cs.advprog.hasilpanen.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "harvest_photos")
public class HarvestPhotoEntity {

    @Id
    @Column(name = "harvest_photo_id", nullable = false)
    private UUID harvestPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_id", nullable = false)
    private HarvestReportEntity harvest;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getHarvestPhotoId() {
        return harvestPhotoId;
    }

    public void setHarvestPhotoId(UUID harvestPhotoId) {
        this.harvestPhotoId = harvestPhotoId;
    }

    public HarvestReportEntity getHarvest() {
        return harvest;
    }

    public void setHarvest(HarvestReportEntity harvest) {
        this.harvest = harvest;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
