package com.example.ptin.auth.domain.port.out;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OtpChallengeRepository {

    /**
     * The newest unconsumed challenge for this number/purpose, taken under a write lock so two
     * concurrent verification attempts cannot both read the same attempt count and overwrite each
     * other's increment. Must be called inside a transaction.
     */
    Optional<OtpChallenge> lockActiveChallenge(MobileNumber mobileNumber, OtpPurpose purpose);

    /** Every unconsumed challenge for this number/purpose, so a new issue can retire the old ones. */
    List<OtpChallenge> findUnconsumed(MobileNumber mobileNumber, OtpPurpose purpose);

    /** Issue timestamps within the throttling window, newest first. Backs cooldown + rate limiting. */
    List<Instant> findIssueTimestampsSince(MobileNumber mobileNumber, OtpPurpose purpose, Instant since);

    OtpChallenge save(OtpChallenge challenge);
}
