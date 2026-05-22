package id.ac.ui.cs.advprog.hasilpanen.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HarvestReportDomainTest {

    @Test
    void testHarvestReportRequiresPositiveKilogram() {
        assertThatThrownBy(() -> HarvestReport.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                BigDecimal.ZERO,
                "ok",
                List.of("https://a")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kilogram");
    }

    @Test
    void testHarvestReportRequiresReportText() {
        assertThatThrownBy(() -> HarvestReport.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                BigDecimal.ONE,
                "   ",
                List.of("https://a")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("report_text");
    }

    @Test
    void testHarvestReportRequiresAtLeastOnePhoto() {
        assertThatThrownBy(() -> HarvestReport.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                BigDecimal.ONE,
                "valid",
                List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("photos");
    }

    @Test
    void testHarvestReportDefaultStatusIsPending() {
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                BigDecimal.TEN,
                "valid",
                List.of("https://a"));

        assertThat(report.getStatus()).isEqualTo(HarvestStatus.PENDING);
    }

    @Test
    void testHarvestReportCannotBeModifiedByBuruhAfterSubmission() {
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                BigDecimal.TEN,
                "valid",
                List.of("https://a"));

        assertThatThrownBy(() -> report.updateSubmissionByBuruh(BigDecimal.ONE, "edited", List.of("https://b")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be modified");
    }

    @Test
    void testHarvestReportRejectionRequiresReason() {
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                2L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.TEN,
                "valid",
                List.of("https://a"));

        assertThatThrownBy(() -> report.reject(3L, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void testHarvestReportStoresAuthIdsAndKebunSnapshot() {
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                10L,
                20L,
                "KB-ALPHA",
                null,
                LocalDate.now(),
                BigDecimal.TEN,
                "valid",
                List.of("https://a"));

        assertThat(report.getBuruhAuthId()).isEqualTo(10L);
        assertThat(report.getMandorIdSnapshot()).isEqualTo(20L);
        assertThat(report.getKebunCodeSnapshot()).isEqualTo("KB-ALPHA");
    }
}
