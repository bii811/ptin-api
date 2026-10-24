package com.example.ptin.taxdeclaration.domain.model;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.shared.validation.FieldValidation;
import com.example.ptin.taxdeclaration.domain.exception.TaxDeclarationNotRetryableException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Getter
public class TaxDeclaration {

    static final Duration STALE_PENDING = Duration.ofMinutes(2);

    private final TaxDeclarationId id;
    private final UserId userId;
    private TaxDeclarationStatus status;

    private final String tin;
    private final String invoiceNumber;
    private final String invoiceDate;
    private final String buyerTin;
    private final String buyerFullName;
    private final String saleCount;
    private final String supplyAmount;
    private final String serviceFee;
    private final String exciseAmount;
    private final String vatAmount;
    private final String saleAmount;
    private final String discountAmount;
    private final String saleCancelCount;
    private final String saleCancelAmount;
    private final List<TaxInvoiceLineItem> items;

    private final Instant submittedAt;
    private String resultCode;
    private String resultMessage;
    private int retryCount;
    /** Start of the latest attempt; a PENDING row older than {@link #STALE_PENDING} is treated as an abandoned attempt. */
    private Instant lastAttemptAt;

    private TaxDeclaration(
            TaxDeclarationId id,
            UserId userId,
            TaxDeclarationStatus status,
            String tin,
            String invoiceNumber,
            String invoiceDate,
            String buyerTin,
            String buyerFullName,
            String saleCount,
            String supplyAmount,
            String serviceFee,
            String exciseAmount,
            String vatAmount,
            String saleAmount,
            String discountAmount,
            String saleCancelCount,
            String saleCancelAmount,
            List<TaxInvoiceLineItem> items,
            Instant submittedAt,
            String resultCode,
            String resultMessage,
            int retryCount,
            Instant lastAttemptAt) {
        FieldValidation.requireNotBlank("tin", tin);
        FieldValidation.requireMaxLength("tin", tin, 12);
        FieldValidation.requireNotBlank("invoiceNumber", invoiceNumber);
        FieldValidation.requireMaxLength("invoiceNumber", invoiceNumber, 50);
        FieldValidation.requireNotBlank("invoiceDate", invoiceDate);
        FieldValidation.requireMaxLength("invoiceDate", invoiceDate, 8);
        FieldValidation.requireMaxLength("buyerTin", buyerTin, 20);
        FieldValidation.requireMaxLength("buyerFullName", buyerFullName, 200);
        FieldValidation.requireNotBlank("saleCount", saleCount);
        FieldValidation.requireMaxLength("saleCount", saleCount, 10);
        FieldValidation.requireNotBlank("supplyAmount", supplyAmount);
        FieldValidation.requireMaxLength("supplyAmount", supplyAmount, 28);
        FieldValidation.requireMaxLength("serviceFee", serviceFee, 28);
        FieldValidation.requireMaxLength("exciseAmount", exciseAmount, 28);
        FieldValidation.requireMaxLength("vatAmount", vatAmount, 28);
        FieldValidation.requireNotBlank("saleAmount", saleAmount);
        FieldValidation.requireMaxLength("saleAmount", saleAmount, 28);
        FieldValidation.requireMaxLength("discountAmount", discountAmount, 28);
        FieldValidation.requireMaxLength("saleCancelCount", saleCancelCount, 10);
        FieldValidation.requireMaxLength("saleCancelAmount", saleCancelAmount, 28);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }

        this.id = id;
        this.userId = userId;
        this.status = status;
        this.tin = tin;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.buyerTin = buyerTin;
        this.buyerFullName = buyerFullName;
        this.saleCount = saleCount;
        this.supplyAmount = supplyAmount;
        this.serviceFee = serviceFee;
        this.exciseAmount = exciseAmount;
        this.vatAmount = vatAmount;
        this.saleAmount = saleAmount;
        this.discountAmount = discountAmount;
        this.saleCancelCount = saleCancelCount;
        this.saleCancelAmount = saleCancelAmount;
        this.items = List.copyOf(items);
        this.submittedAt = submittedAt;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.retryCount = retryCount;
        this.lastAttemptAt = lastAttemptAt;
    }

    public static TaxDeclaration declare(
            UserId userId,
            String tin,
            String invoiceNumber,
            String invoiceDate,
            String buyerTin,
            String buyerFullName,
            String saleCount,
            String supplyAmount,
            String serviceFee,
            String exciseAmount,
            String vatAmount,
            String saleAmount,
            String discountAmount,
            String saleCancelCount,
            String saleCancelAmount,
            List<TaxInvoiceLineItem> items) {
        Instant now = Instant.now();
        return new TaxDeclaration(
                TaxDeclarationId.generate(),
                userId,
                TaxDeclarationStatus.PENDING,
                tin,
                invoiceNumber,
                invoiceDate,
                buyerTin,
                buyerFullName,
                saleCount,
                supplyAmount,
                serviceFee,
                exciseAmount,
                vatAmount,
                saleAmount,
                discountAmount,
                saleCancelCount,
                saleCancelAmount,
                items,
                now,
                null,
                null,
                0,
                now);
    }

    public static TaxDeclaration reconstitute(
            TaxDeclarationId id,
            UserId userId,
            TaxDeclarationStatus status,
            String tin,
            String invoiceNumber,
            String invoiceDate,
            String buyerTin,
            String buyerFullName,
            String saleCount,
            String supplyAmount,
            String serviceFee,
            String exciseAmount,
            String vatAmount,
            String saleAmount,
            String discountAmount,
            String saleCancelCount,
            String saleCancelAmount,
            List<TaxInvoiceLineItem> items,
            Instant submittedAt,
            String resultCode,
            String resultMessage,
            int retryCount,
            Instant lastAttemptAt) {
        return new TaxDeclaration(
                id, userId, status, tin, invoiceNumber, invoiceDate, buyerTin, buyerFullName, saleCount, supplyAmount,
                serviceFee, exciseAmount, vatAmount, saleAmount, discountAmount, saleCancelCount, saleCancelAmount,
                items, submittedAt, resultCode, resultMessage, retryCount, lastAttemptAt);
    }

    public void markSubmitted(String resultCode, String resultMessage) {
        requirePending();
        this.status = TaxDeclarationStatus.SUBMITTED;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public void markFailed(String resultCode, String resultMessage) {
        requirePending();
        this.status = TaxDeclarationStatus.FAILED;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public void markUnknown(String resultMessage) {
        requirePending();
        this.status = TaxDeclarationStatus.UNKNOWN;
        this.resultCode = null;
        this.resultMessage = resultMessage;
    }

    /** Whether {@link #startRetry} would be accepted now. */
    public boolean isRetryable(Instant now) {
        return status == TaxDeclarationStatus.FAILED
                || status == TaxDeclarationStatus.UNKNOWN
                || (status == TaxDeclarationStatus.PENDING && !now.isBefore(lastAttemptAt.plus(STALE_PENDING)));
    }

    /** Claims the declaration for another attempt: back to PENDING with the attempt counted. */
    public void startRetry(Instant now) {
        if (!isRetryable(now)) {
            throw new TaxDeclarationNotRetryableException(id, status);
        }
        this.status = TaxDeclarationStatus.PENDING;
        this.retryCount++;
        this.lastAttemptAt = now;
    }

    /** 1-based attempt number of the submission currently in flight (the audit log records it). */
    public int attemptNo() {
        return retryCount + 1;
    }

    private void requirePending() {
        if (status != TaxDeclarationStatus.PENDING) {
            throw new IllegalStateException("Tax declaration " + id + " has already been recorded as " + status);
        }
    }
}
