package com.example.ptin.ptin.infrastructure.persistence;

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

// ReqTinInfo envelope fields HASH_KEY (auth key) and SYS (system name, fixed 'LMIS')
// are not persisted here - they're fixed per request, not per application.
@Entity
@Table(name = "ptins")
@SQLRestriction("deleted_at is null")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PtinApplicationJpaEntity extends AuditableJpaEntity {

    @Setter(AccessLevel.NONE)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "ptin_type", nullable = false, length = 20)
    private String ptinType;

    @Setter(AccessLevel.NONE)
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    // PersonalInfo

    // LABO_ID - LABO_ID
    @Column(name = "labo_id", length = 20)
    private String laboId;

    // TAXR_GV_NM - Given Name
    @Column(name = "taxr_gv_nm", length = 200)
    private String taxrGvNm;

    // TAXR_FAM_NM - Family Name
    @Column(name = "taxr_fam_nm", length = 200)
    private String taxrFamNm;

    // GND_TP - Gender (Male: 'M', Female: 'F')
    @Column(name = "gnd_tp", length = 1)
    private String gndTp;

    // NAT_TP - Nationality
    @Column(name = "nat_tp", length = 2)
    private String natTp;

    // BDAY - BirthDay
    @Column(name = "bday", length = 8)
    private String bday;

    // IND_ID - Individual Identification number of External Agency
    @Column(name = "ind_id", length = 20)
    private String indId;

    // IND_ID_TP - Individual Identification Type
    @Column(name = "ind_id_tp", length = 2)
    private String indIdTp;

    // FAMB_ISSU_PLC - Laos Family Book Issuance Place
    @Column(name = "famb_issu_plc", length = 300)
    private String fambIssuPlc;

    // ContactInfo

    // TEL_NO - Telephone Number
    @Column(name = "tel_no", length = 20)
    private String telNo;

    // HP_NO - Mobilephone Number
    @Column(name = "hp_no", length = 20)
    private String hpNo;

    // FAX_NO - Fax Number
    @Column(name = "fax_no", length = 20)
    private String faxNo;

    // EMAIL - Email
    @Column(name = "email", length = 200)
    private String email;

    // AddressInfo

    // ADDR_SEQNO - Number of address
    @Column(name = "addr_seqno", length = 8)
    private String addrSeqno;

    // UNIT_NO - Number of unit which is below base address in address structure
    @Column(name = "unit_no", length = 3)
    private String unitNo;

    // ROAD_NM - Road Name
    @Column(name = "road_nm", length = 200)
    private String roadNm;

    // HOU_NO - Address - House No
    @Column(name = "hou_no", length = 200)
    private String houNo;

    // PBOX_NO - P.O. Box
    @Column(name = "pbox_no", length = 10)
    private String pboxNo;

    // EmploymentInfo

    // PUB_OFFI_YN - Yes/No - Civil servants (Y: Yes, N: No)
    @Column(name = "pub_offi_yn", length = 1)
    private String pubOffiYn;

    // IND_BUSN_OPR_YN - Yes/No - Independent jobs/Freelance (Y: Yes, N: No)
    @Column(name = "ind_busn_opr_yn", length = 1)
    private String indBusnOprYn;

    // PVT_CO_EMP_YN - Yes/No - Private employees (Y: Yes, N: No)
    @Column(name = "pvt_co_emp_yn", length = 1)
    private String pvtCoEmpYn;

    // ETC_JOB_CONT - Other jobs
    @Column(name = "etc_job_cont")
    private String etcJobCont;

    // WORK_TIN - Working Company's TIN
    @Column(name = "work_tin", length = 12)
    private String workTin;

    // WORK_ADDR_SEQNO - Working Place's Address
    @Column(name = "work_addr_seqno", length = 8)
    private String workAddrSeqno;

    // WORK_UNIT_NO - Working Place's Unit
    @Column(name = "work_unit_no", length = 3)
    private String workUnitNo;

    // WORK_ROAD_NM - Working Place's Road Name
    @Column(name = "work_road_nm", length = 200)
    private String workRoadNm;

    // WORK_HOU_NO - Working Place's House Number
    @Column(name = "work_hou_no", length = 200)
    private String workHouNo;

    // SRL_AMT - Salary Amount
    @Column(name = "srl_amt", length = 28)
    private String srlAmt;

    // IncomeInfo

    // DIVD_INC_YN - Yes/No - Dividend income existence (Y: Yes, N: No)
    @Column(name = "divd_inc_yn", length = 1)
    private String divdIncYn;

    // RENT_INC_YN - Yes/No - lease income (Y: Yes, N: No)
    @Column(name = "rent_inc_yn", length = 1)
    private String rentIncYn;

    // ETC_INC_CONT - Contents of other jobs
    @Column(name = "etc_inc_cont")
    private String etcIncCont;

    // FinancialInfo

    // BANK_ACC_NO - Bank Account Number
    @Column(name = "bank_acc_no", length = 28)
    private String bankAccNo;

    // SO_SE_NO - SOSE Card No
    @Column(name = "so_se_no", length = 20)
    private String soSeNo;

    // Response

    // TIN - Issued TIN returned in response
    @Column(name = "tin", length = 12)
    private String tin;

    public PtinApplicationJpaEntity(UUID id, UUID userId, String status, Instant submittedAt) {
        assignId(id);
        this.userId = userId;
        this.status = status;
        this.submittedAt = submittedAt;
        this.retryCount = 0;
    }
}
