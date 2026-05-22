package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class InMemoryMandorHarvestRepository implements MandorHarvestRepository {

    private final List<HarvestReport> store = new ArrayList<>();

    void save(HarvestReport report) {
        store.add(report);
    }

    @Override
    public List<HarvestReport> findAll() {
        return List.copyOf(store);
    }

    @Override
    public List<HarvestReport> findByBuruhId(UUID buruhId) {
        return store.stream()
                .filter(report -> report.getBuruhId().equals(buruhId))
                .toList();
    }

    @Override
    public List<HarvestReport> findAllByBuruhIds(Set<UUID> buruhIds) {
        return store.stream()
                .filter(report -> buruhIds.contains(report.getBuruhId()))
                .toList();
    }
}
