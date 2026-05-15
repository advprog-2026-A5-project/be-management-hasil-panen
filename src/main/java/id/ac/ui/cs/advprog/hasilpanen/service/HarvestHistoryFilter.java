package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;

final class HarvestHistoryFilter {

    private HarvestHistoryFilter() {
    }

    static boolean matches(HarvestReport report, MyHarvestHistoryQuery query) {
        if (query.startDate() != null && report.getHarvestDate().isBefore(query.startDate())) {
            return false;
        }
        if (query.endDate() != null && report.getHarvestDate().isAfter(query.endDate())) {
            return false;
        }
        return query.status() == null || report.getStatus() == query.status();
    }
}
