package id.ac.ui.cs.advprog.hasilpanen.client;

import java.util.UUID;

public interface UserAssignmentClient {

    boolean isAssigned(UUID supervisorId, UUID workerId);
}
