package id.ac.ui.cs.advprog.hasilpanen.service;

import java.time.LocalDate;
import java.util.UUID;

public record MandorHarvestListQuery(Long mandorId, LocalDate harvestDate, String buruhName) {
    public MandorHarvestListQuery(UUID mandorId, LocalDate harvestDate, String buruhName) {
        this(LegacyIdBridge.uuidToLong(mandorId), harvestDate, buruhName);
    }
}
