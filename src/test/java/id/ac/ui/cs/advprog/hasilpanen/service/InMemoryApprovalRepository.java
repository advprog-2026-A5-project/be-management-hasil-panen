package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryApprovalRepository implements ApprovalRepository {

    private final Map<UUID, HarvestReport> store = new ConcurrentHashMap<>();

    @Override
    public Optional<HarvestReport> findById(UUID harvestId) {
        return Optional.ofNullable(store.get(harvestId));
    }

    @Override
    public HarvestReport save(HarvestReport report) {
        store.put(report.getHarvestId(), report);
        return report;
    }
}
