package id.ac.ui.cs.advprog.hasilpanen.client;

import java.util.UUID;

public interface KebunClient {

    boolean hasFarmAccess(UUID mandorId, UUID farmId);
}
