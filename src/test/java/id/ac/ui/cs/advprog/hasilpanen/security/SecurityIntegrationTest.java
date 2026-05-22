package id.ac.ui.cs.advprog.hasilpanen.security;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.hasilpanen.config.CorsConfig;
import id.ac.ui.cs.advprog.hasilpanen.config.SecurityConfig;
import id.ac.ui.cs.advprog.hasilpanen.controller.HarvestController;
import id.ac.ui.cs.advprog.hasilpanen.controller.HealthController;
import id.ac.ui.cs.advprog.hasilpanen.controller.InternalHarvestController;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import id.ac.ui.cs.advprog.hasilpanen.repository.HarvestPhotoMetadataRepository;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import id.ac.ui.cs.advprog.hasilpanen.service.TransportEligibilityResult;
import id.ac.ui.cs.advprog.hasilpanen.service.TransportEligibilityService;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageProperties;
import id.ac.ui.cs.advprog.hasilpanen.storage.StorageService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({HarvestController.class, InternalHarvestController.class, HealthController.class})
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtAuthenticationFilter.class,
        InternalApiTokenFilter.class,
        JwtTokenService.class
})
@TestPropertySource(properties = {
        "mysawit.security.jwt-secret=test-secret-value-that-is-at-least-sixty-four-characters-long-123456",
        "mysawit.security.internal-api-token=test-internal-token"
})
class SecurityIntegrationTest {

    private static final String JWT_SECRET = "test-secret-value-that-is-at-least-sixty-four-characters-long-123456";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HarvestPublicApiService harvestPublicApiService;
    @MockBean
    private StorageService storageService;
    @MockBean
    private StorageProperties storageProperties;
    @MockBean
    private HarvestPhotoMetadataRepository harvestPhotoMetadataRepository;
    @MockBean
    private TransportEligibilityService transportEligibilityService;

    @Test
    void protectedEndpointRejectsWhenJwtMissing() throws Exception {
        mockMvc.perform(get("/harvests/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsWhenJwtInvalid() throws Exception {
        mockMvc.perform(get("/harvests/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAllowsWhenJwtValid() throws Exception {
        String token = createValidToken();
        when(harvestPublicApiService.getMyHarvests(null, null, null, "Bearer " + token))
                .thenReturn(List.of(new HarvestPublicApiService.MyHarvestResult(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 5, 20),
                        HarvestStatus.PENDING)));

        mockMvc.perform(get("/harvests/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actuatorHealthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> assertNotUnauthorizedOrForbidden(result.getResponse().getStatus()));
    }

    @Test
    void actuatorInfoEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(result -> assertNotUnauthorizedOrForbidden(result.getResponse().getStatus()));
    }

    @Test
    void actuatorPrometheusEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(result -> assertNotUnauthorizedOrForbidden(result.getResponse().getStatus()));
    }

    @Test
    void internalEndpointRejectsWhenInternalTokenMissing() throws Exception {
        mockMvc.perform(get("/internal/harvests/{harvestId}/transport-eligibility", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalEndpointAllowsWhenInternalTokenValid() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(transportEligibilityService.getEligibility(harvestId))
                .thenReturn(new TransportEligibilityResult(harvestId, true, HarvestStatus.APPROVED, BigDecimal.TEN));

        mockMvc.perform(get("/internal/harvests/{harvestId}/transport-eligibility", harvestId)
                        .header("X-Internal-Api-Token", "test-internal-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true));
    }

    private String createValidToken() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject("buruh@mysawit.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private void assertNotUnauthorizedOrForbidden(int statusCode) {
        assertTrue(statusCode != 401 && statusCode != 403);
    }
}
