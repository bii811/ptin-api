package com.example.ptin.taxdeclaration.infrastructure.persistence;

import com.example.ptin.shared.persistence.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tax_declarations")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaxDeclarationJpaEntity extends AuditableJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Setter
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "tin", nullable = false, length = 12)
    private String tin;

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false, length = 8)
    private String invoiceDate;

    @Column(name = "buyer_tin", length = 20)
    private String buyerTin;

    @Column(name = "buyer_full_name", length = 200)
    private String buyerFullName;

    @Column(name = "sale_count", nullable = false, length = 10)
    private String saleCount;

    @Column(name = "supply_amount", nullable = false, length = 28)
    private String supplyAmount;

    @Column(name = "service_fee", length = 28)
    private String serviceFee;

    @Column(name = "excise_amount", length = 28)
    private String exciseAmount;

    @Column(name = "vat_amount", length = 28)
    private String vatAmount;

    @Column(name = "sale_amount", nullable = false, length = 28)
    private String saleAmount;

    @Column(name = "discount_amount", length = 28)
    private String discountAmount;

    @Column(name = "sale_cancel_count", length = 10)
    private String saleCancelCount;

    @Column(name = "sale_cancel_amount", length = 28)
    private String saleCancelAmount;

    @Column(name = "items_json", nullable = false)
    private String itemsJson;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Setter
    @Column(name = "result_code", length = 10)
    private String resultCode;

    @Setter
    @Column(name = "result_message", length = 1000)
    private String resultMessage;

    public TaxDeclarationJpaEntity(
            UUID id,
            UUID userId,
            String status,
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
            String itemsJson,
            Instant submittedAt,
            String resultCode,
            String resultMessage) {
        assignId(id);
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
        this.itemsJson = itemsJson;
        this.submittedAt = submittedAt;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}
