package id.ac.ui.cs.advprog.hasilpanen.repository;

import id.ac.ui.cs.advprog.hasilpanen.storage.StoredFile;

import java.util.List;
import java.util.UUID;

public interface HarvestPhotoMetadataRepository {

    void updatePhotoMetadata(UUID harvestId, List<StoredFile> files);
}
