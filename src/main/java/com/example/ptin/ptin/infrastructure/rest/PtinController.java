package com.example.ptin.ptin.infrastructure.rest;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.port.in.ApprovePtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.in.ApprovePtinApplicationUseCase.ApprovePtinApplicationCommand;
import com.example.ptin.ptin.domain.port.in.GetPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.in.ListMyPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.in.ListPendingPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.in.RejectPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.in.RejectPtinApplicationUseCase.RejectPtinApplicationCommand;
import com.example.ptin.ptin.domain.port.in.RetryPtinIssuanceUseCase;
import com.example.ptin.ptin.domain.port.in.RetryPtinIssuanceUseCase.RetryPtinIssuanceCommand;
import com.example.ptin.ptin.domain.port.in.SubmitPtinApplicationUseCase;
import com.example.ptin.ptin.infrastructure.rest.request.RejectPtinApplicationRequest;
import com.example.ptin.ptin.infrastructure.rest.request.SubmitPtinApplicationRequest;
import com.example.ptin.ptin.infrastructure.rest.response.PtinApplicationResponse;
import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ptin/applications")
public class PtinController {

    private final SubmitPtinApplicationUseCase submitPtinApplicationUseCase;
    private final GetPtinApplicationUseCase getPtinApplicationUseCase;
    private final ListMyPtinApplicationsUseCase listMyPtinApplicationsUseCase;
    private final ListPendingPtinApplicationsUseCase listPendingPtinApplicationsUseCase;
    private final ApprovePtinApplicationUseCase approvePtinApplicationUseCase;
    private final RejectPtinApplicationUseCase rejectPtinApplicationUseCase;
    private final RetryPtinIssuanceUseCase retryPtinIssuanceUseCase;

    public PtinController(
            SubmitPtinApplicationUseCase submitPtinApplicationUseCase,
            GetPtinApplicationUseCase getPtinApplicationUseCase,
            ListMyPtinApplicationsUseCase listMyPtinApplicationsUseCase,
            ListPendingPtinApplicationsUseCase listPendingPtinApplicationsUseCase,
            ApprovePtinApplicationUseCase approvePtinApplicationUseCase,
            RejectPtinApplicationUseCase rejectPtinApplicationUseCase,
            RetryPtinIssuanceUseCase retryPtinIssuanceUseCase) {
        this.submitPtinApplicationUseCase = submitPtinApplicationUseCase;
        this.getPtinApplicationUseCase = getPtinApplicationUseCase;
        this.listMyPtinApplicationsUseCase = listMyPtinApplicationsUseCase;
        this.listPendingPtinApplicationsUseCase = listPendingPtinApplicationsUseCase;
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
    public ApiResponse<List<PtinApplicationResponse>> listPending() {
        List<PtinApplicationResponse> responses = listPendingPtinApplicationsUseCase.listPending().stream()
                .map(PtinApplicationResponse::from)
                .toList();
        return ApiResponse.success(responses);
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
