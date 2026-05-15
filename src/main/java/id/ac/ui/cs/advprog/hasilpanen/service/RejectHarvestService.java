package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;

public class RejectHarvestService {

    private final ApprovalRepository repository;
    private final MandorBuruhClient mandorBuruhClient;

    public RejectHarvestService(ApprovalRepository repository, MandorBuruhClient mandorBuruhClient) {
        this.repository = repository;
        this.mandorBuruhClient = mandorBuruhClient;
    }

    public synchronized void reject(RejectHarvestCommand command) {
        HarvestReport report = HarvestTransitionGuard.loadPendingAuthorizedHarvest(
                repository, mandorBuruhClient, command.harvestId(), command.mandorId());
        report.reject(command.mandorId(), command.reason());
        repository.save(report);
    }
}
