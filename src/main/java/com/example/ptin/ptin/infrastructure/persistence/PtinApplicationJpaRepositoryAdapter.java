package com.example.ptin.ptin.infrastructure.persistence;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.PtinApplicationSearchCriteria;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
class PtinApplicationJpaRepositoryAdapter implements PtinApplicationRepository {

    private final PtinApplicationJpaRepository jpaRepository;
    private final PtinApplicationPersistenceMapper mapper;

    PtinApplicationJpaRepositoryAdapter(
            PtinApplicationJpaRepository jpaRepository, PtinApplicationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PtinApplication> findById(PtinApplicationId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<PtinApplication> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PtinApplication> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PtinApplication> findByStatus(PtinStatus status) {
        return jpaRepository.findByStatus(status.name()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<PtinApplication> search(PtinApplicationSearchCriteria criteria, Pageable pageable) {
        return jpaRepository
                .findAll(PtinApplicationSpecifications.from(criteria), pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<PtinApplication> findIssuedByTin(String tin) {
        return jpaRepository.findFirstByTinAndStatus(tin, PtinStatus.ISSUED.name()).map(mapper::toDomain);
    }

    @Override
    public PtinApplication save(PtinApplication application) {
        PtinApplicationJpaEntity entity = jpaRepository.findById(application.getId().value())
                .map(existing -> {
                    mapper.updateEntity(existing, application);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(application));
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
