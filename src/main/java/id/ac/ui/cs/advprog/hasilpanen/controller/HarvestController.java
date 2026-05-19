package id.ac.ui.cs.advprog.hasilpanen.controller;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorHarvestView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HarvestController {

    private final HarvestPublicApiService harvestPublicApiService;

    public HarvestController(HarvestPublicApiService harvestPublicApiService) {
        this.harvestPublicApiService = harvestPublicApiService;
    }

    @PostMapping("/harvests")
    public HarvestPublicApiService.HarvestSubmissionResult submitHarvest(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SubmitHarvestRequest request) {
        return harvestPublicApiService.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(request.kilogram(), request.reportText(), request.photos()),
                authorization);
    }

    @GetMapping("/harvests/me")
    public List<HarvestPublicApiService.MyHarvestResult> myHarvests(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) HarvestStatus status) {
        return harvestPublicApiService.getMyHarvests(startDate, endDate, status, authorization);
    }

    @GetMapping("/mandor/harvests")
    public List<MandorHarvestView> mandorHarvests(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) LocalDate harvestDate,
            @RequestParam(required = false) String buruhName) {
        return harvestPublicApiService.getMandorHarvests(harvestDate, buruhName, authorization);
    }

    @PostMapping("/harvests/{harvestId}/approve")
    public void approveHarvest(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID harvestId) {
        harvestPublicApiService.approve(harvestId, authorization);
    }

    @PostMapping("/harvests/{harvestId}/reject")
    public void rejectHarvest(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID harvestId,
            @Valid @RequestBody RejectHarvestRequest request) {
        harvestPublicApiService.reject(harvestId, request.reason(), authorization);
    }

    public record SubmitHarvestRequest(
            @NotNull BigDecimal kilogram,
            @NotBlank String reportText,
            @NotEmpty List<String> photos) {}

    public record RejectHarvestRequest(@NotBlank String reason) {}
}
