package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.time.LocalDate;
import java.util.UUID;

public record MandorHarvestView(
        UUID harvestId,
        UUID buruhId,
        String buruhName,
        LocalDate harvestDate,
        HarvestStatus status,
        String rejectionReason) {
}
