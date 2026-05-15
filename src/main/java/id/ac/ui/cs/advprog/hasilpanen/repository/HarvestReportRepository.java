package id.ac.ui.cs.advprog.hasilpanen.repository;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HarvestReportRepository {

    Optional<HarvestReport> findByBuruhIdAndHarvestDate(UUID buruhId, LocalDate harvestDate);

    HarvestReport save(HarvestReport report);
}
