package id.ac.ui.cs.advprog.hasilpanen.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(StorageProperties properties) {
        String cloudName = properties.getCloudinary().getCloudName();
        String apiKey = properties.getCloudinary().getApiKey();
        String apiSecret = properties.getCloudinary().getApiSecret();

        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("cloudinary mode requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET");
        }

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"));

            String url = (String) result.get("secure_url");
            String key = (String) result.get("public_id");
            return new StoredFile(
                    url,
                    key,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("failed to upload to cloudinary", ex);
        }
    }
}
