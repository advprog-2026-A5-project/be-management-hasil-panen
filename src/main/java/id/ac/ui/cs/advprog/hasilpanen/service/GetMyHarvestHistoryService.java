package id.ac.ui.cs.advprog.hasilpanen.service;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class GetMyHarvestHistoryService {

    private final HarvestHistoryRepository repository;

    public GetMyHarvestHistoryService(HarvestHistoryRepository repository) {
        this.repository = repository;
    }

    public MyHarvestHistoryResult getMyHistory(MyHarvestHistoryQuery query) {
        List<id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport> filtered = repository.findByBuruhId(query.buruhId()).stream()
                .filter(report -> HarvestHistoryFilter.matches(report, query))
                .sorted(Comparator.comparing(id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport::getHarvestDate).reversed())
                .toList();

        int fromIndex = Math.min(query.page() * query.size(), filtered.size());
        int toIndex = Math.min(fromIndex + query.size(), filtered.size());

        List<MyHarvestHistoryResult.Item> items = filtered.subList(fromIndex, toIndex).stream()
                .map(report -> new MyHarvestHistoryResult.Item(
                        report.getHarvestId(),
                        report.getHarvestDate(),
                        report.getStatus(),
                        report.getRejectionReason()))
                .toList();

        int totalPages = query.size() == 0 ? 0 : (int) Math.ceil((double) filtered.size() / query.size());
        return new MyHarvestHistoryResult(items, filtered.size(), totalPages);
    }
}
