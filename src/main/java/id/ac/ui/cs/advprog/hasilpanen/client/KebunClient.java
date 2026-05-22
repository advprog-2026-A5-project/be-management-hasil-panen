package id.ac.ui.cs.advprog.hasilpanen.client;

public interface KebunClient {

    boolean hasFarmAccess(Long mandorId, String kebunCode);

    default boolean hasFarmAccess(Long mandorId, String kebunCode, String bearerToken) {
        return hasFarmAccess(mandorId, kebunCode);
    }

    default boolean kebunExistsByCode(String kebunCode, String bearerToken) {
        return kebunCode != null && !kebunCode.isBlank();
    }
}
