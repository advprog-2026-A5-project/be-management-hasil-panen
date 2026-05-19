package id.ac.ui.cs.advprog.hasilpanen.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.hasilpanen.config.ApiExceptionHandler;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService.HarvestIdentity;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService.HarvestSubmissionResult;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService.MyHarvestResult;
import id.ac.ui.cs.advprog.hasilpanen.service.RoleForbiddenException;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = HarvestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HarvestPublicApiService harvestPublicApiService;

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.harvestId").value(harvestId.toString()))
                .andExpect(jsonPath("$.buruhId").value(20))
                .andExpect(jsonPath("$.mandorId").value(30))
                .andExpect(jsonPath("$.kebunCode").value("KB001"))
                .andExpect(jsonPath("$.status").value("PENDING"));
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
    void buruhCanGetOwnHarvestHistory() throws Exception {
        when(harvestPublicApiService.getMyHarvests(null, null, null, "Bearer token"))
                .thenReturn(List.of(new MyHarvestResult(UUID.randomUUID(), LocalDate.parse("2026-05-19"), HarvestStatus.PENDING)));

        mockMvc.perform(get("/harvests/me").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
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
}
