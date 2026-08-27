package com.example.ptin.ptin.infrastructure.persistence;

import com.example.ptin.ptin.domain.model.PtinApplicationSearchCriteria;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

final class PtinApplicationSpecifications {

    private PtinApplicationSpecifications() {
    }

    static Specification<PtinApplicationJpaEntity> from(PtinApplicationSearchCriteria criteria) {
        List<Specification<PtinApplicationJpaEntity>> specs = new ArrayList<>();
        if (criteria.status() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), criteria.status().name()));
        }
        if (StringUtils.hasText(criteria.tin())) {
            specs.add((root, query, cb) ->
                    cb.like(root.get("tin"), "%" + criteria.tin().trim() + "%"));
        }
        if (StringUtils.hasText(criteria.applicantName())) {
            String pattern = "%" + criteria.applicantName().trim().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("taxrGvNm")), pattern),
                    cb.like(cb.lower(root.get("taxrFamNm")), pattern)));
        }
        if (criteria.submittedFrom() != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("submittedAt"), criteria.submittedFrom()));
        }
        if (criteria.submittedTo() != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("submittedAt"), criteria.submittedTo()));
        }
        return specs.stream().reduce(Specification.unrestricted(), Specification::and);
    }
}
