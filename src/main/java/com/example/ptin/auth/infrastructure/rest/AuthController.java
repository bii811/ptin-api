package com.example.ptin.auth.infrastructure.rest;

import com.example.ptin.auth.config.OtpProperties;
import com.example.ptin.auth.domain.port.in.AuthToken;
import com.example.ptin.auth.domain.port.in.GetCurrentUserUseCase;
import com.example.ptin.auth.domain.port.in.GetCurrentUserUseCase.CurrentUser;
import com.example.ptin.auth.domain.port.in.LoginWithPasswordUseCase;
import com.example.ptin.auth.domain.port.in.LoginWithPasswordUseCase.LoginWithPasswordCommand;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestLoginOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestLoginOtpUseCase.RequestLoginOtpCommand;
import com.example.ptin.auth.domain.port.in.RequestPasswordResetOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestPasswordResetOtpUseCase.RequestPasswordResetOtpCommand;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase.RequestRegistrationOtpCommand;
import com.example.ptin.auth.domain.port.in.ResetPasswordUseCase;
import com.example.ptin.auth.domain.port.in.ResetPasswordUseCase.ResetPasswordCommand;
import com.example.ptin.auth.domain.port.in.SetPasswordUseCase;
import com.example.ptin.auth.domain.port.in.SetPasswordUseCase.SetPasswordCommand;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase.VerifyLoginOtpCommand;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase.VerifyRegistrationOtpCommand;
import com.example.ptin.auth.infrastructure.rest.request.LoginWithPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.request.RequestOtpRequest;
import com.example.ptin.auth.infrastructure.rest.request.RequestPasswordResetOtpRequest;
import com.example.ptin.auth.infrastructure.rest.request.ResetPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.request.SetPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.request.VerifyOtpRequest;
import com.example.ptin.auth.infrastructure.rest.response.AuthTokenResponse;
import com.example.ptin.auth.infrastructure.rest.response.UserSummaryResponse;
import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Three ways in, in the order a taxpayer meets them:
 *
 * <ol>
 *   <li>register, then log in with phone + OTP — the only credential before a TIN exists;
 *   <li>once a TIN has been issued, optionally set a password and log in with TIN + password;
 *   <li>if that password is forgotten, recover with TIN + registered phone + OTP.
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RequestRegistrationOtpUseCase requestRegistrationOtpUseCase;
    private final VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase;
    private final RequestLoginOtpUseCase requestLoginOtpUseCase;
    private final VerifyLoginOtpUseCase verifyLoginOtpUseCase;
    private final LoginWithPasswordUseCase loginWithPasswordUseCase;
    private final SetPasswordUseCase setPasswordUseCase;
    private final RequestPasswordResetOtpUseCase requestPasswordResetOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final boolean exposeOtpInResponse;

    public AuthController(
            RequestRegistrationOtpUseCase requestRegistrationOtpUseCase,
            VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase,
            RequestLoginOtpUseCase requestLoginOtpUseCase,
            VerifyLoginOtpUseCase verifyLoginOtpUseCase,
            LoginWithPasswordUseCase loginWithPasswordUseCase,
            SetPasswordUseCase setPasswordUseCase,
            RequestPasswordResetOtpUseCase requestPasswordResetOtpUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            OtpProperties otpProperties) {
        this.requestRegistrationOtpUseCase = requestRegistrationOtpUseCase;
        this.verifyRegistrationOtpUseCase = verifyRegistrationOtpUseCase;
        this.requestLoginOtpUseCase = requestLoginOtpUseCase;
        this.verifyLoginOtpUseCase = verifyLoginOtpUseCase;
        this.loginWithPasswordUseCase = loginWithPasswordUseCase;
        this.setPasswordUseCase = setPasswordUseCase;
        this.requestPasswordResetOtpUseCase = requestPasswordResetOtpUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.exposeOtpInResponse = otpProperties.exposeInResponse();
    }

    @PostMapping("/register/otp/request")
    public ApiResponse<Void> requestRegistrationOtp(@Valid @RequestBody RequestOtpRequest request) {
        return otpResponse(
                requestRegistrationOtpUseCase.requestOtp(new RequestRegistrationOtpCommand(request.mobileNumber())));
    }

    @PostMapping("/register/otp/verify")
    public ApiResponse<Void> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        verifyRegistrationOtpUseCase.verifyOtp(
                new VerifyRegistrationOtpCommand(request.mobileNumber(), request.otpCode()));
        return ApiResponse.successVoid();
    }

    @PostMapping("/login/otp/request")
    public ApiResponse<Void> requestLoginOtp(@Valid @RequestBody RequestOtpRequest request) {
        return otpResponse(requestLoginOtpUseCase.requestOtp(new RequestLoginOtpCommand(request.mobileNumber())));
    }

    @PostMapping("/login/otp/verify")
    public ApiResponse<AuthTokenResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthToken token = verifyLoginOtpUseCase.verifyOtp(
                new VerifyLoginOtpCommand(request.mobileNumber(), request.otpCode()));
        return ApiResponse.success(AuthTokenResponse.of(token.accessToken(), token.expiresAt(), token.role()));
    }

    @PostMapping("/login/password")
    public ApiResponse<AuthTokenResponse> loginWithPassword(@Valid @RequestBody LoginWithPasswordRequest request) {
        AuthToken token = loginWithPasswordUseCase.login(new LoginWithPasswordCommand(request.tin(), request.password()));
        return ApiResponse.success(AuthTokenResponse.of(token.accessToken(), token.expiresAt(), token.role()));
    }

    @PostMapping("/password")
    public ApiResponse<Void> setPassword(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @Valid @RequestBody SetPasswordRequest request) {
        setPasswordUseCase.setPassword(
                new SetPasswordCommand(principal.userId(), request.currentPassword(), request.newPassword()));
        return ApiResponse.successVoid();
    }

    @PostMapping("/forgot-password/otp/request")
    public ApiResponse<Void> requestPasswordResetOtp(@Valid @RequestBody RequestPasswordResetOtpRequest request) {
        return otpResponse(requestPasswordResetOtpUseCase.requestOtp(
                new RequestPasswordResetOtpCommand(request.tin(), request.mobileNumber())));
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(new ResetPasswordCommand(
                request.tin(), request.mobileNumber(), request.otpCode(), request.newPassword()));
        return ApiResponse.successVoid();
    }

    @GetMapping("/me")
    public ApiResponse<UserSummaryResponse> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        CurrentUser user = getCurrentUserUseCase.get(principal.userId());
        return ApiResponse.success(new UserSummaryResponse(
                user.mobileNumber(), user.role(), user.profileComplete(), user.tin(), user.passwordSet()));
    }

    /**
     * Every OTP request answers identically whether or not a code was actually issued, so these
     * endpoints cannot be used to probe which accounts exist. The plaintext code is only echoed in
     * environments configured with {@code otp.expose-in-response} (no SMS gateway wired up).
     */
    private ApiResponse<Void> otpResponse(OtpIssued issued) {
        boolean echoCode = exposeOtpInResponse && issued.otpCode() != null;
        return ApiResponse.success(null, echoCode ? "OTP: " + issued.otpCode() : "If the account exists, a code was sent");
    }
}
