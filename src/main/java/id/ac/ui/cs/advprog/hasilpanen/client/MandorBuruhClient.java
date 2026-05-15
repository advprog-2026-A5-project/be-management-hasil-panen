package id.ac.ui.cs.advprog.hasilpanen.client;

import java.util.Set;
import java.util.UUID;

public interface MandorBuruhClient {

    Set<UUID> getAssignedBuruhIds(UUID mandorId);
}
