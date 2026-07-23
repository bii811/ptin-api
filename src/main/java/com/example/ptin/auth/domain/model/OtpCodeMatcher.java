package com.example.ptin.auth.domain.model;

@FunctionalInterface
public interface OtpCodeMatcher {
    boolean matches(String candidateCode, String hashedCode);
}
