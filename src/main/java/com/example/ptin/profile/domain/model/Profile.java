package com.example.ptin.profile.domain.model;

import com.example.ptin.shared.identity.UserId;
import java.util.UUID;

public class Profile {

    private final UUID id;
    private final UserId userId;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    private Profile(UUID id, UserId userId, String firstName, String lastName, String avatarUrl) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
    }

    public static Profile createEmpty(UserId userId) {
        return new Profile(UUID.randomUUID(), userId, null, null, null);
    }

    public static Profile reconstitute(
            UUID id, UserId userId, String firstName, String lastName, String avatarUrl) {
        return new Profile(id, userId, firstName, lastName, avatarUrl);
    }

    public void update(String firstName, String lastName, String avatarUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
    }

    public boolean isComplete() {
        return firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank();
    }

    public UUID getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
