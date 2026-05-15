package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MyHarvestHistoryResult(List<Item> items, long totalItems, int totalPages) {

    public record Item(
            UUID harvestId,
            LocalDate harvestDate,
            HarvestStatus status,
            String rejectionReason) {
    }
}
