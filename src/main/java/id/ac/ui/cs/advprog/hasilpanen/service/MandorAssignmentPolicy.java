package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.Set;
import java.util.UUID;

final class MandorAssignmentPolicy {

    private MandorAssignmentPolicy() {
    }

    static void ensureAssigned(UUID buruhId, Set<UUID> assignedBuruhIds) {
        if (!assignedBuruhIds.contains(buruhId)) {
            throw new MandorUnauthorizedAccessException("mandor unauthorized for this buruh");
        }
    }
}
