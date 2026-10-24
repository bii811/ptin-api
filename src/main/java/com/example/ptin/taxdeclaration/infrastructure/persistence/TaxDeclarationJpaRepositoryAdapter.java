package com.example.ptin.taxdeclaration.infrastructure.persistence;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationStatus;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class TaxDeclarationJpaRepositoryAdapter implements TaxDeclarationRepository {

    private final TaxDeclarationJpaRepository jpaRepository;
    private final TaxDeclarationPersistenceMapper mapper;

    TaxDeclarationJpaRepositoryAdapter(TaxDeclarationJpaRepository jpaRepository, TaxDeclarationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<TaxDeclaration> findById(TaxDeclarationId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<TaxDeclaration> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<TaxDeclaration> findByIdForUpdate(TaxDeclarationId id) {
        return jpaRepository.findByIdForUpdate(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsActive(String tin, String invoiceNumber, TaxDeclarationId excludeId) {
        String failed = TaxDeclarationStatus.FAILED.name();
        return excludeId == null
                ? jpaRepository.existsByTinAndInvoiceNumberAndStatusNot(tin, invoiceNumber, failed)
                : jpaRepository.existsByTinAndInvoiceNumberAndStatusNotAndIdNot(
                        tin, invoiceNumber, failed, excludeId.value());
    }

    @Override
    public TaxDeclaration save(TaxDeclaration declaration) {
        TaxDeclarationJpaEntity entity = jpaRepository.findById(declaration.getId().value())
                .map(existing -> {
                    mapper.updateEntity(existing, declaration);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(declaration));
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
