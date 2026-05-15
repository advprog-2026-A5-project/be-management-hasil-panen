package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.UUID;

public record RejectHarvestCommand(UUID harvestId, UUID mandorId, String reason) {
}
