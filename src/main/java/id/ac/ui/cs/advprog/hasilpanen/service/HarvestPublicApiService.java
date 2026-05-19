package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.AuthServiceRestClient;
import id.ac.ui.cs.advprog.hasilpanen.client.KebunServiceRestClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public class HarvestPublicApiService {

    private final AuthServiceRestClient authClient;
    private final KebunServiceRestClient kebunClient;
    private final CreateHarvestService createHarvestService;
    private final GetMyHarvestHistoryService getMyHarvestHistoryService;
    private final MandorHarvestHistoryService mandorHarvestHistoryService;
    private final ApproveHarvestService approveHarvestService;
    private final RejectHarvestService rejectHarvestService;

    public HarvestPublicApiService(
            AuthServiceRestClient authClient,
            KebunServiceRestClient kebunClient,
            CreateHarvestService createHarvestService,
            GetMyHarvestHistoryService getMyHarvestHistoryService,
            MandorHarvestHistoryService mandorHarvestHistoryService,
            ApproveHarvestService approveHarvestService,
            RejectHarvestService rejectHarvestService) {
        this.authClient = authClient;
        this.kebunClient = kebunClient;
        this.createHarvestService = createHarvestService;
        this.getMyHarvestHistoryService = getMyHarvestHistoryService;
        this.mandorHarvestHistoryService = mandorHarvestHistoryService;
        this.approveHarvestService = approveHarvestService;
        this.rejectHarvestService = rejectHarvestService;
    }

    public HarvestSubmissionResult submit(SubmitHarvestRequest request, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "BURUH");

        BuruhSupervisor supervisor = authClient.getBuruhSupervisor(currentUser.id(), bearerToken);
        if (!supervisor.active() || supervisor.mandorId() == null) {
            throw new RoleForbiddenException("buruh is not assigned to a mandor");
        }

        MandorKebunAssignment kebunAssignment = kebunClient.getKebunByMandor(supervisor.mandorId(), bearerToken);
        if (!kebunAssignment.active() || kebunAssignment.kebunCode() == null || kebunAssignment.kebunCode().isBlank()) {
            throw new RoleForbiddenException("mandor is not assigned to an active kebun");
        }

        HarvestReport report = createHarvestService.createHarvest(new CreateHarvestCommand(
                currentUser.id(),
                supervisor.mandorId(),
                kebunAssignment.kebunCode(),
                kebunAssignment.kebunId(),
                request.kilogram(),
                request.reportText(),
                request.photos()));

        return new HarvestSubmissionResult(
                report.getHarvestId(),
                report.getBuruhAuthId(),
                report.getMandorIdSnapshot(),
                report.getKebunCodeSnapshot(),
                report.getStatus());
    }

    public List<MyHarvestResult> getMyHarvests(LocalDate startDate, LocalDate endDate, HarvestStatus status, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "BURUH");

        MyHarvestHistoryResult result = getMyHarvestHistoryService.getMyHistory(
                new MyHarvestHistoryQuery(currentUser.id(), startDate, endDate, status, 0, 50));

        return result.items().stream()
                .map(item -> new MyHarvestResult(item.harvestId(), item.harvestDate(), item.status()))
                .toList();
    }

    public List<MandorHarvestView> getMandorHarvests(LocalDate harvestDate, String buruhName, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "MANDOR");
        return mandorHarvestHistoryService.listAssignedHarvests(
                new MandorHarvestListQuery(currentUser.id(), harvestDate, buruhName));
    }

    public void approve(UUID harvestId, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "MANDOR");
        approveHarvestService.approve(new ApproveHarvestCommand(harvestId, currentUser.id()));
    }

    public void reject(UUID harvestId, String reason, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "MANDOR");
        rejectHarvestService.reject(new RejectHarvestCommand(harvestId, currentUser.id(), reason));
    }

    private HarvestIdentity requireCurrentUser(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new AuthenticationRequiredException("missing Authorization header");
        }
        return authClient.getCurrentUserIdentity(bearerToken);
    }

    private void requireRole(HarvestIdentity user, String expectedRole) {
        if (!expectedRole.equalsIgnoreCase(user.role())) {
            throw new RoleForbiddenException(expectedRole + " role is required");
        }
    }

    public record SubmitHarvestRequest(java.math.BigDecimal kilogram, String reportText, List<String> photos) {}

    public record HarvestSubmissionResult(UUID harvestId, Long buruhId, Long mandorId, String kebunCode, HarvestStatus status) {}

    public record MyHarvestResult(UUID harvestId, LocalDate harvestDate, HarvestStatus status) {}

    public record HarvestIdentity(Long id, String email, String nama, String role) {}

    public record BuruhSupervisor(Long buruhId, String buruhNama, Long mandorId, String mandorNama, boolean active) {}

    public record MandorKebunAssignment(Long mandorId, String kebunId, String kebunCode, String kebunName, boolean active) {}
}
