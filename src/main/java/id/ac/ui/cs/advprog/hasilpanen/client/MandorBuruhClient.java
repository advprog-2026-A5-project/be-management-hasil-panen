package id.ac.ui.cs.advprog.hasilpanen.client;

import java.util.Set;
import java.util.UUID;

public interface MandorBuruhClient {

    Set<UUID> getAssignedBuruhIds(UUID mandorId);

    default Set<Long> getAssignedBuruhIds(Long mandorId) {
        return getAssignedBuruhIds(id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.longToUuid(mandorId))
                .stream()
                .map(id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge::uuidToLong)
                .collect(java.util.stream.Collectors.toSet());
    }
}
