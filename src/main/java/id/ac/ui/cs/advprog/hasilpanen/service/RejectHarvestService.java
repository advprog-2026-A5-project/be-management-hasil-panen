package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.Set;
import java.util.UUID;

public class RejectHarvestService {

    private final ApprovalRepository repository;
    private final MandorBuruhClient mandorBuruhClient;

    public RejectHarvestService(ApprovalRepository repository, MandorBuruhClient mandorBuruhClient) {
        this.repository = repository;
        this.mandorBuruhClient = mandorBuruhClient;
    }

    public synchronized void reject(RejectHarvestCommand command) {
        HarvestReport report = repository.findById(command.harvestId())
                .orElseThrow(() -> new HarvestNotFoundException("harvest not found"));

        Set<UUID> assigned = mandorBuruhClient.getAssignedBuruhIds(command.mandorId());
        MandorAssignmentPolicy.ensureAssigned(report.getBuruhId(), assigned);

        ApprovalPolicy.ensurePending(report);
        report.reject(command.mandorId(), command.reason());
        repository.save(report);
    }
}
