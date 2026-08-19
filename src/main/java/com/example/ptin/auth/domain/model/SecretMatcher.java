package com.example.ptin.auth.domain.model;

/**
 * Constant-time comparison of a candidate plaintext secret against its stored hash. Lets the domain
 * express "does this code/password match" without depending on Spring Security's PasswordEncoder.
 */
@FunctionalInterface
public interface SecretMatcher {

    boolean matches(String candidateSecret, String hashedSecret);
}
