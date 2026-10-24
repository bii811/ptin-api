package com.example.ptin.taxdeclaration.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.exception.DuplicateTaxInvoiceException;
import com.example.ptin.taxdeclaration.domain.exception.TaxDeclarationNotRetryableException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationStatus;
import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;
import com.example.ptin.taxdeclaration.domain.port.in.DeclareTaxUseCase.DeclareTaxCommand;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class TaxDeclarationFlowTest {

    /** In-memory repository; remembers nothing but the last saved copy, like a DB row would. */
    static class FakeRepo implements TaxDeclarationRepository {
        final Map<TaxDeclarationId, TaxDeclaration> rows = new HashMap<>();

        public Optional<TaxDeclaration> findById(TaxDeclarationId id) { return Optional.ofNullable(rows.get(id)); }
        public List<TaxDeclaration> findByUserId(UserId userId) { return new ArrayList<>(rows.values()); }
        public Optional<TaxDeclaration> findByIdForUpdate(TaxDeclarationId id) { return findById(id); }
        public boolean existsActive(String tin, String invoiceNumber, TaxDeclarationId excludeId) {
            return rows.values().stream().anyMatch(d -> d.getTin().equals(tin)
                    && d.getInvoiceNumber().equals(invoiceNumber)
                    && d.getStatus() != TaxDeclarationStatus.FAILED
                    && !d.getId().equals(excludeId));
        }
        public TaxDeclaration save(TaxDeclaration d) { rows.put(d.getId(), d); return d; }
    }

    /** Scripted TaxRIS; also records what the repository held when the call was made. */
    static class FakeClient implements TaxMediationClient {
        final Queue<SubmissionResult> script = new ArrayDeque<>();
        final List<Integer> attemptNos = new ArrayList<>();
        final FakeRepo repo;
        TaxDeclarationStatus statusInDbDuringCall;

        FakeClient(FakeRepo repo) { this.repo = repo; }

        public SubmissionResult submit(TaxDeclaration d) {
            statusInDbDuringCall = repo.rows.get(d.getId()) == null ? null : repo.rows.get(d.getId()).getStatus();
            attemptNos.add(d.attemptNo());
            return script.remove();
        }
    }

    private final UserId user = UserId.generate();
    private FakeRepo repo;
    private FakeClient client;
    private Instant now;
    private DeclareTaxService declare;
    private RetryTaxDeclarationService retry;

    @BeforeEach
    void setUp() {
        repo = new FakeRepo();
        client = new FakeClient(repo);
        now = Instant.parse("2026-01-01T00:00:00Z");
        Clock clock = new Clock() {
            public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId zone) { return this; }
            public Instant instant() { return now; }
        };
        var coordinator = new TaxSubmissionCoordinator(client, repo);
        declare = new DeclareTaxService(u -> Optional.of("100000000001"), repo, coordinator);
        retry = new RetryTaxDeclarationService(new TaxDeclarationTransactionalGateway(repo, clock), coordinator);
    }

    private DeclareTaxCommand cmd(String invoice) {
        var item = new TaxInvoiceLineItem("8517", "Phone", "1", "EA", "100", "100", "0", "10", "110");
        return new DeclareTaxCommand(user, invoice, "20260101", null, null, "1", "100", null, null, "10", "110",
                null, null, null, List.of(item));
    }

    private TaxDeclarationStatus declareWith(String invoice, TaxMediationClient.SubmissionResult result) {
        client.script.add(result);
        return repo.rows.get(declare.declare(cmd(invoice))).getStatus();
    }

    @Test
    void pendingRowIsCommittedBeforeTaxRisIsCalled() {
        assertEquals(TaxDeclarationStatus.SUBMITTED,
                declareWith("INV-1", TaxMediationClient.SubmissionResult.success("000", "ok")));
        assertEquals(TaxDeclarationStatus.PENDING, client.statusInDbDuringCall);
        assertEquals(List.of(1), client.attemptNos);
    }

    @Test
    void rejectionIsFailedThenRetrySucceedsAsSecondAttempt() {
        declareWith("INV-1", TaxMediationClient.SubmissionResult.failure("E01", "bad"));
        TaxDeclaration failed = repo.rows.values().iterator().next();
        assertEquals(TaxDeclarationStatus.FAILED, failed.getStatus());
        assertEquals("E01", failed.getResultCode());

        client.script.add(TaxMediationClient.SubmissionResult.success("000", "ok"));
        TaxDeclaration retried = retry.retry(failed.getId(), user);

        assertEquals(TaxDeclarationStatus.SUBMITTED, retried.getStatus());
        assertEquals(1, retried.getRetryCount());
        assertEquals(List.of(1, 2), client.attemptNos);
    }

    @Test
    void transportErrorIsUnknownAndRetryable() {
        assertEquals(TaxDeclarationStatus.UNKNOWN,
                declareWith("INV-1", TaxMediationClient.SubmissionResult.unknown("timeout")));
        TaxDeclaration d = repo.rows.values().iterator().next();
        assertTrue(d.isRetryable(now));
        assertEquals(null, d.getResultCode());
    }

    @Test
    void submittedAndFreshPendingCannotBeRetriedButAbandonedPendingCan() {
        declareWith("INV-1", TaxMediationClient.SubmissionResult.success("000", "ok"));
        TaxDeclaration submitted = repo.rows.values().iterator().next();
        assertThrows(TaxDeclarationNotRetryableException.class, () -> retry.retry(submitted.getId(), user));

        TaxDeclaration pending = TaxDeclaration.reconstitute(submitted.getId(), user, TaxDeclarationStatus.PENDING,
                "100000000001", "INV-1", "20260101", null, null, "1", "100", null, null, "10", "110", null, null,
                null, submitted.getItems(), now, null, null, 0, now);
        assertFalse(pending.isRetryable(now.plusSeconds(30)));
        assertTrue(pending.isRetryable(now.plus(Duration.ofMinutes(3))));
    }

    @Test
    void secondDeclarationForALiveInvoiceIsRefusedButAFailedOneMayBeFiledAgain() {
        declareWith("INV-1", TaxMediationClient.SubmissionResult.success("000", "ok"));
        assertThrows(DuplicateTaxInvoiceException.class, () -> declare.declare(cmd("INV-1")));

        declareWith("INV-2", TaxMediationClient.SubmissionResult.failure("E01", "bad"));
        declareWith("INV-2", TaxMediationClient.SubmissionResult.success("000", "ok"));
    }

    @Test
    void retryingAnOldFailureIsRefusedOnceANewerDeclarationForTheInvoiceIsLive() {
        declareWith("INV-2", TaxMediationClient.SubmissionResult.failure("E01", "bad"));
        TaxDeclaration old = repo.rows.values().iterator().next();
        declareWith("INV-2", TaxMediationClient.SubmissionResult.success("000", "ok"));

        assertThrows(DuplicateTaxInvoiceException.class, () -> retry.retry(old.getId(), user));
    }

    @Test
    void onlyTheOwnerMayRetry() {
        declareWith("INV-1", TaxMediationClient.SubmissionResult.failure("E01", "bad"));
        TaxDeclaration d = repo.rows.values().iterator().next();
        assertThrows(AccessDeniedException.class, () -> retry.retry(d.getId(), UserId.generate()));
    }
}
