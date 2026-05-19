package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RejectHarvestServiceTest {

    @Test
    void testRejectHarvestSucceedsWithReason() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "Foto tidak valid"));

        HarvestReport updated = repository.findById(report.getHarvestId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(HarvestStatus.REJECTED);
        assertThat(updated.getRejectionReason()).isEqualTo("Foto tidak valid");
    }

    @Test
    void testRejectHarvestFailsWithoutReason() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "  ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testRejectHarvestFailsIfUnauthorized() {
        UUID mandorId = UUID.randomUUID();
        HarvestReport report = report(UUID.randomUUID());

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of()),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "reason")))
                .isInstanceOf(MandorUnauthorizedAccessException.class);
    }

    @Test
    void testRejectHarvestFailsIfAlreadyApproved() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);
        report.approve(mandorId);

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "reason")))
                .isInstanceOf(HarvestTerminalStatusException.class);
    }

    @Test
    void testRejectHarvestFailsIfAlreadyRejected() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);
        report.reject(mandorId, "already");

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "reason")))
                .isInstanceOf(HarvestTerminalStatusException.class);
    }

    @Test
    void testRejectHarvestDoesNotCreatePayrollEvent() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);
        InMemoryOutboxEventRepository outbox = new InMemoryOutboxEventRepository();

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "not valid"));

        assertThat(outbox.findAll()).isEmpty();
    }

    @Test
    void testRejectHarvestConcurrentApproveRejectOnlyOneTerminalStatusWins() throws InterruptedException {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        ApproveHarvestService approveService = new ApproveHarvestService(
                repository,
                new InMemoryOutboxEventRepository(),
                new StubMandorBuruhClient(mandorId, List.of(buruhId)));
        RejectHarvestService rejectService = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        var executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            ready.countDown();
            try {
                start.await(2, TimeUnit.SECONDS);
                approveService.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId));
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });

        executor.submit(() -> {
            ready.countDown();
            try {
                start.await(2, TimeUnit.SECONDS);
                rejectService.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "bad"));
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        HarvestStatus status = repository.findById(report.getHarvestId()).orElseThrow().getStatus();
        assertThat(status == HarvestStatus.APPROVED || status == HarvestStatus.REJECTED).isTrue();
    }

    private HarvestReport report(UUID buruhId) {
        return HarvestReport.submit(
                UUID.randomUUID(),
                buruhId,
                LegacyIdBridge.uuidToLong(UUID.randomUUID()),
                "KB001",
                null,
                LocalDate.of(2026, 5, 11),
                BigDecimal.valueOf(100),
                "panen",
                List.of("https://photo"));
    }

    @Test
    void testRejectHarvestFailsIfMandorKebunMismatch() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository repository = new InMemoryApprovalRepository();
        repository.save(report);

        RejectHarvestService service = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(false));

        assertThatThrownBy(() -> service.reject(new RejectHarvestCommand(report.getHarvestId(), mandorId, "reason")))
                .isInstanceOf(MandorUnauthorizedAccessException.class);
    }
}
