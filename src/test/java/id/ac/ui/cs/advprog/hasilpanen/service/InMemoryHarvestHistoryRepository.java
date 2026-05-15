package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class InMemoryHarvestHistoryRepository implements HarvestHistoryRepository {

    private final List<HarvestReport> store = new ArrayList<>();

    void save(HarvestReport report) {
        store.add(report);
    }

    @Override
    public List<HarvestReport> findByBuruhId(UUID buruhId) {
        return store.stream().filter(report -> report.getBuruhId().equals(buruhId)).toList();
    }
}
