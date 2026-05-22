package id.ac.ui.cs.advprog.hasilpanen.controller;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestPhotoMetadataRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorHarvestView;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageProperties;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageService;
import id.ac.ui.cs.advprog.hasilpanen.storage.StoredFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"", "/api/hasil-panen"})
public class HarvestController {

    private final HarvestPublicApiService harvestPublicApiService;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final HarvestPhotoMetadataRepository photoMetadataRepository;

    public HarvestController(
            HarvestPublicApiService harvestPublicApiService,
            StorageService storageService,
            StorageProperties storageProperties,
            HarvestPhotoMetadataRepository photoMetadataRepository) {
        this.harvestPublicApiService = harvestPublicApiService;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
        this.photoMetadataRepository = photoMetadataRepository;
    }

    @PostMapping(value = "/harvests", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HarvestPublicApiService.HarvestSubmissionResult> submitHarvestJson(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SubmitHarvestRequest request) {
        HarvestPublicApiService.HarvestSubmissionResult result = harvestPublicApiService.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(request.kilogram(), request.reportText(), request.photos()),
                authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(value = "/harvests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HarvestPublicApiService.HarvestSubmissionResult> submitHarvestMultipart(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam BigDecimal kilogram,
            @RequestParam String reportText,
            @RequestPart("photos") MultipartFile[] photos) {
        List<MultipartFile> files = photos == null ? List.of() : Arrays.asList(photos);
        validateUploadFiles(files);

        List<StoredFile> storedFiles = files.stream()
                .map(file -> storageService.upload(file, "harvest-proofs"))
                .toList();

        HarvestPublicApiService.HarvestSubmissionResult result = harvestPublicApiService.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(
                        kilogram,
                        reportText,
                        storedFiles.stream().map(StoredFile::url).toList()),
                authorization);

        photoMetadataRepository.updatePhotoMetadata(result.harvestId(), storedFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/harvests/{harvestId}")
    public HarvestPublicApiService.HarvestDetailResult harvestDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID harvestId) {
        return harvestPublicApiService.getHarvestDetail(harvestId, authorization);
    }

    @GetMapping("/harvests/me")
    public List<HarvestPublicApiService.MyHarvestResult> myHarvests(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) HarvestStatus status) {
        return harvestPublicApiService.getMyHarvests(startDate, endDate, status, authorization);
    }

    @GetMapping("/mandor/harvests")
    public List<MandorHarvestView> mandorHarvests(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) LocalDate harvestDate,
            @RequestParam(required = false) String buruhName) {
        return harvestPublicApiService.getMandorHarvests(harvestDate, buruhName, authorization);
    }

    @GetMapping("/mandor/buruh/{buruhId}/harvests")
    public List<MandorHarvestView> mandorBuruhHarvests(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long buruhId,
            @RequestParam(required = false) LocalDate harvestDate) {
        return harvestPublicApiService.getMandorBuruhHarvests(buruhId, harvestDate, authorization);
    }

    @GetMapping("/harvest-reports/eligible-for-shipment")
    public List<HarvestPublicApiService.EligibleShipmentResult> eligibleForShipment(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return harvestPublicApiService.getEligibleForShipment(authorization);
    }

    @PostMapping("/harvests/{harvestId}/approve")
    public ResponseEntity<Void> approveHarvest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID harvestId) {
        harvestPublicApiService.approve(harvestId, authorization);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/harvests/{harvestId}/reject")
    public ResponseEntity<Void> rejectHarvest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID harvestId,
            @Valid @RequestBody RejectHarvestRequest request) {
        harvestPublicApiService.reject(harvestId, request.reason(), authorization);
        return ResponseEntity.ok().build();
    }

    private void validateUploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("at least one proof photo is required");
        }

        long maxBytes = (long) storageProperties.getMaxUploadSizeMb() * 1024 * 1024;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("proof photo must not be empty");
            }
            if (file.getContentType() == null || !storageProperties.getAllowedContentTypes().contains(file.getContentType())) {
                throw new IllegalArgumentException("unsupported file content type: " + file.getContentType());
            }
            if (file.getSize() > maxBytes) {
                throw new IllegalArgumentException("file exceeds max upload size");
            }
        }
    }

    public record SubmitHarvestRequest(
            @NotNull BigDecimal kilogram,
            @NotBlank String reportText,
            @NotEmpty List<String> photos) {
    }

    public record RejectHarvestRequest(@NotBlank String reason) {
    }
}
