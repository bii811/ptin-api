package com.example.ptin.auth.domain.port.in;

import com.example.ptin.shared.identity.UserId;

public interface GetCurrentUserUseCase {

    CurrentUser get(UserId userId);

    /**
     * {@code tin} is null until one is issued; together with {@code passwordSet} it tells the client
     * which login options this account currently has.
     */
    record CurrentUser(String mobileNumber, String role, boolean profileComplete, String tin, boolean passwordSet) {
    }
}
