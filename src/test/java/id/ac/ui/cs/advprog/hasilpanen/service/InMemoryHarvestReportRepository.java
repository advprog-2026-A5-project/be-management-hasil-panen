package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestReportRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryHarvestReportRepository implements HarvestReportRepository {

    private final Map<String, HarvestReport> store = new ConcurrentHashMap<>();

    @Override
    public Optional<HarvestReport> findByBuruhIdAndHarvestDate(UUID buruhId, LocalDate harvestDate) {
        return Optional.ofNullable(store.get(key(buruhId, harvestDate)));
    }

    @Override
    public HarvestReport save(HarvestReport report) {
        store.putIfAbsent(key(report.getBuruhId(), report.getHarvestDate()), report);
        return report;
    }

    private String key(UUID buruhId, LocalDate date) {
        return buruhId + "|" + date;
    }

}
