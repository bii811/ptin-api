package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.GetCurrentUserUseCase;
import com.example.ptin.auth.domain.port.out.ProfileCompletionLookupPort;
import com.example.ptin.auth.domain.port.out.TaxpayerTinLookupPort;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final TaxpayerTinLookupPort taxpayerTinLookup;
    private final ProfileCompletionLookupPort profileCompletionLookup;

    GetCurrentUserService(
            UserRepository userRepository,
            TaxpayerTinLookupPort taxpayerTinLookup,
            ProfileCompletionLookupPort profileCompletionLookup) {
        this.userRepository = userRepository;
        this.taxpayerTinLookup = taxpayerTinLookup;
        this.profileCompletionLookup = profileCompletionLookup;
    }

    @Override
    public CurrentUser get(UserId userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return new CurrentUser(
                user.getMobileNumber().value(),
                user.getRole().name(),
                profileCompletionLookup.isProfileComplete(userId),
                taxpayerTinLookup.findIssuedTin(userId).orElse(null),
                user.hasPassword());
    }
}
