package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MandorHarvestRepository {

    List<HarvestReport> findAll();

    List<HarvestReport> findByBuruhId(UUID buruhId);

    default List<HarvestReport> findAllByBuruhIds(Set<UUID> buruhIds) {
        return findAll().stream().filter(report -> buruhIds.contains(report.getBuruhId())).toList();
    }
}
