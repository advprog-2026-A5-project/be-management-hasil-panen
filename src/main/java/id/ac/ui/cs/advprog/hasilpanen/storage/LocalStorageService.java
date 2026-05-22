package id.ac.ui.cs.advprog.hasilpanen.storage;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

public class LocalStorageService implements StorageService {

    private final Path rootPath;

    public LocalStorageService(String localRoot) {
        this.rootPath = Path.of(localRoot).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootPath);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to initialize local storage root", ex);
        }
    }

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("proof photo must not be empty");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.contains("..")) {
            throw new IllegalArgumentException("invalid filename");
        }

        String extension = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.')) : "";
        String newName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + extension;

        Path folderPath = rootPath.resolve(folder == null || folder.isBlank() ? "harvest-proofs" : folder).normalize();
        Path target = folderPath.resolve(newName).normalize();
        if (!target.startsWith(rootPath)) {
            throw new IllegalArgumentException("invalid upload target path");
        }

        try {
            Files.createDirectories(folderPath);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to store uploaded file", ex);
        }

        String relative = rootPath.relativize(target).toString().replace('\\', '/');
        return new StoredFile(
                "/uploads/" + relative,
                relative,
                original,
                file.getContentType(),
                file.getSize());
    }
}
