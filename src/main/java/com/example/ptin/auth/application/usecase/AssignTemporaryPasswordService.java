package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.model.UserRole;
import com.example.ptin.auth.domain.port.in.AssignTemporaryPasswordUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.security.SecureRandom;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class AssignTemporaryPasswordService implements AssignTemporaryPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(AssignTemporaryPasswordService.class);

    /** No look-alike characters (0/O, 1/l/I): the admin reads this out over the phone. */
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static final int LENGTH = 12;

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    AssignTemporaryPasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String assign(AssignTemporaryPasswordCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        User user = userRepository.findByMobileNumber(mobileNumber)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException(mobileNumber.toString()));
        // Staff have no mobile (so they never match above); this guards any future overlap.
        if (user.getRole() != UserRole.APPLICANT) {
            throw new AccessDeniedException("Temporary passwords can only be assigned to applicants");
        }

        String temporaryPassword = generate();
        user.assignTemporaryPassword(passwordEncoder.encode(temporaryPassword), clock.instant());
        userRepository.save(user);
        log.info("Admin {} assigned a temporary password to user {}", command.actorId(), user.getId());
        return temporaryPassword;
    }

    private String generate() {
        StringBuilder password = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            password.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return password.toString();
    }
}
