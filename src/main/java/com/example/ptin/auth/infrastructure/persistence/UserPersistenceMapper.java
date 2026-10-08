package com.example.ptin.auth.infrastructure.persistence;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.model.UserRole;
import com.example.ptin.auth.domain.model.UserStatus;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceMapper {

    UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId().value(),
                user.getMobileNumber() == null ? null : user.getMobileNumber().value(),
                user.getUsername(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getPasswordHash(),
                user.getPasswordUpdatedAt(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.isMustChangePassword());
    }

    void updateEntity(UserJpaEntity entity, User user) {
        entity.setStatus(user.getStatus().name());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPasswordUpdatedAt(user.getPasswordUpdatedAt());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLockedUntil(user.getLockedUntil());
        entity.setMustChangePassword(user.isMustChangePassword());
    }

    User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                new UserId(entity.getId()),
                entity.getMobileNumber() == null ? null : new MobileNumber(entity.getMobileNumber()),
                entity.getUsername(),
                UserRole.valueOf(entity.getRole()),
                UserStatus.valueOf(entity.getStatus()),
                entity.getPasswordHash(),
                entity.getPasswordUpdatedAt(),
                entity.getFailedLoginAttempts(),
                entity.getLockedUntil(),
                entity.isMustChangePassword());
    }
}
