package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.time.Instant;
import java.util.Set;
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
        HarvestReport report = approvalRepository.findById(command.harvestId())
                .orElseThrow(() -> new HarvestNotFoundException("harvest not found"));

        Set<UUID> assigned = mandorBuruhClient.getAssignedBuruhIds(command.mandorId());
        MandorAssignmentPolicy.ensureAssigned(report.getBuruhId(), assigned);

        ApprovalPolicy.ensurePending(report);

        report.approve(command.mandorId());
        approvalRepository.save(report);

        outboxEventRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                report.getHarvestId(),
                "HarvestReport",
                "PAYROLL_TRIGGERED",
                ApprovalPolicy.payrollPayload(report),
                "PENDING",
                Instant.now()));
    }
}
