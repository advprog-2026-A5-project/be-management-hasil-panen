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
import java.util.Set;
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
    void submitFailsWhenAuthIdentityMissingRequiredFields() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(null, "a@b.c", "Buruh", ""));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.ONE, "Panen", List.of("proof.jpg")),
                "Bearer token"))
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessageContaining("unable to resolve");
    }

    @Test
    void submitPropagatesDuplicateDailySubmissionConflict() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(authClient.getBuruhSupervisor(2L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.BuruhSupervisor(2L, "Buruh", 3L, "Mandor", true));
        when(kebunClient.getKebunByMandor(3L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.MandorKebunAssignment(3L, null, "KB001", "Kebun A", true));
        when(createHarvestService.createHarvest(any()))
                .thenThrow(new DuplicateHarvestSubmissionException("harvest already submitted for today"));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.TEN, "Panen", List.of("proof.jpg")),
                "Bearer token"))
                .isInstanceOf(DuplicateHarvestSubmissionException.class)
                .hasMessageContaining("submitted");
    }

    @Test
    void submitPropagatesInvalidKilogramValidation() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(authClient.getBuruhSupervisor(2L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.BuruhSupervisor(2L, "Buruh", 3L, "Mandor", true));
        when(kebunClient.getKebunByMandor(3L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.MandorKebunAssignment(3L, null, "KB001", "Kebun A", true));
        when(createHarvestService.createHarvest(any()))
                .thenThrow(new IllegalArgumentException("kilogram must be positive"));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.ZERO, "Panen", List.of("proof.jpg")),
                "Bearer token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kilogram");
    }

    @Test
    void submitPropagatesBlankBeritaValidation() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(authClient.getBuruhSupervisor(2L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.BuruhSupervisor(2L, "Buruh", 3L, "Mandor", true));
        when(kebunClient.getKebunByMandor(3L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.MandorKebunAssignment(3L, null, "KB001", "Kebun A", true));
        when(createHarvestService.createHarvest(any()))
                .thenThrow(new IllegalArgumentException("report_text must not be blank"));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.ONE, " ", List.of("proof.jpg")),
                "Bearer token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("report_text");
    }

    @Test
    void submitPropagatesMissingPhotoValidation() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(authClient.getBuruhSupervisor(2L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.BuruhSupervisor(2L, "Buruh", 3L, "Mandor", true));
        when(kebunClient.getKebunByMandor(3L, "Bearer token"))
                .thenReturn(new HarvestPublicApiService.MandorKebunAssignment(3L, null, "KB001", "Kebun A", true));
        when(createHarvestService.createHarvest(any()))
                .thenThrow(new IllegalArgumentException("photos must contain at least one item"));

        assertThatThrownBy(() -> service.submit(
                new HarvestPublicApiService.SubmitHarvestRequest(BigDecimal.ONE, "Panen", List.of()),
                "Bearer token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("photos");
    }

    @Test
    void getMyHarvestsForwardsFiltersAndIncludesRejectionReason() {
        UUID rejectedId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(getMyHarvestHistoryService.getMyHistory(any()))
                .thenReturn(new MyHarvestHistoryResult(
                        List.of(new MyHarvestHistoryResult.Item(
                                rejectedId,
                                LocalDate.of(2026, 5, 20),
                                HarvestStatus.REJECTED,
                                "foto blur")),
                        1,
                        1));

        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 21);
        List<HarvestPublicApiService.MyHarvestResult> results =
                service.getMyHarvests(startDate, endDate, HarvestStatus.REJECTED, "Bearer token");

        ArgumentCaptor<MyHarvestHistoryQuery> captor = ArgumentCaptor.forClass(MyHarvestHistoryQuery.class);
        verify(getMyHarvestHistoryService).getMyHistory(captor.capture());
        assertThat(captor.getValue().buruhId()).isEqualTo(2L);
        assertThat(captor.getValue().startDate()).isEqualTo(startDate);
        assertThat(captor.getValue().endDate()).isEqualTo(endDate);
        assertThat(captor.getValue().status()).isEqualTo(HarvestStatus.REJECTED);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().harvestId()).isEqualTo(rejectedId);
        assertThat(results.getFirst().rejectionReason()).isEqualTo("foto blur");
    }

    @Test
    void getMyHarvestsFailsWhenUserNotBuruh() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(7L, "mandor@mysawit.id", "Mandor", "MANDOR"));

        assertThatThrownBy(() -> service.getMyHarvests(null, null, null, "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class);
    }

    @Test
    void getMandorHarvestsFailsWhenAuthorizationMissing() {
        assertThatThrownBy(() -> service.getMandorHarvests(null, null, null))
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessageContaining("missing Authorization");
    }

    @Test
    void getMandorHarvestsForwardsFilters() {
        UUID harvestId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(7L, "mandor@mysawit.id", "Mandor", "MANDOR"));
        when(mandorHarvestHistoryService.listAssignedHarvests(any()))
                .thenReturn(List.of(new MandorHarvestView(
                        harvestId,
                        LegacyIdBridge.longToUuid(2L),
                        "Buruh A",
                        LocalDate.of(2026, 5, 20),
                        HarvestStatus.PENDING,
                        null)));

        LocalDate harvestDate = LocalDate.of(2026, 5, 20);
        List<MandorHarvestView> results = service.getMandorHarvests(harvestDate, "buruh", "Bearer token");

        ArgumentCaptor<MandorHarvestListQuery> captor = ArgumentCaptor.forClass(MandorHarvestListQuery.class);
        verify(mandorHarvestHistoryService).listAssignedHarvests(captor.capture());
        assertThat(captor.getValue().mandorId()).isEqualTo(7L);
        assertThat(captor.getValue().harvestDate()).isEqualTo(harvestDate);
        assertThat(captor.getValue().buruhName()).isEqualTo("buruh");
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().harvestId()).isEqualTo(harvestId);
    }

    @Test
    void getMandorHarvestsFailsWhenCurrentUserIsBuruh() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));

        assertThatThrownBy(() -> service.getMandorHarvests(null, null, "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class);
    }

    @Test
    void getMandorBuruhHarvestsAppliesHarvestDateFilter() {
        UUID harvestId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(7L, "mandor@mysawit.id", "Mandor", "MANDOR"));
        when(mandorHarvestHistoryService.getBuruhHarvests(7L, 22L))
                .thenReturn(List.of(
                        new MandorHarvestView(
                                harvestId,
                                LegacyIdBridge.longToUuid(22L),
                                "Buruh A",
                                LocalDate.of(2026, 5, 20),
                                HarvestStatus.PENDING,
                                null),
                        new MandorHarvestView(
                                UUID.randomUUID(),
                                LegacyIdBridge.longToUuid(22L),
                                "Buruh A",
                                LocalDate.of(2026, 5, 21),
                                HarvestStatus.APPROVED,
                                null)));

        List<MandorHarvestView> results =
                service.getMandorBuruhHarvests(22L, LocalDate.of(2026, 5, 20), "Bearer token");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().harvestId()).isEqualTo(harvestId);
    }

    @Test
    void getHarvestDetailForBuruhOwnHarvestMapsAllFields() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.restore(
                harvestId,
                2L,
                3L,
                "KB001",
                "K01",
                "Buruh A",
                LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(90),
                "panen valid",
                List.of("proof-1.jpg", "proof-2.jpg"),
                HarvestStatus.REJECTED,
                "foto blur",
                null,
                null,
                3L,
                java.time.OffsetDateTime.parse("2026-05-21T01:00:00+07:00"),
                java.time.OffsetDateTime.parse("2026-05-20T01:00:00+07:00"),
                java.time.OffsetDateTime.parse("2026-05-21T01:30:00+07:00"));
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(approvalRepository.findById(harvestId)).thenReturn(java.util.Optional.of(report));

        HarvestPublicApiService.HarvestDetailResult result = service.getHarvestDetail(harvestId, "Bearer token");

        assertThat(result.harvestId()).isEqualTo(harvestId);
        assertThat(result.buruhId()).isEqualTo(2L);
        assertThat(result.buruhName()).isEqualTo("Buruh A");
        assertThat(result.kebunCode()).isEqualTo("KB001");
        assertThat(result.kilogram()).isEqualTo(BigDecimal.valueOf(90));
        assertThat(result.reportText()).isEqualTo("panen valid");
        assertThat(result.photos()).containsExactly("proof-1.jpg", "proof-2.jpg");
        assertThat(result.status()).isEqualTo(HarvestStatus.REJECTED);
        assertThat(result.rejectionReason()).isEqualTo("foto blur");
        assertThat(result.rejectedAt()).isNotNull();
    }

    @Test
    void getHarvestDetailForMandorAssignedAndSameKebunSucceeds() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.submit(
                harvestId,
                2L,
                7L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.valueOf(90),
                "panen",
                List.of("proof.jpg"));
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(7L, "mandor@mysawit.id", "Mandor", "MANDOR"));
        when(approvalRepository.findById(harvestId)).thenReturn(java.util.Optional.of(report));
        when(authClient.getBuruhUnderMandor(7L, "Bearer token")).thenReturn(Set.of(2L));
        when(kebunClient.hasFarmAccess(7L, "KB001", "Bearer token")).thenReturn(true);

        HarvestPublicApiService.HarvestDetailResult result = service.getHarvestDetail(harvestId, "Bearer token");

        assertThat(result.harvestId()).isEqualTo(harvestId);
        assertThat(result.buruhId()).isEqualTo(2L);
    }

    @Test
    void getHarvestDetailThrowsNotFoundWhenHarvestMissing() {
        UUID harvestId = UUID.randomUUID();
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(7L, "mandor@mysawit.id", "Mandor", "MANDOR"));
        when(approvalRepository.findById(harvestId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.getHarvestDetail(harvestId, "Bearer token"))
                .isInstanceOf(HarvestNotFoundException.class);
    }

    @Test
    void getHarvestDetailRejectsDifferentBuruh() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.submit(
                harvestId,
                20L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.valueOf(20),
                "panen",
                List.of("proof.jpg"));
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));
        when(approvalRepository.findById(harvestId)).thenReturn(java.util.Optional.of(report));

        assertThatThrownBy(() -> service.getHarvestDetail(harvestId, "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class)
                .hasMessageContaining("other buruh");
    }

    @Test
    void getHarvestDetailRejectsMandorWithoutAssignmentOrFarmAccess() {
        UUID harvestId = UUID.randomUUID();
        HarvestReport report = HarvestReport.submit(
                harvestId,
                20L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.valueOf(20),
                "panen",
                List.of("proof.jpg"));
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(7L, "mandor@mysawit.id", "Mandor", "MANDOR"));
        when(approvalRepository.findById(harvestId)).thenReturn(java.util.Optional.of(report));
        when(authClient.getBuruhUnderMandor(7L, "Bearer token")).thenReturn(Set.of(99L));

        assertThatThrownBy(() -> service.getHarvestDetail(harvestId, "Bearer token"))
                .isInstanceOf(RoleForbiddenException.class)
                .hasMessageContaining("unauthorized");
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

    @Test
    void eligibleForShipmentForMandorReturnsOnlyAssignedApprovedHarvests() {
        HarvestReport approvedAssigned = HarvestReport.submit(
                UUID.randomUUID(),
                2L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.TEN,
                "ok",
                List.of("proof.jpg"));
        approvedAssigned.approve(3L);

        HarvestReport approvedUnassigned = HarvestReport.submit(
                UUID.randomUUID(),
                9L,
                3L,
                "KB001",
                null,
                LocalDate.now(),
                BigDecimal.TEN,
                "ok",
                List.of("proof.jpg"));
        approvedUnassigned.approve(3L);

        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(3L, "mandor@mysawit.id", "Mandor", "MANDOR"));
        when(mandorHarvestRepository.findAll()).thenReturn(List.of(approvedAssigned, approvedUnassigned));
        when(authClient.getBuruhUnderMandor(3L, "Bearer token")).thenReturn(Set.of(2L));

        List<HarvestPublicApiService.EligibleShipmentResult> result = service.getEligibleForShipment("Bearer token");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().harvestId()).isEqualTo(approvedAssigned.getHarvestId());
        assertThat(result.getFirst().buruhId()).isEqualTo(2L);
    }

    @Test
    void eligibleForShipmentFailsForUnauthorizedRole() {
        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(2L, "buruh@mysawit.id", "Buruh", "BURUH"));

        assertThatThrownBy(() -> service.getEligibleForShipment("Bearer token"))
                .isInstanceOf(RoleForbiddenException.class)
                .hasMessageContaining("MANDOR, SUPIR, or ADMIN_UTAMA");
    }

    @Test
    void eligibleForShipmentAllowsSupirRole() {
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

        when(authClient.getCurrentUserIdentity("Bearer token"))
                .thenReturn(new HarvestPublicApiService.HarvestIdentity(5L, "supir@mysawit.id", "Supir", "SUPIR"));
        when(mandorHarvestRepository.findAll()).thenReturn(List.of(approved));

        assertThat(service.getEligibleForShipment("Bearer token")).hasSize(1);
    }
}
