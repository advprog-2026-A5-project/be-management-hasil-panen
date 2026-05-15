package id.ac.ui.cs.advprog.hasilpanen.repository;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.service.ApprovalRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryApprovalRepositoryAdapter implements ApprovalRepository {

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
