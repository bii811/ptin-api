package com.example.ptin.auth.infrastructure.persistence;

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
@Table(name = "users")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity extends AuditableJpaEntity {

    @Column(name = "mobile_number", nullable = false, unique = true, length = 20)
    private String mobileNumber;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Setter
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public UserJpaEntity(UUID id, String mobileNumber, String role, String status) {
        assignId(id);
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.status = status;
    }
}
