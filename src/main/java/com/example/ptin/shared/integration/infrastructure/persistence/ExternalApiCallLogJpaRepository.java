package com.example.ptin.shared.integration.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ExternalApiCallLogJpaRepository extends JpaRepository<ExternalApiCallLogJpaEntity, UUID> {
}
