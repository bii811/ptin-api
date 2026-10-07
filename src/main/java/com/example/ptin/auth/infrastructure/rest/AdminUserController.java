package com.example.ptin.auth.infrastructure.rest;

import com.example.ptin.auth.domain.port.in.AssignTemporaryPasswordUseCase;
import com.example.ptin.auth.domain.port.in.AssignTemporaryPasswordUseCase.AssignTemporaryPasswordCommand;
import com.example.ptin.auth.infrastructure.rest.request.AssignTemporaryPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.response.TemporaryPasswordResponse;
import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Role gating ({@code ADMIN}) is enforced on the use case, per the project convention. */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AssignTemporaryPasswordUseCase assignTemporaryPasswordUseCase;

    public AdminUserController(AssignTemporaryPasswordUseCase assignTemporaryPasswordUseCase) {
        this.assignTemporaryPasswordUseCase = assignTemporaryPasswordUseCase;
    }

    @PostMapping("/temporary-password")
    public ApiResponse<TemporaryPasswordResponse> assignTemporaryPassword(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AssignTemporaryPasswordRequest request) {
        String password = assignTemporaryPasswordUseCase.assign(
                new AssignTemporaryPasswordCommand(principal.userId(), request.mobileNumber()));
        return ApiResponse.success(new TemporaryPasswordResponse(password));
    }
}
