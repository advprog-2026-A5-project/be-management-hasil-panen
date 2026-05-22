package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestReportRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CreateHarvestServiceTest {

    @Test
    void testCreateHarvestSuccessForValidBuruhSubmission() {
        HarvestReportRepository repository = new InMemoryHarvestReportRepository();
        CreateHarvestService service = new CreateHarvestService(repository, () -> LocalDate.of(2026, 5, 15));

        HarvestReport report = service.createHarvest(new CreateHarvestCommand(
                2L,
                3L,
                "KB001",
                null,
                BigDecimal.valueOf(125.5),
                "Panen blok A berjalan lancar.",
                List.of("https://storage.example.com/photo-1.jpg")));

        assertThat(report).isNotNull();
        assertThat(report.getMandorIdSnapshot()).isEqualTo(3L);
        assertThat(report.getKebunCodeSnapshot()).isEqualTo("KB001");
    }

    @Test
    void testCreateHarvestFailsWhenKilogramInvalid() {
        HarvestReportRepository repository = new InMemoryHarvestReportRepository();
        CreateHarvestService service = new CreateHarvestService(repository, LocalDate::now);

        assertThatThrownBy(() -> service.createHarvest(new CreateHarvestCommand(
                UUID.randomUUID(), BigDecimal.ZERO, "ok", List.of("https://a"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreateHarvestFailsWhenReportTextBlank() {
        HarvestReportRepository repository = new InMemoryHarvestReportRepository();
        CreateHarvestService service = new CreateHarvestService(repository, LocalDate::now);

        assertThatThrownBy(() -> service.createHarvest(new CreateHarvestCommand(
                UUID.randomUUID(), BigDecimal.ONE, " ", List.of("https://a"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreateHarvestFailsWhenNoPhotosProvided() {
        HarvestReportRepository repository = new InMemoryHarvestReportRepository();
        CreateHarvestService service = new CreateHarvestService(repository, LocalDate::now);

        assertThatThrownBy(() -> service.createHarvest(new CreateHarvestCommand(
                UUID.randomUUID(), BigDecimal.ONE, "ok", List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreateHarvestFailsWhenAlreadySubmittedToday() {
        UUID buruhId = UUID.randomUUID();
        HarvestReportRepository repository = new InMemoryHarvestReportRepository();
        CreateHarvestService service = new CreateHarvestService(repository, () -> LocalDate.of(2026, 5, 15));

        service.createHarvest(new CreateHarvestCommand(buruhId, BigDecimal.ONE, "one", List.of("https://a")));

        assertThatThrownBy(() -> service.createHarvest(new CreateHarvestCommand(
                buruhId, BigDecimal.TEN, "two", List.of("https://b"))))
                .isInstanceOf(DuplicateHarvestSubmissionException.class);
    }

    @Test
    void testCreateHarvestConcurrentSameDayCreatesOnlyOneReport() throws InterruptedException {
        UUID buruhId = UUID.randomUUID();
        HarvestReportRepository repository = new InMemoryHarvestReportRepository();
        CreateHarvestService service = new CreateHarvestService(repository, () -> LocalDate.of(2026, 5, 15));

        int threads = 10;
        var pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        var successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        var conflictCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(2, TimeUnit.SECONDS);
                    service.createHarvest(new CreateHarvestCommand(
                            buruhId, BigDecimal.valueOf(100), "same day", List.of("https://a")));
                    successCount.incrementAndGet();
                } catch (DuplicateHarvestSubmissionException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threads - 1);
    }
}
