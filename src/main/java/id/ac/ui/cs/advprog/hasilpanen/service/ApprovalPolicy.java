package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.util.UUID;

final class ApprovalPolicy {

    private ApprovalPolicy() {
    }

    static void ensurePending(HarvestReport report) {
        if (report.getStatus() != HarvestStatus.PENDING) {
            throw new HarvestTerminalStatusException("harvest already in terminal status");
        }
    }

    static String payrollPayload(HarvestReport report, UUID eventId) {
        String approvedAt = report.getApprovedAt() == null ? "" : report.getApprovedAt().toString();
        String kebunId = report.getKebunIdSnapshot() == null ? "null" : "\"" + report.getKebunIdSnapshot() + "\"";
        String idempotencyKey = "harvest-approved:" + report.getHarvestId();

        return "{"
                + "\"eventId\":\"" + eventId + "\","
                + "\"eventType\":\"harvest.approved.v1\","
                + "\"harvestId\":\"" + report.getHarvestId() + "\","
                + "\"buruhId\":" + report.getBuruhAuthId() + ","
                + "\"mandorId\":" + report.getApprovedBy() + ","
                + "\"kebunId\":" + kebunId + ","
                + "\"kebunCode\":\"" + report.getKebunCodeSnapshot() + "\","
                + "\"harvestDate\":\"" + report.getHarvestDate() + "\","
                + "\"kilogram\":" + report.getKilogram().toPlainString() + ","
                + "\"approvedAt\":\"" + approvedAt + "\","
                + "\"idempotencyKey\":\"" + idempotencyKey + "\""
                + "}";
    }
}
