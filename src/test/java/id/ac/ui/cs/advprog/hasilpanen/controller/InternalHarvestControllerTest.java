package id.ac.ui.cs.advprog.hasilpanen.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.hasilpanen.config.ApiExceptionHandler;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.service.TransportEligibilityResult;
import id.ac.ui.cs.advprog.hasilpanen.service.TransportEligibilityService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InternalHarvestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class InternalHarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransportEligibilityService transportEligibilityService;

    @Test
    void returnsTransportEligibilityResult() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(transportEligibilityService.getEligibility(harvestId))
                .thenReturn(new TransportEligibilityResult(harvestId, true, HarvestStatus.APPROVED, BigDecimal.TEN));

        mockMvc.perform(get("/internal/harvests/{harvestId}/transport-eligibility", harvestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.harvestId").value(harvestId.toString()))
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
