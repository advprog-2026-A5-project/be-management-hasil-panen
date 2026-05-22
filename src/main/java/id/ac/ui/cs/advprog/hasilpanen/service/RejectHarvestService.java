package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.KebunClient;
import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RejectHarvestService {

    private final ApprovalRepository repository;
    private final MandorBuruhClient mandorBuruhClient;
    private final KebunClient kebunClient;

    @Autowired
    public RejectHarvestService(
            ApprovalRepository repository,
            MandorBuruhClient mandorBuruhClient,
            KebunClient kebunClient) {
        this.repository = repository;
        this.mandorBuruhClient = mandorBuruhClient;
        this.kebunClient = kebunClient;
    }

    public RejectHarvestService(ApprovalRepository repository, MandorBuruhClient mandorBuruhClient) {
        this(repository, mandorBuruhClient, (mandorId, kebunCode) -> true);
    }

    public synchronized void reject(RejectHarvestCommand command) {
        HarvestReport report = HarvestTransitionGuard.loadPendingAuthorizedHarvest(
                repository, mandorBuruhClient, command.harvestId(), command.mandorId());
        if (!kebunClient.hasFarmAccess(command.mandorId(), report.getKebunCodeSnapshot())) {
            throw new MandorUnauthorizedAccessException("mandor unauthorized for this kebun");
        }
        report.reject(command.mandorId(), command.reason());
        repository.save(report);
    }
}
