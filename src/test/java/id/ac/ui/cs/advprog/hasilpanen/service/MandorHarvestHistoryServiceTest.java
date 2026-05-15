package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MandorHarvestHistoryServiceTest {

    @Test
    void testMandorListHarvestsReturnsAssignedBuruhOnly() {
        UUID mandorId = UUID.randomUUID();
        UUID assigned = UUID.randomUUID();
        UUID unassigned = UUID.randomUUID();

        InMemoryMandorHarvestRepository repository = new InMemoryMandorHarvestRepository();
        repository.save(report(assigned, "Budi", LocalDate.of(2026, 5, 10)));
        repository.save(report(unassigned, "Andi", LocalDate.of(2026, 5, 10)));

        MandorHarvestHistoryService service = new MandorHarvestHistoryService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(assigned)));

        var result = service.listAssignedHarvests(new MandorHarvestListQuery(mandorId, null, null));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().buruhId()).isEqualTo(assigned);
    }

    @Test
    void testMandorListHarvestsFiltersByDate() {
        UUID mandorId = UUID.randomUUID();
        UUID assigned = UUID.randomUUID();

        InMemoryMandorHarvestRepository repository = new InMemoryMandorHarvestRepository();
        repository.save(report(assigned, "Budi", LocalDate.of(2026, 5, 10)));
        repository.save(report(assigned, "Budi", LocalDate.of(2026, 5, 11)));

        MandorHarvestHistoryService service = new MandorHarvestHistoryService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(assigned)));

        var result = service.listAssignedHarvests(new MandorHarvestListQuery(mandorId, LocalDate.of(2026, 5, 11), null));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().harvestDate()).isEqualTo(LocalDate.of(2026, 5, 11));
    }

    @Test
    void testMandorListHarvestsFiltersByName() {
        UUID mandorId = UUID.randomUUID();
        UUID assigned = UUID.randomUUID();

        InMemoryMandorHarvestRepository repository = new InMemoryMandorHarvestRepository();
        repository.save(report(assigned, "Budi", LocalDate.of(2026, 5, 10)));
        repository.save(report(assigned, "Andi", LocalDate.of(2026, 5, 11)));

        MandorHarvestHistoryService service = new MandorHarvestHistoryService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(assigned)));

        var result = service.listAssignedHarvests(new MandorHarvestListQuery(mandorId, null, "Bud"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().buruhName()).isEqualTo("Budi");
    }

    @Test
    void testMandorGetBuruhHarvestsFailsWhenUnauthorized() {
        UUID mandorId = UUID.randomUUID();
        UUID notAssigned = UUID.randomUUID();

        InMemoryMandorHarvestRepository repository = new InMemoryMandorHarvestRepository();
        MandorHarvestHistoryService service = new MandorHarvestHistoryService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of()));

        assertThatThrownBy(() -> service.getBuruhHarvests(mandorId, notAssigned))
                .isInstanceOf(MandorUnauthorizedAccessException.class);
    }

    @Test
    void testMandorGetBuruhHarvestsSucceedsWhenAuthorized() {
        UUID mandorId = UUID.randomUUID();
        UUID assigned = UUID.randomUUID();

        InMemoryMandorHarvestRepository repository = new InMemoryMandorHarvestRepository();
        repository.save(report(assigned, "Budi", LocalDate.of(2026, 5, 10)));
        MandorHarvestHistoryService service = new MandorHarvestHistoryService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(assigned)));

        var result = service.getBuruhHarvests(mandorId, assigned);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().buruhId()).isEqualTo(assigned);
    }

    private HarvestReport report(UUID buruhId, String buruhName, LocalDate date) {
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                buruhId,
                date,
                BigDecimal.valueOf(100),
                "Panen",
                List.of("https://photo"));
        report.setBuruhNameSnapshot(buruhName);
        return report;
    }
}
