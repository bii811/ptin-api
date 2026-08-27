package com.example.ptin.ptin.infrastructure.rest;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.PtinApplicationSearchCriteria;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.port.in.ApprovePtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.in.ApprovePtinApplicationUseCase.ApprovePtinApplicationCommand;
import com.example.ptin.ptin.domain.port.in.GetPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.in.ListMyPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.in.ListPtinApplicationsByStatusUseCase;
import com.example.ptin.ptin.domain.port.in.RejectPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.in.RejectPtinApplicationUseCase.RejectPtinApplicationCommand;
import com.example.ptin.ptin.domain.port.in.RetryPtinIssuanceUseCase;
import com.example.ptin.ptin.domain.port.in.RetryPtinIssuanceUseCase.RetryPtinIssuanceCommand;
import com.example.ptin.ptin.domain.port.in.SearchPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.in.SubmitPtinApplicationUseCase;
import com.example.ptin.ptin.infrastructure.rest.request.RejectPtinApplicationRequest;
import com.example.ptin.ptin.infrastructure.rest.request.SubmitPtinApplicationRequest;
import com.example.ptin.ptin.infrastructure.rest.response.PtinApplicationResponse;
import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import com.example.ptin.shared.web.PageResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ptin/applications")
public class PtinController {

    private final SubmitPtinApplicationUseCase submitPtinApplicationUseCase;
    private final GetPtinApplicationUseCase getPtinApplicationUseCase;
    private final ListMyPtinApplicationsUseCase listMyPtinApplicationsUseCase;
    private final ListPtinApplicationsByStatusUseCase listPtinApplicationsByStatusUseCase;
    private final SearchPtinApplicationsUseCase searchPtinApplicationsUseCase;
    private final ApprovePtinApplicationUseCase approvePtinApplicationUseCase;
    private final RejectPtinApplicationUseCase rejectPtinApplicationUseCase;
    private final RetryPtinIssuanceUseCase retryPtinIssuanceUseCase;

    public PtinController(
            SubmitPtinApplicationUseCase submitPtinApplicationUseCase,
            GetPtinApplicationUseCase getPtinApplicationUseCase,
            ListMyPtinApplicationsUseCase listMyPtinApplicationsUseCase,
            ListPtinApplicationsByStatusUseCase listPtinApplicationsByStatusUseCase,
            SearchPtinApplicationsUseCase searchPtinApplicationsUseCase,
            ApprovePtinApplicationUseCase approvePtinApplicationUseCase,
            RejectPtinApplicationUseCase rejectPtinApplicationUseCase,
            RetryPtinIssuanceUseCase retryPtinIssuanceUseCase) {
        this.submitPtinApplicationUseCase = submitPtinApplicationUseCase;
        this.getPtinApplicationUseCase = getPtinApplicationUseCase;
        this.listMyPtinApplicationsUseCase = listMyPtinApplicationsUseCase;
        this.listPtinApplicationsByStatusUseCase = listPtinApplicationsByStatusUseCase;
        this.searchPtinApplicationsUseCase = searchPtinApplicationsUseCase;
        this.approvePtinApplicationUseCase = approvePtinApplicationUseCase;
        this.rejectPtinApplicationUseCase = rejectPtinApplicationUseCase;
        this.retryPtinIssuanceUseCase = retryPtinIssuanceUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PtinApplicationResponse> submit(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody SubmitPtinApplicationRequest request) {
        var id = submitPtinApplicationUseCase.submit(request.toCommand(principal.userId()));
        var application = getPtinApplicationUseCase.getById(id, principal.userId(), isAuthorizer(principal));
        return ApiResponse.success(PtinApplicationResponse.from(application));
    }

    @GetMapping("/me")
    public ApiResponse<List<PtinApplicationResponse>> listMine(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<PtinApplicationResponse> responses = listMyPtinApplicationsUseCase.listMine(principal.userId()).stream()
                .map(PtinApplicationResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<PtinApplicationResponse> getById(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
        var application = getPtinApplicationUseCase.getById(
                new PtinApplicationId(id), principal.userId(), isAuthorizer(principal));
        return ApiResponse.success(PtinApplicationResponse.from(application));
    }

    @GetMapping
    public ApiResponse<List<PtinApplicationResponse>> list(@RequestParam(defaultValue = "all") String status) {
        List<PtinApplicationResponse> responses = listPtinApplicationsByStatusUseCase
                .list(resolveStatusFilter(status)).stream()
                .map(PtinApplicationResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<PtinApplicationResponse>> search(
            @RequestParam(required = false) PtinStatus status,
            @RequestParam(required = false) String tin,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) Instant submittedFrom,
            @RequestParam(required = false) Instant submittedTo,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var criteria = new PtinApplicationSearchCriteria(status, tin, applicantName, submittedFrom, submittedTo);
        Page<PtinApplication> results = searchPtinApplicationsUseCase.search(criteria, pageable);
        return ApiResponse.success(PageResponse.from(results, PtinApplicationResponse::from));
    }

    /** Maps the {@code status} query param on {@code GET /} to a domain status, or {@code null} for "all". */
    private PtinStatus resolveStatusFilter(String status) {
        return switch (status.toLowerCase()) {
            case "all" -> null;
            case "pending" -> PtinStatus.PENDING_APPROVAL;
            case "approve", "approved" -> PtinStatus.APPROVED;
            case "reject", "rejected" -> PtinStatus.REJECTED;
            case "retry" -> PtinStatus.ISSUANCE_FAILED;
            default -> throw new IllegalArgumentException("Invalid status filter: " + status);
        };
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PtinApplicationResponse> approve(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
        var applicationId = new PtinApplicationId(id);
        approvePtinApplicationUseCase.approve(new ApprovePtinApplicationCommand(applicationId, principal.userId()));
        var application = getPtinApplicationUseCase.getById(applicationId, principal.userId(), true);
        return ApiResponse.success(PtinApplicationResponse.from(application));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<PtinApplicationResponse> reject(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody RejectPtinApplicationRequest request) {
        var applicationId = new PtinApplicationId(id);
        rejectPtinApplicationUseCase.reject(
                new RejectPtinApplicationCommand(applicationId, principal.userId(), request.reason()));
        var application = getPtinApplicationUseCase.getById(applicationId, principal.userId(), true);
        return ApiResponse.success(PtinApplicationResponse.from(application));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<PtinApplicationResponse> retry(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
        var applicationId = new PtinApplicationId(id);
        retryPtinIssuanceUseCase.retry(new RetryPtinIssuanceCommand(applicationId, principal.userId()));
        var application = getPtinApplicationUseCase.getById(applicationId, principal.userId(), true);
        return ApiResponse.success(PtinApplicationResponse.from(application));
    }

    private boolean isAuthorizer(AuthenticatedPrincipal principal) {
        return "AUTHORIZER".equals(principal.role());
    }
}
