package id.ac.ui.cs.advprog.hasilpanen.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFile upload(MultipartFile file, String folder);

    default void delete(String key) {
        // Best-effort delete is optional for current use-cases.
    }
}
