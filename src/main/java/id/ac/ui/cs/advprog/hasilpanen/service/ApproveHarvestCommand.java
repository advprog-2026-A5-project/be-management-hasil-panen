package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.UUID;

public record ApproveHarvestCommand(UUID harvestId, Long mandorId) {
    public ApproveHarvestCommand(UUID harvestId, UUID mandorId) {
        this(harvestId, LegacyIdBridge.uuidToLong(mandorId));
    }
}
