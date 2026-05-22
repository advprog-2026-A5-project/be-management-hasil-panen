package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetMyHarvestHistoryServiceTest {

    @Test
    void testGetMyHarvestHistoryReturnsOnlyCurrentBuruhReports() {
        UUID me = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        InMemoryHarvestHistoryRepository repository = new InMemoryHarvestHistoryRepository();
        repository.save(report(me, LocalDate.of(2026, 5, 10), HarvestStatus.PENDING));
        repository.save(report(other, LocalDate.of(2026, 5, 10), HarvestStatus.PENDING));

        GetMyHarvestHistoryService service = new GetMyHarvestHistoryService(repository);
        var result = service.getMyHistory(new MyHarvestHistoryQuery(me, null, null, null, 0, 10));

        assertThat(result.items()).hasSize(1);
    }

    @Test
    void testGetMyHarvestHistoryFiltersByDate() {
        UUID me = UUID.randomUUID();
        InMemoryHarvestHistoryRepository repository = new InMemoryHarvestHistoryRepository();
        repository.save(report(me, LocalDate.of(2026, 5, 1), HarvestStatus.PENDING));
        repository.save(report(me, LocalDate.of(2026, 5, 20), HarvestStatus.PENDING));

        GetMyHarvestHistoryService service = new GetMyHarvestHistoryService(repository);
        var result = service.getMyHistory(new MyHarvestHistoryQuery(
                me, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 31), null, 0, 10));

        assertThat(result.items()).hasSize(1);
    }

    @Test
    void testGetMyHarvestHistoryFiltersByStatus() {
        UUID me = UUID.randomUUID();
        InMemoryHarvestHistoryRepository repository = new InMemoryHarvestHistoryRepository();
        repository.save(report(me, LocalDate.of(2026, 5, 10), HarvestStatus.PENDING));
        repository.save(report(me, LocalDate.of(2026, 5, 11), HarvestStatus.REJECTED));

        GetMyHarvestHistoryService service = new GetMyHarvestHistoryService(repository);
        var result = service.getMyHistory(new MyHarvestHistoryQuery(me, null, null, HarvestStatus.REJECTED, 0, 10));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().status()).isEqualTo(HarvestStatus.REJECTED);
    }

    @Test
    void testGetMyHarvestHistoryIncludesRejectionReason() {
        UUID me = UUID.randomUUID();
        InMemoryHarvestHistoryRepository repository = new InMemoryHarvestHistoryRepository();
        HarvestReport rejected = report(me, LocalDate.of(2026, 5, 11), HarvestStatus.PENDING);
        rejected.reject(UUID.randomUUID(), "Foto tidak valid");
        repository.save(rejected);

        GetMyHarvestHistoryService service = new GetMyHarvestHistoryService(repository);
        var result = service.getMyHistory(new MyHarvestHistoryQuery(me, null, null, null, 0, 10));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().rejectionReason()).isEqualTo("Foto tidak valid");
    }

    @Test
    void testGetMyHarvestHistoryPaginatesResults() {
        UUID me = UUID.randomUUID();
        InMemoryHarvestHistoryRepository repository = new InMemoryHarvestHistoryRepository();
        for (int i = 1; i <= 5; i++) {
            repository.save(report(me, LocalDate.of(2026, 5, i), HarvestStatus.PENDING));
        }

        GetMyHarvestHistoryService service = new GetMyHarvestHistoryService(repository);
        var page1 = service.getMyHistory(new MyHarvestHistoryQuery(me, null, null, null, 0, 2));
        var page2 = service.getMyHistory(new MyHarvestHistoryQuery(me, null, null, null, 1, 2));

        assertThat(page1.items()).hasSize(2);
        assertThat(page2.items()).hasSize(2);
        assertThat(page1.totalItems()).isEqualTo(5);
        assertThat(page1.totalPages()).isEqualTo(3);
    }

    private HarvestReport report(UUID buruhId, LocalDate date, HarvestStatus status) {
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(),
                buruhId,
                date,
                BigDecimal.valueOf(100),
                "Panen",
                List.of("https://photo"));
        if (status == HarvestStatus.REJECTED) {
            report.reject(UUID.randomUUID(), "Rejected");
        }
        return report;
    }
}
