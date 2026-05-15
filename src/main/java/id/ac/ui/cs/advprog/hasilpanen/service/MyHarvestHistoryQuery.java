package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.time.LocalDate;
import java.util.UUID;

public record MyHarvestHistoryQuery(
        UUID buruhId,
        LocalDate startDate,
        LocalDate endDate,
        HarvestStatus status,
        int page,
        int size) {
}
