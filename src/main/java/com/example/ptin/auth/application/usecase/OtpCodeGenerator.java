package com.example.ptin.auth.application.usecase;

import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class OtpCodeGenerator {

    private final SecureRandom random = new SecureRandom();
    private final int codeLength;

    OtpCodeGenerator(@Value("${otp.code-length}") int codeLength) {
        this.codeLength = codeLength;
    }

    String generate() {
        StringBuilder code = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
