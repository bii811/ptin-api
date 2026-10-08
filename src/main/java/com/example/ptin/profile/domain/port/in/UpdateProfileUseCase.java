package com.example.ptin.profile.domain.port.in;

import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.shared.identity.UserId;

public interface UpdateProfileUseCase {

    Profile update(UpdateProfileCommand command);

    /** Same as {@link #update} but for editing another user's profile; requires ADMIN. */
    Profile updateAsAdmin(UpdateProfileCommand command);

    record UpdateProfileCommand(UserId userId, String firstName, String lastName, String avatarUrl) {
    }
}
