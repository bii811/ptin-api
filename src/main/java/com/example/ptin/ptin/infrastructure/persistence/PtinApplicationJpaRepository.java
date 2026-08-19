package com.example.ptin.ptin.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PtinApplicationJpaRepository extends JpaRepository<PtinApplicationJpaEntity, UUID> {

    List<PtinApplicationJpaEntity> findByUserId(UUID userId);

    List<PtinApplicationJpaEntity> findByStatus(String status);

    Optional<PtinApplicationJpaEntity> findFirstByTinAndStatus(String tin, String status);
}
