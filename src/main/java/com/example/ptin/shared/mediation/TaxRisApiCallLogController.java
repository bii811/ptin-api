package com.example.ptin.shared.mediation;

import com.example.ptin.shared.web.ApiResponse;
import com.example.ptin.shared.web.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only view of the TaxRIS audit log for staff. {@code HASH_KEY} is already redacted at write time. */
@RestController
@RequestMapping("/api/v1/admin/taxris-logs")
class TaxRisApiCallLogController {

    private final TaxRisApiCallLogJpaRepository repository;

    TaxRisApiCallLogController(TaxRisApiCallLogJpaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PageResponse<Item>> list(
            @RequestParam(required = false) String referenceId,
            @PageableDefault(size = 20, sort = "calledAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TaxRisApiCallLog> page = referenceId == null
                ? repository.findAll(pageable) : repository.findByReferenceId(referenceId, pageable);
        return ApiResponse.success(PageResponse.from(page, Item::from));
    }

    record Item(UUID id, String functionSource, String referenceId, String triggeredBy, String endpointUrl,
            String requestHeaders, String requestBody, String responseBody, String resultCode,
            String resultMessage, int attemptNo, Instant calledAt) {

        static Item from(TaxRisApiCallLog l) {
            return new Item(l.getId(), l.getFunctionSource(), l.getReferenceId(), l.getTriggeredBy(),
                    l.getEndpointUrl(), l.getRequestHeaders(), l.getRequestBody(), l.getResponseBody(),
                    l.getResultCode(), l.getResultMessage(), l.getAttemptNo(), l.getCalledAt());
        }
    }
}
