package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;

final class TransportEligibilityMapper {

    private TransportEligibilityMapper() {
    }

    static TransportEligibilityResult toResult(HarvestReport report) {
        boolean eligible = report.getStatus() == HarvestStatus.APPROVED;
        return new TransportEligibilityResult(
                report.getHarvestId(),
                eligible,
                report.getStatus(),
                report.getKilogram());
    }
}
