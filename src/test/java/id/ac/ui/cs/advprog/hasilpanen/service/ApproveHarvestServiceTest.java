package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ApproveHarvestServiceTest {

    @Test
    void testApproveHarvestSucceeds() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);
        InMemoryOutboxEventRepository outboxRepository = new InMemoryOutboxEventRepository();

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                outboxRepository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId));

        HarvestReport updated = approvalRepository.findById(report.getHarvestId()).orElseThrow();
        assertThat(updated.getStatus().name()).isEqualTo("APPROVED");
    }

    @Test
    void testApproveHarvestFailsIfNotFound() {
        ApproveHarvestService service = new ApproveHarvestService(
                new InMemoryApprovalRepository(),
                new InMemoryOutboxEventRepository(),
                new StubMandorBuruhClient(UUID.randomUUID(), List.of()),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.approve(new ApproveHarvestCommand(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(HarvestNotFoundException.class);
    }

    @Test
    void testApproveHarvestFailsIfUnauthorized() {
        UUID mandorId = UUID.randomUUID();
        HarvestReport report = report(UUID.randomUUID());

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                new InMemoryOutboxEventRepository(),
                new StubMandorBuruhClient(mandorId, List.of()),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId)))
                .isInstanceOf(MandorUnauthorizedAccessException.class);
    }

    @Test
    void testApproveHarvestFailsIfAlreadyApproved() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);
        report.approve(mandorId);

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                new InMemoryOutboxEventRepository(),
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId)))
                .isInstanceOf(HarvestTerminalStatusException.class);
    }

    @Test
    void testApproveHarvestFailsIfAlreadyRejected() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);
        report.reject(mandorId, "bad");

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                new InMemoryOutboxEventRepository(),
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId)))
                .isInstanceOf(HarvestTerminalStatusException.class);
    }

    @Test
    void testApproveHarvestCreatesPayrollOutboxEvent() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);
        InMemoryOutboxEventRepository outboxRepository = new InMemoryOutboxEventRepository();

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                outboxRepository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId));

        assertThat(outboxRepository.findAll()).hasSize(1);
        OutboxEvent event = outboxRepository.findAll().getFirst();
        assertThat(event.eventType()).isEqualTo("harvest.approved.v1");
        assertThat(event.payload()).contains("\"eventType\":\"harvest.approved.v1\"");
        assertThat(event.payload()).contains("\"harvestId\":\"" + report.getHarvestId() + "\"");
        assertThat(event.payload()).contains("\"buruhId\":" + report.getBuruhAuthId());
        assertThat(event.payload()).contains("\"mandorId\":" + mandorId);
        assertThat(event.payload()).contains("\"kebunCode\":\"KB001\"");
        assertThat(event.payload()).contains("\"kilogram\":120");
        assertThat(event.payload()).contains("\"idempotencyKey\":\"");
    }

    @Test
    void testApproveHarvestConcurrentApprovalCreatesOneEvent() throws InterruptedException {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);
        InMemoryOutboxEventRepository outboxRepository = new InMemoryOutboxEventRepository();

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                outboxRepository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(true));

        int threads = 10;
        var executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        var success = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await(2, TimeUnit.SECONDS);
                    service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId));
                    success.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertThat(success.get()).isEqualTo(1);
        assertThat(outboxRepository.findAll()).hasSize(1);
    }

    private HarvestReport report(UUID buruhId) {
        return HarvestReport.submit(
                UUID.randomUUID(),
                LegacyIdBridge.uuidToLong(buruhId),
                LegacyIdBridge.uuidToLong(UUID.randomUUID()),
                "KB001",
                null,
                LocalDate.of(2026, 5, 10),
                BigDecimal.valueOf(120),
                "Panen valid",
                List.of("https://photo"));
    }

    @Test
    void testApproveHarvestFailsIfMandorKebunMismatch() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                new InMemoryOutboxEventRepository(),
                new StubMandorBuruhClient(mandorId, List.of(buruhId)),
                new StubKebunClient(false));

        assertThatThrownBy(() -> service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId)))
                .isInstanceOf(MandorUnauthorizedAccessException.class);
    }

    @Test
    void testApproveHarvestFailsSafelyWhenAuthClientUnavailable() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        HarvestReport report = report(buruhId);

        InMemoryApprovalRepository approvalRepository = new InMemoryApprovalRepository();
        approvalRepository.save(report);

        ApproveHarvestService service = new ApproveHarvestService(
                approvalRepository,
                new InMemoryOutboxEventRepository(),
                m -> {
                    throw new RuntimeException("auth unavailable");
                },
                new StubKebunClient(true));

        assertThatThrownBy(() -> service.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("auth unavailable");
    }
}
