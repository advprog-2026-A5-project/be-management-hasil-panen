package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.hasilpanen.client.AuthServiceRestClient;
import id.ac.ui.cs.advprog.hasilpanen.client.KebunServiceRestClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HarvestPublicApiServiceTest {

    @Mock
    private AuthServiceRestClient authClient;
    @Mock
    private KebunServiceRestClient kebunClient;
    @Mock
    private CreateHarvestService createHarvestService;
    @Mock
    private GetMyHarvestHistoryService getMyHarvestHistoryService;
    @Mock
    private MandorHarvestHistoryService mandorHarvestHistoryService;
    @Mock
    private ApproveHarvestService approveHarvestService;
    @Mock
    private RejectHarvestService rejectHarvestService;
    @Mock
    private ApprovalRepository approvalRepository;
    @Mock
    private MandorHarvestRepository mandorHarvestRepository;

    @InjectMocks
    private HarvestPublicApiService service;

    @Test
    void submitUsesAuthenticatedBuruhAndAssignmentSnapshots() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.submit(
                harvestId,
                2L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.valueOf(120),
                "Panen",
                List.of("proof.jpg"));
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(authClient.getBuruhSupervisor(2L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.BuruhSupervisor(2L, "Buruh", 3L, "Mandor", true));
        when(kebunClient.getKebunByMandor(3L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.MandorKebunAssignment(3L, null, "KB001", "Kebun A", true));
        when(createHarvestService.createHarvest(any())).thenReturn(report);

        HarvestPublicApiService.HarvestSubmissionResult result = service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.valueOf(120), "Panen", List.of("proof.jpg")),
                "Bearer token");

        ArgumentCaptor<CreateHarvestCommand> captor = ArgumentCaptor.forClass(CreateHarvestCommand.class);
        verify(createHarvestService).createHarvest(captor.capture());
        assertThat(captor.getValue().buruhId()).isEqualTo(2L);
        assertThat(captor.getValue().mandorId()).isEqualTo(3L);
        assertThat(captor.getValue().kebunCode()).isEqualTo("KB001");
        assertThat(result.status()).isEqualTo(HarvestStatus.PENDING);
    }

    @Test
    void submitFailsWhenCurrentUserIsNotBuruh() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(99L, "mandor@mysawit.id", "Mandor", "MANDOR"));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.ONE, "Panen", List.of("proof.jpg")),
                "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class);
    }

    @Test
    void approveUsesAuthenticatedMandorIdentity() {
        UUID harvestId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(3L, "mandor@mysawit.id", "Mandor", "MANDOR"));

        service.approve(harvestId, "Bearer token");

        verify(approveHarvestService).approve(new ApproveHarvestCommand(harvestId, 3L));
    }

    @Test
    void approveFailsWhenCurrentUserIsNotMandor() {
        UUID harvestId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));

        assertThatThrownBy(() -> service.approve(harvestId, "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class);
    }

    @Test
    void rejectFailsWhenCurrentUserIsNotMandor() {
        UUID harvestId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));

        assertThatThrownBy(() -> service.reject(harvestId, "reason", "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class);
    }

    @Test
    void submitFailsSafelyWhenAuthServiceRejectsToken() {
        when(authClient.getCurrentUserIdentity("Bearer invalid"))
                .thenThrow(new RuntimeException("401 from auth"));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.ONE, "Panen", List.of("proof.jpg")),
                "Bearer invalid"))
                .isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void eligibleForShipmentReturnsOnlyApprovedHarvests() {
        HarvestReport approved = HarvestReport.submit(
                UUID.randomUUID(),
                2L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.TEN,
                "ok",
                List.of("proof.jpg"));
        approved.approve(3L);

        HarvestReport rejected = HarvestReport.submit(
                UUID.randomUUID(),
                2L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.TEN,
                "bad",
                List.of("proof.jpg"));
        rejected.reject(3L, "invalid");

        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(1L, "admin@mysawit.id", "Admin", "ADMIN_UTAMA"));
        when(mandorHarvestRepository.findAll()).thenReturn(List.of(approved, rejected));

        List<HarvestPublicApiService.EligibleShipmentResult> result = service.getEligibleForShipment("Bearer token");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().harvestId()).isEqualTo(approved.getHarvestId());
    }
}
