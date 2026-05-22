package id.ac.ui.cs.advprog.hasilpanen.service;

import id.ac.ui.cs.advprog.hasilpanen.client.AuthServiceRestClient;
import id.ac.ui.cs.advprog.hasilpanen.client.KebunServiceRestClient;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestReport;
import id.ac.ui.cs.advprog.hasilpanen.domain.HarvestStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class HarvestPublicApiService {

    private final AuthServiceRestClient authClient;
    private final KebunServiceRestClient kebunClient;
    private final CreateHarvestService createHarvestService;
    private final GetMyHarvestHistoryService getMyHarvestHistoryService;
    private final MandorHarvestHistoryService mandorHarvestHistoryService;
    private final ApproveHarvestService approveHarvestService;
    private final RejectHarvestService rejectHarvestService;
    private final ApprovalRepository approvalRepository;
    private final MandorHarvestRepository mandorHarvestRepository;

    public HarvestPublicApiService(
            AuthServiceRestClient authClient,
            KebunServiceRestClient kebunClient,
            CreateHarvestService createHarvestService,
            GetMyHarvestHistoryService getMyHarvestHistoryService,
            MandorHarvestHistoryService mandorHarvestHistoryService,
            ApproveHarvestService approveHarvestService,
            RejectHarvestService rejectHarvestService,
            ApprovalRepository approvalRepository,
            MandorHarvestRepository mandorHarvestRepository) {
        this.authClient = authClient;
        this.kebunClient = kebunClient;
        this.createHarvestService = createHarvestService;
        this.getMyHarvestHistoryService = getMyHarvestHistoryService;
        this.mandorHarvestHistoryService = mandorHarvestHistoryService;
        this.approveHarvestService = approveHarvestService;
        this.rejectHarvestService = rejectHarvestService;
        this.approvalRepository = approvalRepository;
        this.mandorHarvestRepository = mandorHarvestRepository;
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
                new MyHarvestHistoryQuery(currentUser.id(), startDate, endDate, status, 0, 200));

        return result.items().stream()
                .map(item -> new MyHarvestResult(item.harvestId(), item.harvestDate(), item.status(), item.rejectionReason()))
                .toList();
    }

    public List<MandorHarvestView> getMandorHarvests(LocalDate harvestDate, String buruhName, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "MANDOR");
        return mandorHarvestHistoryService.listAssignedHarvests(
                new MandorHarvestListQuery(currentUser.id(), harvestDate, buruhName));
    }

    public List<MandorHarvestView> getMandorBuruhHarvests(Long buruhId, LocalDate harvestDate, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        requireRole(currentUser, "MANDOR");

        return mandorHarvestHistoryService.getBuruhHarvests(currentUser.id(), buruhId).stream()
                .filter(item -> harvestDate == null || harvestDate.equals(item.harvestDate()))
                .toList();
    }

    public HarvestDetailResult getHarvestDetail(UUID harvestId, String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        HarvestReport report = approvalRepository.findById(harvestId)
                .orElseThrow(() -> new HarvestNotFoundException("harvest not found"));

        String role = currentUser.role() == null ? "" : currentUser.role().toUpperCase();
        if ("BURUH".equals(role) && !currentUser.id().equals(report.getBuruhAuthId())) {
            throw new RoleForbiddenException("cannot access other buruh harvest");
        }
        if ("MANDOR".equals(role)) {
            Set<Long> assigned = authClient.getBuruhUnderMandor(currentUser.id(), bearerToken);
            if (!assigned.contains(report.getBuruhAuthId())
                    || !kebunClient.hasFarmAccess(currentUser.id(), report.getKebunCodeSnapshot(), bearerToken)) {
                throw new RoleForbiddenException("mandor unauthorized for this harvest");
            }
        }

        return HarvestDetailResult.from(report);
    }

    public List<EligibleShipmentResult> getEligibleForShipment(String bearerToken) {
        HarvestIdentity currentUser = requireCurrentUser(bearerToken);
        String role = currentUser.role() == null ? "" : currentUser.role().toUpperCase();
        if (!"MANDOR".equals(role) && !"SUPIR".equals(role) && !"ADMIN_UTAMA".equals(role)) {
            throw new RoleForbiddenException("MANDOR, SUPIR, or ADMIN_UTAMA role is required");
        }

        List<HarvestReport> reports = mandorHarvestRepository.findAll().stream()
                .filter(item -> item.getStatus() == HarvestStatus.APPROVED)
                .toList();

        if ("MANDOR".equals(role)) {
            Set<Long> assigned = authClient.getBuruhUnderMandor(currentUser.id(), bearerToken);
            reports = reports.stream().filter(item -> assigned.contains(item.getBuruhAuthId())).toList();
        }

        return reports.stream().map(EligibleShipmentResult::from).toList();
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
        try {
            HarvestIdentity identity = authClient.getCurrentUserIdentity(bearerToken);
            if (identity == null || identity.id() == null || identity.role() == null || identity.role().isBlank()) {
                throw new AuthenticationRequiredException("unable to resolve authenticated user");
            }
            return identity;
        } catch (AuthenticationRequiredException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AuthenticationRequiredException("invalid or expired authentication token");
        }
    }

    private void requireRole(HarvestIdentity user, String expectedRole) {
        if (!expectedRole.equalsIgnoreCase(user.role())) {
            throw new RoleForbiddenException(expectedRole + " role is required");
        }
    }

    public record SubmitHarvestRequest(java.math.BigDecimal kilogram, String reportText, List<String> photos) {
    }

    public record HarvestSubmissionResult(UUID harvestId, Long buruhId, Long mandorId, String kebunCode, HarvestStatus status) {
    }

    public record MyHarvestResult(UUID harvestId, LocalDate harvestDate, HarvestStatus status, String rejectionReason) {
        public MyHarvestResult(UUID harvestId, LocalDate harvestDate, HarvestStatus status) {
            this(harvestId, harvestDate, status, null);
        }
    }

    public record HarvestDetailResult(
            UUID harvestId,
            Long buruhId,
            String buruhName,
            String kebunCode,
            LocalDate harvestDate,
            java.math.BigDecimal kilogram,
            String reportText,
            List<String> photos,
            HarvestStatus status,
            String rejectionReason,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime updatedAt,
            java.time.OffsetDateTime approvedAt,
            java.time.OffsetDateTime rejectedAt) {

        public static HarvestDetailResult from(HarvestReport report) {
            return new HarvestDetailResult(
                    report.getHarvestId(),
                    report.getBuruhAuthId(),
                    report.getBuruhNameSnapshot(),
                    report.getKebunCodeSnapshot(),
                    report.getHarvestDate(),
                    report.getKilogram(),
                    report.getReportText(),
                    report.getPhotos(),
                    report.getStatus(),
                    report.getRejectionReason(),
                    report.getCreatedAt(),
                    report.getUpdatedAt(),
                    report.getApprovedAt(),
                    report.getRejectedAt());
        }
    }

    public record EligibleShipmentResult(UUID harvestId, Long buruhId, String kebunCode, LocalDate harvestDate, java.math.BigDecimal kilogram) {
        public static EligibleShipmentResult from(HarvestReport report) {
            return new EligibleShipmentResult(
                    report.getHarvestId(),
                    report.getBuruhAuthId(),
                    report.getKebunCodeSnapshot(),
                    report.getHarvestDate(),
                    report.getKilogram());
        }
    }

    public record HarvestIdentity(Long id, String email, String nama, String role) {
    }

    public record BuruhSupervisor(Long buruhId, String buruhNama, Long mandorId, String mandorNama, boolean active) {
    }

    public record MandorKebunAssignment(Long mandorId, String kebunId, String kebunCode, String kebunName, boolean active) {
    }
}
