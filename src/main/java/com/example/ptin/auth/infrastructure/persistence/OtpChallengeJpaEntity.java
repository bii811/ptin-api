package com.example.ptin.auth.infrastructure.persistence;

import com.example.ptin.shared.persistence.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "otp_challenges")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OtpChallengeJpaEntity extends AuditableJpaEntity {

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "purpose", nullable = false, length = 20)
    private String purpose;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Setter
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Setter
    @Column(name = "consumed_at")
    private Instant consumedAt;

    public OtpChallengeJpaEntity(
            UUID id,
            String mobileNumber,
            String purpose,
            String codeHash,
            Instant expiresAt,
            int attemptCount,
            int maxAttempts,
            Instant consumedAt) {
        assignId(id);
        this.mobileNumber = mobileNumber;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.attemptCount = attemptCount;
        this.maxAttempts = maxAttempts;
        this.consumedAt = consumedAt;
    }
}
