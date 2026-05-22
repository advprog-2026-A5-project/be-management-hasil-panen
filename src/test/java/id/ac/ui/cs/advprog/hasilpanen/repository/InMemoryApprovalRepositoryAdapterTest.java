package id.ac.ui.cs.advprog.hasilpanen.repository;

import static org.assertj.core.api.Assertions.assertThat;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryApprovalRepositoryAdapterTest {

    @Test
    void saveAndFindByIdRoundTripsReport() {
        InMemoryApprovalRepositoryAdapter repository = new InMemoryApprovalRepositoryAdapter();
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                21L,
                31L,
                "KB001",
                null,
                LocalDate.of(2026, 5, 21),
                BigDecimal.valueOf(120),
                "panen",
                List.of("proof.jpg"));

        repository.save(report);

        assertThat(repository.findById(report.getHarvestId())).contains(report);
    }
}
