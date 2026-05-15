package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransportEligibilityServiceTest {

    @Test
    void testTransportEligibilityReturnsEligibleForApproved() {
        HarvestReport report = report();
        report.approve(UUID.randomUUID());

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        TransportEligibilityService service = new TransportEligibilityService(repository);
        var result = service.getEligibility(report.getHarvestId());

        assertThat(result.eligible()).isTrue();
        assertThat(result.status().name()).isEqualTo("APPROVED");
    }

    @Test
    void testTransportEligibilityReturnsFalseForPending() {
        HarvestReport report = report();
        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        TransportEligibilityService service = new TransportEligibilityService(repository);
        var result = service.getEligibility(report.getHarvestId());

        assertThat(result.eligible()).isFalse();
        assertThat(result.status().name()).isEqualTo("PENDING");
    }

    @Test
    void testTransportEligibilityReturnsFalseForRejected() {
        HarvestReport report = report();
        report.reject(UUID.randomUUID(), "invalid");

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        TransportEligibilityService service = new TransportEligibilityService(repository);
        var result = service.getEligibility(report.getHarvestId());

        assertThat(result.eligible()).isFalse();
        assertThat(result.status().name()).isEqualTo("REJECTED");
    }

    @Test
    void testTransportEligibilityReturnsNotFoundForMissingHarvest() {
        TransportEligibilityService service = new TransportEligibilityService(new InMemoryApprovalRepository());

        assertThatThrownBy(() -> service.getEligibility(UUID.randomUUID()))
                .isInstanceOf(HarvestNotFoundException.class);
    }

    private HarvestReport report() {
        return HarvestReport.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 5, 12),
                BigDecimal.valueOf(98),
                "panen",
                List.of("https://photo"));
    }
}
