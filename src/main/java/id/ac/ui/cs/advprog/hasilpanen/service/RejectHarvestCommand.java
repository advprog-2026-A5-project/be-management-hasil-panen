package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.UUID;

public record RejectHarvestCommand(UUID harvestId, Long mandorId, String reason) {
    public RejectHarvestCommand(UUID harvestId, UUID mandorId, String reason) {
        this(harvestId, LegacyIdBridge.uuidToLong(mandorId), reason);
    }
}
