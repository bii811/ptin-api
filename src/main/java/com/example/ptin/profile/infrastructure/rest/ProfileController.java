package com.example.ptin.profile.infrastructure.rest;

import com.example.ptin.profile.domain.port.in.GetProfileUseCase;
import com.example.ptin.profile.domain.port.in.UpdateProfileUseCase;
import com.example.ptin.profile.domain.port.in.UpdateProfileUseCase.UpdateProfileCommand;
import com.example.ptin.profile.infrastructure.rest.request.UpdateProfileRequest;
import com.example.ptin.profile.infrastructure.rest.response.ProfileResponse;
import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    public ProfileController(GetProfileUseCase getProfileUseCase, UpdateProfileUseCase updateProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ApiResponse.success(ProfileResponse.from(getProfileUseCase.getByUserId(principal.userId())));
    }

    @PutMapping("/me")
    public ApiResponse<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        var updated = updateProfileUseCase.update(new UpdateProfileCommand(
                principal.userId(), request.firstName(), request.lastName(), request.avatarUrl()));
        return ApiResponse.success(ProfileResponse.from(updated));
    }
}
