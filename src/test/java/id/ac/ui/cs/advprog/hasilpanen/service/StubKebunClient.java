package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.KebunClient;

class StubKebunClient implements KebunClient {

    private final boolean farmAccess;

    StubKebunClient(boolean farmAccess) {
        this.farmAccess = farmAccess;
    }

    @Override
    public boolean hasFarmAccess(Long mandorId, String kebunCode) {
        return farmAccess;
    }
}
