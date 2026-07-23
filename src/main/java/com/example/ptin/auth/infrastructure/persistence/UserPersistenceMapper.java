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
                user.getMobileNumber().value(),
                user.getRole().name(),
                user.getStatus().name());
    }

    void updateEntity(UserJpaEntity entity, User user) {
        entity.setStatus(user.getStatus().name());
    }

    User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                new UserId(entity.getId()),
                new MobileNumber(entity.getMobileNumber()),
                UserRole.valueOf(entity.getRole()),
                UserStatus.valueOf(entity.getStatus()));
    }
}
