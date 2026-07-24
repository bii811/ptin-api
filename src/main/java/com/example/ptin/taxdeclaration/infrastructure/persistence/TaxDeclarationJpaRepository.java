package com.example.ptin.taxdeclaration.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TaxDeclarationJpaRepository extends JpaRepository<TaxDeclarationJpaEntity, UUID> {

    List<TaxDeclarationJpaEntity> findByUserId(UUID userId);
}
