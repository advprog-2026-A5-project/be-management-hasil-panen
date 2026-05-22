package id.ac.ui.cs.advprog.hasilpanen.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceTest {

    @Test
    void localStorageUploadsAndReturnsMetadata() throws Exception {
        Path tempDir = Files.createTempDirectory("hasil-panen-storage-test");
        LocalStorageService service = new LocalStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "photos",
                "proof.jpg",
                "image/jpeg",
                "dummy-image".getBytes());

        StoredFile stored = service.upload(file, "harvest-proofs");

        assertThat(stored.url()).contains("/uploads/");
        assertThat(stored.key()).isNotBlank();
        assertThat(stored.originalFilename()).isEqualTo("proof.jpg");
        assertThat(stored.contentType()).isEqualTo("image/jpeg");
        assertThat(stored.size()).isGreaterThan(0L);
        assertThat(Files.exists(tempDir.resolve(stored.key()))).isTrue();
    }

    @Test
    void localStorageRejectsPathTraversalFilename() {
        LocalStorageService service = new LocalStorageService("build/test-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "photos",
                "..\\..\\evil.jpg",
                "image/jpeg",
                "dummy-image".getBytes());

        assertThatThrownBy(() -> service.upload(file, "harvest-proofs"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void localStorageSanitizesUnsafeFilenameCharacters() throws Exception {
        Path tempDir = Files.createTempDirectory("hasil-panen-storage-sanitize");
        LocalStorageService service = new LocalStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "photos",
                "proof @#$ 1.jpg",
                "image/jpeg",
                "dummy-image".getBytes());

        StoredFile stored = service.upload(file, "harvest-proofs");

        assertThat(stored.originalFilename()).isEqualTo("proof @#$ 1.jpg");
        assertThat(stored.key()).endsWith(".jpg");
        assertThat(stored.key()).doesNotContain("..");
    }

    @Test
    void localStorageRejectsTraversalFolderTarget() {
        LocalStorageService service = new LocalStorageService("build/test-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "photos",
                "proof.jpg",
                "image/jpeg",
                "dummy-image".getBytes());

        assertThatThrownBy(() -> service.upload(file, "../outside"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid upload target path");
    }

    @Test
    void localStorageRejectsEmptyFile() {
        LocalStorageService service = new LocalStorageService("build/test-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "photos",
                "proof.jpg",
                "image/jpeg",
                new byte[0]);

        assertThatThrownBy(() -> service.upload(file, "harvest-proofs"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    void cloudinaryStorageFailsFastWhenConfigMissing() {
        StorageProperties properties = new StorageProperties();
        properties.setProvider("cloudinary");

        assertThatThrownBy(() -> new CloudinaryStorageService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cloudinary mode requires");
    }
}
