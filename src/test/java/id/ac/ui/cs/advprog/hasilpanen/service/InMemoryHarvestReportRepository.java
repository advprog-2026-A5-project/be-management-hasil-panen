package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestReportRepository;
import java.lang.reflect.Field;
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
        store.putIfAbsent(key(readBuruhId(report), readHarvestDate(report)), report);
        return report;
    }

    private String key(UUID buruhId, LocalDate date) {
        return buruhId + "|" + date;
    }

    private UUID readBuruhId(HarvestReport report) {
        return readField(report, "buruhId", UUID.class);
    }

    private LocalDate readHarvestDate(HarvestReport report) {
        return readField(report, "harvestDate", LocalDate.class);
    }

    private <T> T readField(HarvestReport report, String fieldName, Class<T> type) {
        try {
            Field field = HarvestReport.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(report));
        } catch (Exception e) {
            throw new IllegalStateException("cannot read field: " + fieldName, e);
        }
    }
}
