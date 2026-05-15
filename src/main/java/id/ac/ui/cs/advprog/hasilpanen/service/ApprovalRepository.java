package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRepository {

    Optional<HarvestReport> findById(UUID harvestId);

    HarvestReport save(HarvestReport report);
}
