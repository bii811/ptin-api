package com.example.ptin.ptin.domain.model;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotEditableException;
import com.example.ptin.ptin.domain.exception.PtinApplicationNotPendingException;
import com.example.ptin.ptin.domain.exception.PtinApplicationNotRetryableException;
import com.example.ptin.shared.identity.UserId;
import java.time.Instant;

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

    private PtinApplication(
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
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.ptinType = ptinType;
        this.personalInfo = personalInfo;
        this.contactInfo = contactInfo;
        this.addressInfo = addressInfo;
        this.employmentInfo = employmentInfo;
        this.incomeInfo = incomeInfo;
        this.financialInfo = financialInfo;
        this.tin = tin;
        this.submittedAt = submittedAt;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = rejectedAt;
        this.rejectionReason = rejectionReason;
        this.issuedAt = issuedAt;
        this.failureReason = failureReason;
        this.failedAt = failedAt;
        this.retryCount = retryCount;
    }

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

    public PtinApplicationId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public PtinType getPtinType() {
        return ptinType;
    }

    public PtinStatus getStatus() {
        return status;
    }

    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public AddressInfo getAddressInfo() {
        return addressInfo;
    }

    public EmploymentInfo getEmploymentInfo() {
        return employmentInfo;
    }

    public IncomeInfo getIncomeInfo() {
        return incomeInfo;
    }

    public FinancialInfo getFinancialInfo() {
        return financialInfo;
    }

    public TaxpayerIdentificationNumber getTin() {
        return tin;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public UserId getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public UserId getRejectedBy() {
        return rejectedBy;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
