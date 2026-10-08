package com.example.ptin.auth.infrastructure.rest;

import com.example.ptin.auth.config.OtpProperties;
import com.example.ptin.auth.domain.port.in.GetCurrentUserUseCase;
import com.example.ptin.auth.domain.port.in.GetCurrentUserUseCase.CurrentUser;
import com.example.ptin.auth.domain.port.in.LoginWithPasswordUseCase;
import com.example.ptin.auth.domain.port.in.LoginWithPasswordUseCase.LoginWithPasswordCommand;
import com.example.ptin.auth.domain.port.in.CompleteRegistrationUseCase;
import com.example.ptin.auth.domain.port.in.CompleteRegistrationUseCase.CompleteRegistrationCommand;
import com.example.ptin.auth.domain.port.in.FlowToken;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestPasswordResetOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestPasswordResetOtpUseCase.RequestPasswordResetOtpCommand;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase.RequestRegistrationOtpCommand;
import com.example.ptin.auth.domain.port.in.ResetPasswordUseCase;
import com.example.ptin.auth.domain.port.in.ResetPasswordUseCase.ResetPasswordCommand;
import com.example.ptin.auth.domain.port.in.SetPasswordUseCase;
import com.example.ptin.auth.domain.port.in.SetPasswordUseCase.SetPasswordCommand;
import com.example.ptin.auth.domain.port.in.VerifyPasswordResetOtpUseCase;
import com.example.ptin.auth.domain.port.in.VerifyPasswordResetOtpUseCase.VerifyPasswordResetOtpCommand;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase.VerifyRegistrationOtpCommand;
import com.example.ptin.auth.infrastructure.rest.request.LoginWithPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.request.RequestOtpRequest;
import com.example.ptin.auth.infrastructure.rest.request.CompleteRegistrationRequest;
import com.example.ptin.auth.infrastructure.rest.request.ResetPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.request.SetPasswordRequest;
import com.example.ptin.auth.infrastructure.rest.request.VerifyOtpRequest;
import com.example.ptin.auth.infrastructure.rest.response.AuthTokenResponse;
import com.example.ptin.auth.infrastructure.rest.response.FlowTokenResponse;
import com.example.ptin.auth.infrastructure.rest.response.PasswordStrengthResponse;
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
 * Every multi-step flow is a small state machine: an OTP is exchanged for a short-lived signed token,
 * and only that token unlocks the final step.
 *
 * <ul>
 *   <li>register: {@code otp/request} -> {@code otp/verify} (registration token) -> {@code complete};
 *   <li>login: mobile number, or the issued TIN, plus password;
 *   <li>forgotten password: {@code otp/request} -> {@code otp/verify} (reset token) -> {@code reset}.
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RequestRegistrationOtpUseCase requestRegistrationOtpUseCase;
    private final VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase;
    private final CompleteRegistrationUseCase completeRegistrationUseCase;
    private final LoginWithPasswordUseCase loginWithPasswordUseCase;
    private final SetPasswordUseCase setPasswordUseCase;
    private final RequestPasswordResetOtpUseCase requestPasswordResetOtpUseCase;
    private final VerifyPasswordResetOtpUseCase verifyPasswordResetOtpUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final boolean exposeOtpInResponse;

    public AuthController(
            RequestRegistrationOtpUseCase requestRegistrationOtpUseCase,
            VerifyRegistrationOtpUseCase verifyRegistrationOtpUseCase,
            CompleteRegistrationUseCase completeRegistrationUseCase,
            LoginWithPasswordUseCase loginWithPasswordUseCase,
            SetPasswordUseCase setPasswordUseCase,
            RequestPasswordResetOtpUseCase requestPasswordResetOtpUseCase,
            VerifyPasswordResetOtpUseCase verifyPasswordResetOtpUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            OtpProperties otpProperties) {
        this.requestRegistrationOtpUseCase = requestRegistrationOtpUseCase;
        this.verifyRegistrationOtpUseCase = verifyRegistrationOtpUseCase;
        this.completeRegistrationUseCase = completeRegistrationUseCase;
        this.loginWithPasswordUseCase = loginWithPasswordUseCase;
        this.setPasswordUseCase = setPasswordUseCase;
        this.requestPasswordResetOtpUseCase = requestPasswordResetOtpUseCase;
        this.verifyPasswordResetOtpUseCase = verifyPasswordResetOtpUseCase;
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
    public ApiResponse<FlowTokenResponse> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return flowToken(verifyRegistrationOtpUseCase.verifyOtp(
                new VerifyRegistrationOtpCommand(request.mobileNumber(), request.otpCode())));
    }

    @PostMapping("/register/complete")
    public ApiResponse<PasswordStrengthResponse> completeRegistration(
            @Valid @RequestBody CompleteRegistrationRequest request) {
        return ApiResponse.success(new PasswordStrengthResponse(completeRegistrationUseCase.complete(
                new CompleteRegistrationCommand(request.registrationToken(), request.password()))));
    }

    @PostMapping("/login/password")
    public ApiResponse<AuthTokenResponse> loginWithPassword(@Valid @RequestBody LoginWithPasswordRequest request) {
        return ApiResponse.success(AuthTokenResponse.from(loginWithPasswordUseCase.login(
                new LoginWithPasswordCommand(
                        request.mobileNumber(), request.tin(), request.username(), request.password()))));
    }

    @PostMapping("/password")
    public ApiResponse<Void> setPassword(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @Valid @RequestBody SetPasswordRequest request) {
        setPasswordUseCase.setPassword(
                new SetPasswordCommand(principal.userId(), request.currentPassword(), request.newPassword()));
        return ApiResponse.successVoid();
    }

    @PostMapping("/forgot-password/otp/request")
    public ApiResponse<Void> requestPasswordResetOtp(@Valid @RequestBody RequestOtpRequest request) {
        return otpResponse(
                requestPasswordResetOtpUseCase.requestOtp(new RequestPasswordResetOtpCommand(request.mobileNumber())));
    }

    @PostMapping("/forgot-password/otp/verify")
    public ApiResponse<FlowTokenResponse> verifyPasswordResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return flowToken(verifyPasswordResetOtpUseCase.verifyOtp(
                new VerifyPasswordResetOtpCommand(request.mobileNumber(), request.otpCode())));
    }

    @PostMapping("/forgot-password/reset")
    public ApiResponse<PasswordStrengthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.success(new PasswordStrengthResponse(
                resetPasswordUseCase.resetPassword(new ResetPasswordCommand(request.resetToken(), request.newPassword()))));
    }

    @GetMapping("/me")
    public ApiResponse<UserSummaryResponse> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        CurrentUser user = getCurrentUserUseCase.get(principal.userId());
        return ApiResponse.success(new UserSummaryResponse(
                user.mobileNumber(), user.username(), user.role(), user.profileComplete(), user.tin(), user.passwordSet()));
    }

    private ApiResponse<FlowTokenResponse> flowToken(FlowToken token) {
        return ApiResponse.success(new FlowTokenResponse(token.token(), token.expiresAt()));
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
