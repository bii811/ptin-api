package com.example.ptin.profile.infrastructure.event;

import com.example.ptin.auth.domain.event.UserRegisteredEvent;
import com.example.ptin.profile.domain.port.in.CreateInitialProfileUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class UserRegisteredEventListener {

    private final CreateInitialProfileUseCase createInitialProfileUseCase;

    UserRegisteredEventListener(CreateInitialProfileUseCase createInitialProfileUseCase) {
        this.createInitialProfileUseCase = createInitialProfileUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        createInitialProfileUseCase.createEmpty(event.userId());
    }
}
