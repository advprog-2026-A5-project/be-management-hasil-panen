package id.ac.ui.cs.advprog.hasilpanen.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateHarvestCommand(
        Long buruhId,
        Long mandorId,
        String kebunCode,
        String kebunId,
        BigDecimal kilogram,
        String reportText,
        List<String> photos) {

    public CreateHarvestCommand(
            UUID buruhId,
            BigDecimal kilogram,
            String reportText,
            List<String> photos) {
        this(LegacyIdBridge.uuidToLong(buruhId), null, null, null, kilogram, reportText, photos);
    }
}
