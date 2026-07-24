package com.example.ptin.profile.infrastructure.persistence;

import com.example.ptin.shared.persistence.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "profiles")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileJpaEntity extends AuditableJpaEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Setter
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Setter
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Setter
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    public ProfileJpaEntity(UUID id, UUID userId, String firstName, String lastName, String avatarUrl) {
        assignId(id);
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
    }
}
