package id.ac.ui.cs.advprog.hasilpanen.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.hasilpanen.config.ApiExceptionHandler;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.service.DuplicateHarvestSubmissionException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestTerminalStatusException;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorHarvestView;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService.HarvestIdentity;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService.HarvestSubmissionResult;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService.MyHarvestResult;
import id.ac.ui.cs.advprog.hasilpanen.service.AuthenticationRequiredException;
import id.ac.ui.cs.advprog.hasilpanen.service.RoleForbiddenException;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestPhotoMetadataRepository;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageProperties;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageService;
import id.ac.ui.cs.advprog.hasilpanen.storage.StoredFile;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = HarvestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HarvestPublicApiService harvestPublicApiService;
    @MockBean
    private StorageService storageService;
    @MockBean
    private StorageProperties storageProperties;
    @MockBean
    private HarvestPhotoMetadataRepository photoMetadataRepository;

    @Test
    void buruhCanSubmitHarvestAndSnapshotIsReturned() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(harvestPublicApiService.submit(any(), eq("Bearer token")))
                .thenReturn(new HarvestSubmissionResult(harvestId, 20L, 30L, "KB001", HarvestStatus.PENDING));

        mockMvc.perform(post("/harvests")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kilogram": 100,
                                  "reportText": "Panen hari ini",
                                  "photos": ["proof-1.jpg"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.harvestId").value(harvestId.toString()))
                .andExpect(jsonPath("$.buruhId").value(20))
                .andExpect(jsonPath("$.mandorId").value(30))
                .andExpect(jsonPath("$.kebunCode").value("KB001"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void prefixedPathSubmitRemainsCompatible() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(harvestPublicApiService.submit(any(), eq("Bearer token")))
                .thenReturn(new HarvestSubmissionResult(harvestId, 20L, 30L, "KB001", HarvestStatus.PENDING));

        mockMvc.perform(post("/api/hasil-panen/harvests")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kilogram": 100,
                                  "reportText": "Panen hari ini",
                                  "photos": ["proof-1.jpg"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.harvestId").value(harvestId.toString()));
    }

    @Test
    void multipartSubmitReturnsCreatedAndPersistsPhotoMetadata() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(storageProperties.getMaxUploadSizeMb()).thenReturn(5);
        when(storageProperties.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));
        when(storageService.upload(any(), eq("harvest-proofs")))
                .thenReturn(new StoredFile("https://cdn/proof.jpg", "proof-key", "proof.jpg", "image/jpeg", 8));
        when(harvestPublicApiService.submit(any(), eq("Bearer token")))
                .thenReturn(new HarvestSubmissionResult(harvestId, 20L, 30L, "KB001", HarvestStatus.PENDING));

        MockMultipartFile photo = new MockMultipartFile("photos", "proof.jpg", "image/jpeg", "img".getBytes());

        mockMvc.perform(multipart("/harvests")
                        .file(photo)
                        .param("kilogram", "100")
                        .param("reportText", "Panen multipart")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.harvestId").value(harvestId.toString()));

        verify(photoMetadataRepository).updatePhotoMetadata(eq(harvestId), any());
    }

    @Test
    void multipartSubmitFailsWhenPhotoPartEmpty() throws Exception {
        when(storageProperties.getMaxUploadSizeMb()).thenReturn(5);
        when(storageProperties.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));

        MockMultipartFile emptyPhoto = new MockMultipartFile("photos", "proof.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/harvests")
                        .file(emptyPhoto)
                        .param("kilogram", "100")
                        .param("reportText", "Panen multipart")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void multipartSubmitFailsWhenUnsupportedContentType() throws Exception {
        when(storageProperties.getMaxUploadSizeMb()).thenReturn(5);
        when(storageProperties.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));

        MockMultipartFile invalid = new MockMultipartFile("photos", "proof.gif", "image/gif", "gif".getBytes());

        mockMvc.perform(multipart("/harvests")
                        .file(invalid)
                        .param("kilogram", "100")
                        .param("reportText", "Panen multipart")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("unsupported file content type: image/gif"));
    }

    @Test
    void multipartSubmitFailsWhenFileTooLarge() throws Exception {
        when(storageProperties.getMaxUploadSizeMb()).thenReturn(1);
        when(storageProperties.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));

        MockMultipartFile bigPhoto = new MockMultipartFile("photos", "proof.jpg", "image/jpeg", new byte[2 * 1024 * 1024]);

        mockMvc.perform(multipart("/harvests")
                        .file(bigPhoto)
                        .param("kilogram", "100")
                        .param("reportText", "Panen multipart")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("file exceeds max upload size"));
    }

    @Test
    void submitFailsWithForbiddenWhenRoleIsNotBuruh() throws Exception {
        when(harvestPublicApiService.submit(any(), eq("Bearer token")))
                .thenThrow(new RoleForbiddenException("BURUH role is required"));

        mockMvc.perform(post("/harvests")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kilogram": 100,
                                  "reportText": "Panen hari ini",
                                  "photos": ["proof-1.jpg"]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void submitFailsValidationWhenPhotosFieldEmpty() throws Exception {
        mockMvc.perform(post("/harvests")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kilogram": 100,
                                  "reportText": "Panen hari ini",
                                  "photos": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void submitFailsWithConflictWhenDuplicateDailyReport() throws Exception {
        when(harvestPublicApiService.submit(any(), eq("Bearer token")))
                .thenThrow(new DuplicateHarvestSubmissionException("harvest already submitted for today"));

        mockMvc.perform(post("/harvests")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kilogram": 100,
                                  "reportText": "Panen hari ini",
                                  "photos": ["proof-1.jpg"]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void buruhCanGetOwnHarvestHistory() throws Exception {
        when(harvestPublicApiService.getMyHarvests(null, null, null, "Bearer token"))
                .thenReturn(List.of(new MyHarvestResult(UUID.randomUUID(), LocalDate.parse("2026-05-19"), HarvestStatus.PENDING)));

        mockMvc.perform(get("/harvests/me").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void mandorCanGetHarvestHistoryWithFilters() throws Exception {
        when(harvestPublicApiService.getMandorHarvests(LocalDate.of(2026, 5, 20), "buruh", "Bearer token"))
                .thenReturn(List.of(new MandorHarvestView(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Buruh A",
                        LocalDate.of(2026, 5, 20),
                        HarvestStatus.PENDING,
                        null)));

        mockMvc.perform(get("/mandor/harvests")
                        .param("harvestDate", "2026-05-20")
                        .param("buruhName", "buruh")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buruhName").value("Buruh A"));
    }

    @Test
    void detailReturns404WhenHarvestMissing() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(harvestPublicApiService.getHarvestDetail(harvestId, "Bearer token"))
                .thenThrow(new HarvestNotFoundException("harvest not found"));

        mockMvc.perform(get("/harvests/{harvestId}", harvestId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void approveFailsWithConflictForInvalidTransition() throws Exception {
        UUID harvestId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new HarvestTerminalStatusException("already terminal"))
                .when(harvestPublicApiService).approve(harvestId, "Bearer token");

        mockMvc.perform(post("/harvests/{harvestId}/approve", harvestId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void mandorCanApproveHarvest() throws Exception {
        UUID harvestId = UUID.randomUUID();

        mockMvc.perform(post("/harvests/{harvestId}/approve", harvestId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());

        verify(harvestPublicApiService).approve(harvestId, "Bearer token");
    }

    @Test
    void rejectWithoutReasonFailsValidation() throws Exception {
        UUID harvestId = UUID.randomUUID();

        mockMvc.perform(post("/harvests/{harvestId}/reject", harvestId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void submitFailsWithUnauthorizedWhenAuthorizationHeaderMissing() throws Exception {
        when(harvestPublicApiService.submit(any(), eq(null)))
                .thenThrow(new AuthenticationRequiredException("missing Authorization header"));

        mockMvc.perform(post("/harvests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kilogram": 100,
                                  "reportText": "Panen hari ini",
                                  "photos": ["proof-1.jpg"]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void approveFailsWithForbiddenWhenRoleIsNotMandor() throws Exception {
        UUID harvestId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new RoleForbiddenException("MANDOR role is required"))
                .when(harvestPublicApiService).approve(harvestId, "Bearer token");

        mockMvc.perform(post("/harvests/{harvestId}/approve", harvestId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void rejectFailsWithForbiddenWhenRoleIsNotMandor() throws Exception {
        UUID harvestId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new RoleForbiddenException("MANDOR role is required"))
                .when(harvestPublicApiService).reject(harvestId, "not valid", "Bearer token");

        mockMvc.perform(post("/harvests/{harvestId}/reject", harvestId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "not valid"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void rejectFailsWithConflictForInvalidTransition() throws Exception {
        UUID harvestId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new HarvestTerminalStatusException("already terminal"))
                .when(harvestPublicApiService).reject(harvestId, "reason", "Bearer token");

        mockMvc.perform(post("/harvests/{harvestId}/reject", harvestId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "reason"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void eligibleForShipmentEndpointReturnsApprovedItems() throws Exception {
        when(harvestPublicApiService.getEligibleForShipment("Bearer token"))
                .thenReturn(List.of(new HarvestPublicApiService.EligibleShipmentResult(
                        UUID.randomUUID(), 20L, "KB001", LocalDate.of(2026, 5, 20), java.math.BigDecimal.TEN)));

        mockMvc.perform(get("/harvest-reports/eligible-for-shipment")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kebunCode").value("KB001"));
    }
}
