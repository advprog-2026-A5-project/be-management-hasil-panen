package id.ac.ui.cs.advprog.hasilpanen.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

final class HarvestValidationPolicy {

    private HarvestValidationPolicy() {
    }

    static void validateSubmission(BigDecimal kilogram, String reportText, List<String> photos) {
        if (kilogram == null || kilogram.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("kilogram must be positive");
        }
        if (reportText == null || reportText.isBlank()) {
            throw new IllegalArgumentException("report_text must not be blank");
        }
        if (photos == null || photos.isEmpty()) {
            throw new IllegalArgumentException("photos must contain at least one item");
        }
        if (photos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("photos must not contain null value");
        }
    }
}
