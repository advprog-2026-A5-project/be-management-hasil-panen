package id.ac.ui.cs.advprog.hasilpanen.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.ac.ui.cs.advprog.hasilpanen.storage.LocalStorageService;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageProperties;
import org.junit.jupiter.api.Test;

class StorageConfigurationTest {

    @Test
    void storageServiceUsesLocalProviderByDefault() {
        StorageConfiguration configuration = new StorageConfiguration();
        StorageProperties properties = new StorageProperties();
        properties.setProvider("local");
        properties.setLocalRoot("build/test-uploads");

        assertThat(configuration.storageService(properties)).isInstanceOf(LocalStorageService.class);
    }

    @Test
    void storageServiceFailsFastWhenCloudinarySelectedWithoutCredentials() {
        StorageConfiguration configuration = new StorageConfiguration();
        StorageProperties properties = new StorageProperties();
        properties.setProvider("cloudinary");

        assertThatThrownBy(() -> configuration.storageService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cloudinary mode requires");
    }
}
