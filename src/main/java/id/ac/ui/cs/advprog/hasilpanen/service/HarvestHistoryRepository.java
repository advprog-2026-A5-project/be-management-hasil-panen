package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.List;
import java.util.UUID;

public interface HarvestHistoryRepository {

    List<HarvestReport> findByBuruhId(UUID buruhId);

    default List<HarvestReport> findByBuruhId(Long buruhId) {
        return findByBuruhId(LegacyIdBridge.longToUuid(buruhId));
    }
}
