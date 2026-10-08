package com.example.ptin.auth.domain.model;

public enum UserRole {
    APPLICANT,
    /** Staff: reviews/edits applications and users, resets passwords. Provisioned out-of-band. */
    ADMIN,
    /** Inherits everything ADMIN can do (see RoleHierarchy in SecurityConfig). Provisioned out-of-band. */
    SUPERADMIN
}
