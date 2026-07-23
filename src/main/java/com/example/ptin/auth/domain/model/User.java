package com.example.ptin.auth.domain.model;

import com.example.ptin.shared.identity.UserId;

public class User {

    private final UserId id;
    private final MobileNumber mobileNumber;
    private final UserRole role;
    private UserStatus status;

    private User(UserId id, MobileNumber mobileNumber, UserRole role, UserStatus status) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.status = status;
    }

    public static User register(MobileNumber mobileNumber) {
        // Public self-registration can only ever create an APPLICANT; AUTHORIZER accounts are provisioned out-of-band.
        return new User(UserId.generate(), mobileNumber, UserRole.APPLICANT, UserStatus.PENDING_VERIFICATION);
    }

    public static User reconstitute(UserId id, MobileNumber mobileNumber, UserRole role, UserStatus status) {
        return new User(id, mobileNumber, role, status);
    }

    public void activate() {
        if (status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
    }

    public UserId getId() {
        return id;
    }

    public MobileNumber getMobileNumber() {
        return mobileNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
