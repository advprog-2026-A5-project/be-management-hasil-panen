package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.Set;

final class MandorAssignmentPolicy {

    private MandorAssignmentPolicy() {
    }

    static void ensureAssigned(Long buruhId, Set<Long> assignedBuruhIds) {
        if (!assignedBuruhIds.contains(buruhId)) {
            throw new MandorUnauthorizedAccessException("mandor unauthorized for this buruh");
        }
    }
}
