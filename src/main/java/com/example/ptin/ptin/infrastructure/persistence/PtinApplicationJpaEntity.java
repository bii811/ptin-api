package com.example.ptin.ptin.infrastructure.persistence;

import com.example.ptin.shared.persistence.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "ptins")
@SQLRestriction("deleted_at is null")
public class PtinApplicationJpaEntity extends AuditableJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

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
    @Column(name = "labo_id", length = 20)
    private String laboId;

    @Column(name = "taxr_gv_nm", length = 200)
    private String taxrGvNm;

    @Column(name = "taxr_fam_nm", length = 200)
    private String taxrFamNm;

    @Column(name = "gnd_tp", length = 1)
    private String gndTp;

    @Column(name = "nat_tp", length = 2)
    private String natTp;

    @Column(name = "bday", length = 8)
    private String bday;

    @Column(name = "ind_id", length = 20)
    private String indId;

    @Column(name = "ind_id_tp", length = 2)
    private String indIdTp;

    @Column(name = "famb_issu_plc", length = 300)
    private String fambIssuPlc;

    // ContactInfo
    @Column(name = "tel_no", length = 20)
    private String telNo;

    @Column(name = "hp_no", length = 20)
    private String hpNo;

    @Column(name = "fax_no", length = 20)
    private String faxNo;

    @Column(name = "email", length = 200)
    private String email;

    // AddressInfo
    @Column(name = "addr_seqno", length = 8)
    private String addrSeqno;

    @Column(name = "unit_no", length = 3)
    private String unitNo;

    @Column(name = "road_nm", length = 200)
    private String roadNm;

    @Column(name = "hou_no", length = 200)
    private String houNo;

    @Column(name = "pbox_no", length = 10)
    private String pboxNo;

    // EmploymentInfo
    @Column(name = "pub_offi_yn", length = 1)
    private String pubOffiYn;

    @Column(name = "ind_busn_opr_yn", length = 1)
    private String indBusnOprYn;

    @Column(name = "pvt_co_emp_yn", length = 1)
    private String pvtCoEmpYn;

    @Column(name = "etc_job_cont")
    private String etcJobCont;

    @Column(name = "work_tin", length = 12)
    private String workTin;

    @Column(name = "work_addr_seqno", length = 8)
    private String workAddrSeqno;

    @Column(name = "work_unit_no", length = 3)
    private String workUnitNo;

    @Column(name = "work_road_nm", length = 200)
    private String workRoadNm;

    @Column(name = "work_hou_no", length = 200)
    private String workHouNo;

    @Column(name = "srl_amt", length = 28)
    private String srlAmt;

    // IncomeInfo
    @Column(name = "divd_inc_yn", length = 1)
    private String divdIncYn;

    @Column(name = "rent_inc_yn", length = 1)
    private String rentIncYn;

    @Column(name = "etc_inc_cont")
    private String etcIncCont;

    // FinancialInfo
    @Column(name = "bank_acc_no", length = 28)
    private String bankAccNo;

    @Column(name = "so_se_no", length = 20)
    private String soSeNo;

    // Response
    @Column(name = "tin", length = 12)
    private String tin;

    protected PtinApplicationJpaEntity() {
    }

    public PtinApplicationJpaEntity(UUID id, UUID userId, String status, Instant submittedAt) {
        assignId(id);
        this.userId = userId;
        this.status = status;
        this.submittedAt = submittedAt;
        this.retryCount = 0;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public UUID getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(UUID rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getLaboId() {
        return laboId;
    }

    public void setLaboId(String laboId) {
        this.laboId = laboId;
    }

    public String getTaxrGvNm() {
        return taxrGvNm;
    }

    public void setTaxrGvNm(String taxrGvNm) {
        this.taxrGvNm = taxrGvNm;
    }

    public String getTaxrFamNm() {
        return taxrFamNm;
    }

    public void setTaxrFamNm(String taxrFamNm) {
        this.taxrFamNm = taxrFamNm;
    }

    public String getGndTp() {
        return gndTp;
    }

    public void setGndTp(String gndTp) {
        this.gndTp = gndTp;
    }

    public String getNatTp() {
        return natTp;
    }

    public void setNatTp(String natTp) {
        this.natTp = natTp;
    }

    public String getBday() {
        return bday;
    }

    public void setBday(String bday) {
        this.bday = bday;
    }

    public String getIndId() {
        return indId;
    }

    public void setIndId(String indId) {
        this.indId = indId;
    }

    public String getIndIdTp() {
        return indIdTp;
    }

    public void setIndIdTp(String indIdTp) {
        this.indIdTp = indIdTp;
    }

    public String getFambIssuPlc() {
        return fambIssuPlc;
    }

    public void setFambIssuPlc(String fambIssuPlc) {
        this.fambIssuPlc = fambIssuPlc;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getHpNo() {
        return hpNo;
    }

    public void setHpNo(String hpNo) {
        this.hpNo = hpNo;
    }

    public String getFaxNo() {
        return faxNo;
    }

    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddrSeqno() {
        return addrSeqno;
    }

    public void setAddrSeqno(String addrSeqno) {
        this.addrSeqno = addrSeqno;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public String getRoadNm() {
        return roadNm;
    }

    public void setRoadNm(String roadNm) {
        this.roadNm = roadNm;
    }

    public String getHouNo() {
        return houNo;
    }

    public void setHouNo(String houNo) {
        this.houNo = houNo;
    }

    public String getPboxNo() {
        return pboxNo;
    }

    public void setPboxNo(String pboxNo) {
        this.pboxNo = pboxNo;
    }

    public String getPubOffiYn() {
        return pubOffiYn;
    }

    public void setPubOffiYn(String pubOffiYn) {
        this.pubOffiYn = pubOffiYn;
    }

    public String getIndBusnOprYn() {
        return indBusnOprYn;
    }

    public void setIndBusnOprYn(String indBusnOprYn) {
        this.indBusnOprYn = indBusnOprYn;
    }

    public String getPvtCoEmpYn() {
        return pvtCoEmpYn;
    }

    public void setPvtCoEmpYn(String pvtCoEmpYn) {
        this.pvtCoEmpYn = pvtCoEmpYn;
    }

    public String getEtcJobCont() {
        return etcJobCont;
    }

    public void setEtcJobCont(String etcJobCont) {
        this.etcJobCont = etcJobCont;
    }

    public String getWorkTin() {
        return workTin;
    }

    public void setWorkTin(String workTin) {
        this.workTin = workTin;
    }

    public String getWorkAddrSeqno() {
        return workAddrSeqno;
    }

    public void setWorkAddrSeqno(String workAddrSeqno) {
        this.workAddrSeqno = workAddrSeqno;
    }

    public String getWorkUnitNo() {
        return workUnitNo;
    }

    public void setWorkUnitNo(String workUnitNo) {
        this.workUnitNo = workUnitNo;
    }

    public String getWorkRoadNm() {
        return workRoadNm;
    }

    public void setWorkRoadNm(String workRoadNm) {
        this.workRoadNm = workRoadNm;
    }

    public String getWorkHouNo() {
        return workHouNo;
    }

    public void setWorkHouNo(String workHouNo) {
        this.workHouNo = workHouNo;
    }

    public String getSrlAmt() {
        return srlAmt;
    }

    public void setSrlAmt(String srlAmt) {
        this.srlAmt = srlAmt;
    }

    public String getDivdIncYn() {
        return divdIncYn;
    }

    public void setDivdIncYn(String divdIncYn) {
        this.divdIncYn = divdIncYn;
    }

    public String getRentIncYn() {
        return rentIncYn;
    }

    public void setRentIncYn(String rentIncYn) {
        this.rentIncYn = rentIncYn;
    }

    public String getEtcIncCont() {
        return etcIncCont;
    }

    public void setEtcIncCont(String etcIncCont) {
        this.etcIncCont = etcIncCont;
    }

    public String getBankAccNo() {
        return bankAccNo;
    }

    public void setBankAccNo(String bankAccNo) {
        this.bankAccNo = bankAccNo;
    }

    public String getSoSeNo() {
        return soSeNo;
    }

    public void setSoSeNo(String soSeNo) {
        this.soSeNo = soSeNo;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }
}
