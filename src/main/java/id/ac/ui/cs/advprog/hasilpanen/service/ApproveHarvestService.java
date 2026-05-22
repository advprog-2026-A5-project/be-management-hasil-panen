package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.KebunClient;
import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ApproveHarvestService {

    private final ApprovalRepository approvalRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final MandorBuruhClient mandorBuruhClient;
    private final KebunClient kebunClient;

    @Autowired
    public ApproveHarvestService(
            ApprovalRepository approvalRepository,
            OutboxEventRepository outboxEventRepository,
            MandorBuruhClient mandorBuruhClient,
            KebunClient kebunClient) {
        this.approvalRepository = approvalRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.mandorBuruhClient = mandorBuruhClient;
        this.kebunClient = kebunClient;
    }

    public ApproveHarvestService(
            ApprovalRepository approvalRepository,
            OutboxEventRepository outboxEventRepository,
            MandorBuruhClient mandorBuruhClient) {
        this(approvalRepository, outboxEventRepository, mandorBuruhClient, (mandorId, kebunCode) -> true);
    }

    public synchronized void approve(ApproveHarvestCommand command) {
        HarvestReport report = HarvestTransitionGuard.loadPendingAuthorizedHarvest(
                approvalRepository, mandorBuruhClient, command.harvestId(), command.mandorId());
        if (!kebunClient.hasFarmAccess(command.mandorId(), report.getKebunCodeSnapshot())) {
            throw new MandorUnauthorizedAccessException("mandor unauthorized for this kebun");
        }

        report.approve(command.mandorId());
        approvalRepository.save(report);

        UUID eventId = UUID.randomUUID();
        outboxEventRepository.save(new OutboxEvent(
                eventId,
                report.getHarvestId(),
                "HarvestReport",
                "harvest.approved.v1",
                ApprovalPolicy.payrollPayload(report, eventId),
                "PENDING",
                Instant.now(),
                null,
                0));
    }
}
