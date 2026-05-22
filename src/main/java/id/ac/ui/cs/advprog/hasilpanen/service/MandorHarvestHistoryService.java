package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MandorHarvestHistoryService {

    private final MandorHarvestRepository repository;
    private final MandorBuruhClient mandorBuruhClient;

    public MandorHarvestHistoryService(MandorHarvestRepository repository, MandorBuruhClient mandorBuruhClient) {
        this.repository = repository;
        this.mandorBuruhClient = mandorBuruhClient;
    }

    public List<MandorHarvestView> listAssignedHarvests(MandorHarvestListQuery query) {
        Set<Long> assigned = mandorBuruhClient.getAssignedBuruhIds(query.mandorId());
        return repository.findAllByBuruhIdsLong(assigned).stream()
                .filter(report -> query.harvestDate() == null || report.getHarvestDate().equals(query.harvestDate()))
                .filter(report -> query.buruhName() == null
                        || report.getBuruhNameSnapshot().toLowerCase(Locale.ROOT)
                        .contains(query.buruhName().toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(HarvestReport::getHarvestDate).reversed())
                .map(this::toView)
                .toList();
    }

    public List<MandorHarvestView> getBuruhHarvests(Long mandorId, Long buruhId) {
        Set<Long> assigned = mandorBuruhClient.getAssignedBuruhIds(mandorId);
        MandorAssignmentPolicy.ensureAssigned(buruhId, assigned);

        return repository.findByBuruhId(buruhId).stream()
                .sorted(Comparator.comparing(HarvestReport::getHarvestDate).reversed())
                .map(this::toView)
                .toList();
    }

    public List<MandorHarvestView> getBuruhHarvests(UUID mandorId, UUID buruhId) {
        return getBuruhHarvests(LegacyIdBridge.uuidToLong(mandorId), LegacyIdBridge.uuidToLong(buruhId));
    }

    private MandorHarvestView toView(HarvestReport report) {
        return new MandorHarvestView(
                report.getHarvestId(),
                report.getBuruhId(),
                report.getBuruhNameSnapshot(),
                report.getHarvestDate(),
                report.getStatus(),
                report.getRejectionReason());
    }
}
