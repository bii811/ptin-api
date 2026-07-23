package com.example.ptin.profile.infrastructure.rest.response;

import com.example.ptin.profile.domain.model.Profile;

public record ProfileResponse(String firstName, String lastName, String avatarUrl, boolean complete) {

    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getFirstName(), profile.getLastName(), profile.getAvatarUrl(), profile.isComplete());
    }
}
