package com.example.ptin.auth.domain.port.out;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import java.util.Optional;

public interface OtpChallengeRepository {

    Optional<OtpChallenge> findActiveChallenge(MobileNumber mobileNumber, OtpPurpose purpose);

    OtpChallenge save(OtpChallenge challenge);
}
