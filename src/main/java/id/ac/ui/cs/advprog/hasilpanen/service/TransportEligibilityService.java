package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransportEligibilityService {

    private final ApprovalRepository repository;

    public TransportEligibilityService(ApprovalRepository repository) {
        this.repository = repository;
    }

    public TransportEligibilityResult getEligibility(UUID harvestId) {
        HarvestReport report = repository.findById(harvestId)
                .orElseThrow(() -> new HarvestNotFoundException("harvest not found"));

        boolean eligible = report.getStatus() == HarvestStatus.APPROVED;
        return new TransportEligibilityResult(
                report.getHarvestId(),
                eligible,
                report.getStatus(),
                report.getKilogram());
    }
}
