package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;

final class ApprovalPolicy {

    private ApprovalPolicy() {
    }

    static void ensurePending(HarvestReport report) {
        if (report.getStatus() != HarvestStatus.PENDING) {
            throw new HarvestTerminalStatusException("harvest already in terminal status");
        }
    }

    static String payrollPayload(HarvestReport report) {
        return "{\"harvestId\":\"" + report.getHarvestId() + "\"}";
    }
}
