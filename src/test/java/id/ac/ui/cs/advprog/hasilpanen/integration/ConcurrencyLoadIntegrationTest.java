package id.ac.ui.cs.advprog.hasilpanen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestReportRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.ApprovalRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.ApproveHarvestCommand;
import id.ac.ui.cs.advprog.hasilpanen.service.ApproveHarvestService;
import id.ac.ui.cs.advprog.hasilpanen.service.CreateHarvestCommand;
import id.ac.ui.cs.advprog.hasilpanen.service.CreateHarvestService;
import id.ac.ui.cs.advprog.hasilpanen.service.DuplicateHarvestSubmissionException;
import id.ac.ui.cs.advprog.hasilpanen.service.GetMyHarvestHistoryService;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestHistoryRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.MyHarvestHistoryQuery;
import id.ac.ui.cs.advprog.hasilpanen.service.OutboxEvent;
import id.ac.ui.cs.advprog.hasilpanen.service.OutboxEventRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.RejectHarvestCommand;
import id.ac.ui.cs.advprog.hasilpanen.service.RejectHarvestService;
import id.ac.ui.cs.advprog.hasilpanen.service.StubMandorBuruhClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ConcurrencyLoadIntegrationTest {

    @Test
    void testConcurrentCreateHarvest50RequestsOnlyOneCreated() throws InterruptedException {
        UUID buruhId = UUID.randomUUID();
        UnifiedInMemoryRepository repository = new UnifiedInMemoryRepository();
        CreateHarvestService service = new CreateHarvestService(repository, () -> LocalDate.of(2026, 5, 15));

        int threads = 50;
        var pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        var success = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(2, TimeUnit.SECONDS);
                    service.createHarvest(new CreateHarvestCommand(
                            buruhId,
                            BigDecimal.valueOf(100),
                            "load",
                            List.of("https://photo")));
                    success.incrementAndGet();
                } catch (DuplicateHarvestSubmissionException ignored) {
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(success.get()).isEqualTo(1);
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void testConcurrentApproveHarvestOnlyOneApprovalWins() throws InterruptedException {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();

        UnifiedInMemoryRepository repository = new UnifiedInMemoryRepository();
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(), buruhId, LocalDate.of(2026, 5, 16), BigDecimal.TEN, "ok", List.of("https://p"));
        repository.save(report);
        InMemoryOutbox outbox = new InMemoryOutbox();

        ApproveHarvestService service = new ApproveHarvestService(
                repository,
                outbox,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)));

        int threads = 20;
        var pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        var success = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
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
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(success.get()).isEqualTo(1);
        assertThat(repository.findById(report.getHarvestId()).orElseThrow().getStatus()).isEqualTo(HarvestStatus.APPROVED);
        assertThat(outbox.findAll()).hasSize(1);
    }

    @Test
    void testConcurrentApproveRejectOnlyOneTerminalStatusWins() throws InterruptedException {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();

        UnifiedInMemoryRepository repository = new UnifiedInMemoryRepository();
        HarvestReport report = HarvestReport.submit(
                UUID.randomUUID(), buruhId, LocalDate.of(2026, 5, 17), BigDecimal.TEN, "ok", List.of("https://p"));
        repository.save(report);

        ApproveHarvestService approveService = new ApproveHarvestService(
                repository,
                new InMemoryOutbox(),
                new StubMandorBuruhClient(mandorId, List.of(buruhId)));
        RejectHarvestService rejectService = new RejectHarvestService(
                repository,
                new StubMandorBuruhClient(mandorId, List.of(buruhId)));

        var pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        pool.submit(() -> {
            ready.countDown();
            try {
                start.await(2, TimeUnit.SECONDS);
                approveService.approve(new ApproveHarvestCommand(report.getHarvestId(), mandorId));
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });

        pool.submit(() -> {
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
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        HarvestStatus status = repository.findById(report.getHarvestId()).orElseThrow().getStatus();
        assertThat(status == HarvestStatus.APPROVED || status == HarvestStatus.REJECTED).isTrue();
    }

    @Test
    void testHarvestHistoryFilteringWithMultipleStatuses() {
        UUID buruhId = UUID.randomUUID();
        UnifiedInMemoryRepository repository = new UnifiedInMemoryRepository();

        HarvestReport pending = HarvestReport.submit(
                UUID.randomUUID(), buruhId, LocalDate.of(2026, 5, 1), BigDecimal.ONE, "p", List.of("https://a"));
        HarvestReport approved = HarvestReport.submit(
                UUID.randomUUID(), buruhId, LocalDate.of(2026, 5, 2), BigDecimal.ONE, "a", List.of("https://a"));
        approved.approve(UUID.randomUUID());
        HarvestReport rejected = HarvestReport.submit(
                UUID.randomUUID(), buruhId, LocalDate.of(2026, 5, 3), BigDecimal.ONE, "r", List.of("https://a"));
        rejected.reject(UUID.randomUUID(), "invalid");

        repository.save(pending);
        repository.save(approved);
        repository.save(rejected);

        GetMyHarvestHistoryService service = new GetMyHarvestHistoryService(repository);

        var pendingResult = service.getMyHistory(new MyHarvestHistoryQuery(buruhId, null, null, HarvestStatus.PENDING, 0, 10));
        var approvedResult = service.getMyHistory(new MyHarvestHistoryQuery(buruhId, null, null, HarvestStatus.APPROVED, 0, 10));
        var rejectedResult = service.getMyHistory(new MyHarvestHistoryQuery(buruhId, null, null, HarvestStatus.REJECTED, 0, 10));

        assertThat(pendingResult.items()).hasSize(1);
        assertThat(approvedResult.items()).hasSize(1);
        assertThat(rejectedResult.items()).hasSize(1);
        assertThat(rejectedResult.items().getFirst().rejectionReason()).isEqualTo("invalid");
    }

    static class UnifiedInMemoryRepository implements HarvestReportRepository, ApprovalRepository, HarvestHistoryRepository {
        private final Map<UUID, HarvestReport> store = new ConcurrentHashMap<>();

        @Override
        public Optional<HarvestReport> findByBuruhIdAndHarvestDate(UUID buruhId, LocalDate harvestDate) {
            return store.values().stream()
                    .filter(r -> r.getBuruhId().equals(buruhId) && r.getHarvestDate().equals(harvestDate))
                    .findFirst();
        }

        @Override
        public HarvestReport save(HarvestReport report) {
            store.putIfAbsent(report.getHarvestId(), report);
            store.put(report.getHarvestId(), report);
            return report;
        }

        @Override
        public Optional<HarvestReport> findById(UUID harvestId) {
            return Optional.ofNullable(store.get(harvestId));
        }

        @Override
        public List<HarvestReport> findByBuruhId(UUID buruhId) {
            return store.values().stream().filter(r -> r.getBuruhId().equals(buruhId)).toList();
        }

        int size() {
            return store.size();
        }
    }

    static class InMemoryOutbox implements OutboxEventRepository {
        private final List<OutboxEvent> events = new ArrayList<>();

        @Override
        public synchronized void save(OutboxEvent event) {
            events.add(event);
        }

        @Override
        public synchronized List<OutboxEvent> findAll() {
            return List.copyOf(events);
        }
    }
}
