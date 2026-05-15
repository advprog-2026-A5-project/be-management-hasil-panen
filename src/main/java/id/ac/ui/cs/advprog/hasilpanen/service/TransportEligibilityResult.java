package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record TransportEligibilityResult(
        UUID harvestId,
        boolean eligible,
        HarvestStatus status,
        BigDecimal kilogram) {
}
