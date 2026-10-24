package com.example.ptin.ptin.domain.model;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotEditableException;
import com.example.ptin.ptin.domain.exception.PtinApplicationNotPendingException;
import com.example.ptin.ptin.domain.exception.PtinApplicationNotRetryableException;
import com.example.ptin.shared.identity.UserId;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PtinApplication {

    private final PtinApplicationId id;
    private final UserId userId;
    private PtinStatus status;
    private PtinType ptinType;

    private PersonalInfo personalInfo;
    private ContactInfo contactInfo;
    private AddressInfo addressInfo;
    private EmploymentInfo employmentInfo;
    private IncomeInfo incomeInfo;
    private FinancialInfo financialInfo;
    private TaxpayerIdentificationNumber tin;

    private final Instant submittedAt;
    private UserId approvedBy;
    private Instant approvedAt;
    private UserId rejectedBy;
    private Instant rejectedAt;
    private String rejectionReason;
    private Instant issuedAt;
    private String failureReason;
    private Instant failedAt;
    private int retryCount;

    public static PtinApplication submit(
            UserId userId,
            PtinType ptinType,
            PersonalInfo personalInfo,
            ContactInfo contactInfo,
            AddressInfo addressInfo,
            EmploymentInfo employmentInfo,
            IncomeInfo incomeInfo,
            FinancialInfo financialInfo) {
        validateType(ptinType, personalInfo);
        return new PtinApplication(
                PtinApplicationId.generate(),
                userId,
                PtinStatus.PENDING_APPROVAL,
                ptinType,
                personalInfo,
                contactInfo,
                addressInfo,
                employmentInfo,
                incomeInfo,
                financialInfo,
                null,
                Instant.now(),
                null, null, null, null, null, null, null, null, 0);
    }

    public static PtinApplication reconstitute(
            PtinApplicationId id,
            UserId userId,
            PtinStatus status,
            PtinType ptinType,
            PersonalInfo personalInfo,
            ContactInfo contactInfo,
            AddressInfo addressInfo,
            EmploymentInfo employmentInfo,
            IncomeInfo incomeInfo,
            FinancialInfo financialInfo,
            TaxpayerIdentificationNumber tin,
            Instant submittedAt,
            UserId approvedBy,
            Instant approvedAt,
            UserId rejectedBy,
            Instant rejectedAt,
            String rejectionReason,
            Instant issuedAt,
            String failureReason,
            Instant failedAt,
            int retryCount) {
        return new PtinApplication(
                id, userId, status, ptinType, personalInfo, contactInfo, addressInfo, employmentInfo, incomeInfo,
                financialInfo, tin, submittedAt, approvedBy, approvedAt, rejectedBy, rejectedAt, rejectionReason,
                issuedAt, failureReason, failedAt, retryCount);
    }

    /** Admin correction of the form. Only before approval, or after a failed issuance (fix data, then retry). */
    public void edit(
            PtinType ptinType,
            PersonalInfo personalInfo,
            ContactInfo contactInfo,
            AddressInfo addressInfo,
            EmploymentInfo employmentInfo,
            IncomeInfo incomeInfo,
            FinancialInfo financialInfo) {
        if (status != PtinStatus.PENDING_APPROVAL && status != PtinStatus.ISSUANCE_FAILED) {
            throw new PtinApplicationNotEditableException(id);
        }
        validateType(ptinType, personalInfo);
        this.ptinType = ptinType;
        this.personalInfo = personalInfo;
        this.contactInfo = contactInfo;
        this.addressInfo = addressInfo;
        this.employmentInfo = employmentInfo;
        this.incomeInfo = incomeInfo;
        this.financialInfo = financialInfo;
    }

    private static void validateType(PtinType ptinType, PersonalInfo personalInfo) {
        if (ptinType == null) {
            throw new IllegalArgumentException("ptinType is required");
        }
        if (ptinType == PtinType.LABOR && (personalInfo.laboId() == null || personalInfo.laboId().isBlank())) {
            throw new IllegalArgumentException("laboId is required for a LABOR PTIN");
        }
    }

    public void approve(UserId authorizerId) {
        requireStatus(PtinStatus.PENDING_APPROVAL);
        this.status = PtinStatus.APPROVED;
        this.approvedBy = authorizerId;
        this.approvedAt = Instant.now();
    }

    public void reject(UserId authorizerId, String reason) {
        requireStatus(PtinStatus.PENDING_APPROVAL);
        this.status = PtinStatus.REJECTED;
        this.rejectedBy = authorizerId;
        this.rejectedAt = Instant.now();
        this.rejectionReason = reason;
    }

    public void markIssued(TaxpayerIdentificationNumber tin, String confirmedGivenName, String confirmedFamilyName) {
        requireRetryableStatus();
        this.status = PtinStatus.ISSUED;
        this.tin = tin;
        this.personalInfo = new PersonalInfo(
                personalInfo.laboId(),
                confirmedGivenName,
                confirmedFamilyName,
                personalInfo.gender(),
                personalInfo.nationality(),
                personalInfo.birthDay(),
                personalInfo.individualId(),
                personalInfo.individualIdType(),
                personalInfo.familyBookIssuancePlace());
        this.issuedAt = Instant.now();
    }

    public void markIssuanceFailed(String reason) {
        requireRetryableStatus();
        this.status = PtinStatus.ISSUANCE_FAILED;
        this.failureReason = reason;
        this.failedAt = Instant.now();
        this.retryCount++;
    }

    private void requireStatus(PtinStatus expected) {
        if (status != expected) {
            throw new PtinApplicationNotPendingException(id);
        }
    }

    private void requireRetryableStatus() {
        if (status != PtinStatus.APPROVED && status != PtinStatus.ISSUANCE_FAILED) {
            throw new PtinApplicationNotRetryableException(id);
        }
    }
}
