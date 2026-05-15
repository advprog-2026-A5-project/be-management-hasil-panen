package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.Set;
import java.util.UUID;

final class HarvestTransitionGuard {

    private HarvestTransitionGuard() {
    }

    static HarvestReport loadPendingAuthorizedHarvest(
            ApprovalRepository repository,
            MandorBuruhClient mandorBuruhClient,
            UUID harvestId,
            UUID mandorId) {
        HarvestReport report = repository.findById(harvestId)
                .orElseThrow(() -> new HarvestNotFoundException("harvest not found"));
        Set<UUID> assigned = mandorBuruhClient.getAssignedBuruhIds(mandorId);
        MandorAssignmentPolicy.ensureAssigned(report.getBuruhId(), assigned);
        ApprovalPolicy.ensurePending(report);
        return report;
    }
}
