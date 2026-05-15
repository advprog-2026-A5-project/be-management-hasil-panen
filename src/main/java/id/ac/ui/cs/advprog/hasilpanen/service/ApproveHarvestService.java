package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.time.Instant;
import java.util.UUID;

public class ApproveHarvestService {

    private final ApprovalRepository approvalRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final MandorBuruhClient mandorBuruhClient;

    public ApproveHarvestService(
            ApprovalRepository approvalRepository,
            OutboxEventRepository outboxEventRepository,
            MandorBuruhClient mandorBuruhClient) {
        this.approvalRepository = approvalRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.mandorBuruhClient = mandorBuruhClient;
    }

    public synchronized void approve(ApproveHarvestCommand command) {
        HarvestReport report = HarvestTransitionGuard.loadPendingAuthorizedHarvest(
                approvalRepository, mandorBuruhClient, command.harvestId(), command.mandorId());

        report.approve(command.mandorId());
        approvalRepository.save(report);

        outboxEventRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                report.getHarvestId(),
                "HarvestReport",
                "PAYROLL_TRIGGERED",
                ApprovalPolicy.payrollPayload(report),
                "PENDING",
                Instant.now(),
                null,
                0));
    }
}
