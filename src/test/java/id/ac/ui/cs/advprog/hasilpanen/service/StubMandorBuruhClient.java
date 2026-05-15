package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.MandorBuruhClient;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class StubMandorBuruhClient implements MandorBuruhClient {

    private final UUID mandorId;
    private final Set<UUID> assigned;

    StubMandorBuruhClient(UUID mandorId, List<UUID> assigned) {
        this.mandorId = mandorId;
        this.assigned = new HashSet<>(assigned);
    }

    @Override
    public Set<UUID> getAssignedBuruhIds(UUID mandorId) {
        if (!this.mandorId.equals(mandorId)) {
            return Set.of();
        }
        return Set.copyOf(assigned);
    }
}
