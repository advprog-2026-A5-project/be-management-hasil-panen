package id.ac.ui.cs.advprog.hasilpanen.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateHarvestCommand(
        UUID buruhId,
        BigDecimal kilogram,
        String reportText,
        List<String> photos) {
}
