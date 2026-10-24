package com.example.ptin.taxdeclaration.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TaxDeclarationJpaRepository extends JpaRepository<TaxDeclarationJpaEntity, UUID> {

    List<TaxDeclarationJpaEntity> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from TaxDeclarationJpaEntity d where d.id = :id")
    Optional<TaxDeclarationJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByTinAndInvoiceNumberAndStatusNot(String tin, String invoiceNumber, String status);

    boolean existsByTinAndInvoiceNumberAndStatusNotAndIdNot(String tin, String invoiceNumber, String status, UUID id);
}
