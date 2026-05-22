package id.ac.ui.cs.advprog.hasilpanen.config;

import id.ac.ui.cs.advprog.hasilpanen.storage.CloudinaryStorageService;
import id.ac.ui.cs.advprog.hasilpanen.storage.LocalStorageService;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageProperties;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    @Bean
    public StorageService storageService(StorageProperties properties) {
        if ("cloudinary".equalsIgnoreCase(properties.getProvider())) {
            return new CloudinaryStorageService(properties);
        }
        return new LocalStorageService(properties.getLocalRoot());
    }
}
