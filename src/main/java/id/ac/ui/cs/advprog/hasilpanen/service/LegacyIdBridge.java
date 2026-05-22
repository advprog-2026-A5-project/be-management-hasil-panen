package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LegacyIdBridge {

    private static final ConcurrentMap<UUID, Long> UUID_TO_LONG = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, UUID> LONG_TO_UUID = new ConcurrentHashMap<>();

    private LegacyIdBridge() {
    }

    public static Long uuidToLong(UUID id) {
        if (id == null) {
            return null;
        }
        return UUID_TO_LONG.computeIfAbsent(id, key -> {
            long converted = Math.abs(key.getLeastSignificantBits());
            LONG_TO_UUID.putIfAbsent(converted, key);
            return converted;
        });
    }

    public static UUID longToUuid(Long id) {
        if (id == null) {
            return null;
        }
        return LONG_TO_UUID.getOrDefault(id, new UUID(0L, id));
    }
}
