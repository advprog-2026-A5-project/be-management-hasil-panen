package id.ac.ui.cs.advprog.hasilpanen.storage;

public record StoredFile(
        String url,
        String key,
        String originalFilename,
        String contentType,
        long size) {
}
