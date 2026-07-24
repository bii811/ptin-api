package com.example.ptin.auth.infrastructure.rest;

import com.example.ptin.auth.domain.port.in.RequestLoginOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestLoginOtpUseCase.RequestLoginOtpCommand;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase.RequestRegistrationOtpCommand;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase.AuthToken;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase.VerifyLoginOtpCommand;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase.VerifyRegistrationOtpCommand;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.auth.infrastructure.rest.request.RequestOtpRequest;
import com.example.ptin.auth.infrastructure.rest.request.VerifyOtpRequest;
import com.example.ptin.auth.infrastructure.rest.response.AuthTokenResponse;
import com.example.ptin.auth.infrastructure.rest.response.UserSummaryResponse;
import com.example.ptin.profile.domain.port.in.GetProfileUseCase;
import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RequestRegistrationOtpUseCase requestRegistrationOtpUseCase;
    private final VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase;
    private final RequestLoginOtpUseCase requestLoginOtpUseCase;
    private final VerifyLoginOtpUseCase verifyLoginOtpUseCase;
    private final UserRepository userRepository;
    private final GetProfileUseCase getProfileUseCase;
    private final boolean exposeOtpInResponse;

    public AuthController(
            RequestRegistrationOtpUseCase requestRegistrationOtpUseCase,
            VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase,
            RequestLoginOtpUseCase requestLoginOtpUseCase,
            VerifyLoginOtpUseCase verifyLoginOtpUseCase,
            UserRepository userRepository,
            GetProfileUseCase getProfileUseCase,
            @Value("${otp.expose-in-response:false}") boolean exposeOtpInResponse) {
        this.requestRegistrationOtpUseCase = requestRegistrationOtpUseCase;
        this.verifyRegistrationOtpUseCase = verifyRegistrationOtpUseCase;
        this.requestLoginOtpUseCase = requestLoginOtpUseCase;
        this.verifyLoginOtpUseCase = verifyLoginOtpUseCase;
        this.userRepository = userRepository;
        this.getProfileUseCase = getProfileUseCase;
        this.exposeOtpInResponse = exposeOtpInResponse;
    }

    @PostMapping("/register/otp/request")
    public ApiResponse<Void> requestRegistrationOtp(@Valid @RequestBody RequestOtpRequest request) {
        var issued = requestRegistrationOtpUseCase.requestOtp(new RequestRegistrationOtpCommand(request.mobileNumber()));
        return ApiResponse.success(null, exposeOtpInResponse ? "OTP: " + issued.otpCode() : null);
    }

    @PostMapping("/register/otp/verify")
    public ApiResponse<Void> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        verifyRegistrationOtpUseCase.verifyOtp(
                new VerifyRegistrationOtpCommand(request.mobileNumber(), request.otpCode()));
        return ApiResponse.successVoid();
    }

    @PostMapping("/login/otp/request")
    public ApiResponse<Void> requestLoginOtp(@Valid @RequestBody RequestOtpRequest request) {
        var issued = requestLoginOtpUseCase.requestOtp(new RequestLoginOtpCommand(request.mobileNumber()));
        return ApiResponse.success(null, exposeOtpInResponse ? "OTP: " + issued.otpCode() : null);
    }

    @PostMapping("/login/otp/verify")
    public ApiResponse<AuthTokenResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthToken token = verifyLoginOtpUseCase.verifyOtp(
                new VerifyLoginOtpCommand(request.mobileNumber(), request.otpCode()));
        return ApiResponse.success(AuthTokenResponse.of(token.accessToken(), token.expiresAt(), token.role()));
    }

    @GetMapping("/me")
    public ApiResponse<UserSummaryResponse> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new UserNotFoundException(principal.userId().toString()));
        boolean profileComplete = getProfileUseCase.getByUserId(principal.userId()).isComplete();
        return ApiResponse.success(new UserSummaryResponse(
                user.getMobileNumber().value(), user.getRole().name(), profileComplete));
    }
}
