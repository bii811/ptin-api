package com.example.ptin.ptin.infrastructure.persistence;

import com.example.ptin.ptin.domain.model.AddressInfo;
import com.example.ptin.ptin.domain.model.ContactInfo;
import com.example.ptin.ptin.domain.model.EmploymentInfo;
import com.example.ptin.ptin.domain.model.FinancialInfo;
import com.example.ptin.ptin.domain.model.IncomeInfo;
import com.example.ptin.ptin.domain.model.PersonalInfo;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.model.TaxpayerIdentificationNumber;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Component;

@Component
class PtinApplicationPersistenceMapper {

    PtinApplicationJpaEntity toEntity(PtinApplication application) {
        PtinApplicationJpaEntity entity = new PtinApplicationJpaEntity(
                application.getId().value(),
                application.getUserId().value(),
                application.getStatus().name(),
                application.getSubmittedAt());
        copyMutableFields(entity, application);
        return entity;
    }

    void updateEntity(PtinApplicationJpaEntity entity, PtinApplication application) {
        entity.setStatus(application.getStatus().name());
        copyMutableFields(entity, application);
    }

    private void copyMutableFields(PtinApplicationJpaEntity entity, PtinApplication application) {
        entity.setApprovedBy(application.getApprovedBy() == null ? null : application.getApprovedBy().value());
        entity.setApprovedAt(application.getApprovedAt());
        entity.setRejectedBy(application.getRejectedBy() == null ? null : application.getRejectedBy().value());
        entity.setRejectedAt(application.getRejectedAt());
        entity.setRejectionReason(application.getRejectionReason());
        entity.setIssuedAt(application.getIssuedAt());
        entity.setFailureReason(application.getFailureReason());
        entity.setFailedAt(application.getFailedAt());
        entity.setRetryCount(application.getRetryCount());
        entity.setTin(application.getTin() == null ? null : application.getTin().value());

        PersonalInfo personalInfo = application.getPersonalInfo();
        entity.setLaboId(personalInfo.laboId());
        entity.setTaxrGvNm(personalInfo.givenName());
        entity.setTaxrFamNm(personalInfo.familyName());
        entity.setGndTp(personalInfo.gender());
        entity.setNatTp(personalInfo.nationality());
        entity.setBday(personalInfo.birthDay());
        entity.setIndId(personalInfo.individualId());
        entity.setIndIdTp(personalInfo.individualIdType());
        entity.setFambIssuPlc(personalInfo.familyBookIssuancePlace());

        ContactInfo contactInfo = application.getContactInfo();
        entity.setTelNo(contactInfo.telNo());
        entity.setHpNo(contactInfo.hpNo());
        entity.setFaxNo(contactInfo.faxNo());
        entity.setEmail(contactInfo.email());

        AddressInfo addressInfo = application.getAddressInfo();
        entity.setAddrSeqno(addressInfo.addrSeqNo());
        entity.setUnitNo(addressInfo.unitNo());
        entity.setRoadNm(addressInfo.roadNm());
        entity.setHouNo(addressInfo.houNo());
        entity.setPboxNo(addressInfo.pboxNo());

        EmploymentInfo employmentInfo = application.getEmploymentInfo();
        entity.setPubOffiYn(employmentInfo.pubOffiYn());
        entity.setIndBusnOprYn(employmentInfo.indBusnOprYn());
        entity.setPvtCoEmpYn(employmentInfo.pvtCoEmpYn());
        entity.setEtcJobCont(employmentInfo.etcJobCont());
        entity.setWorkTin(employmentInfo.workTin());
        entity.setWorkAddrSeqno(employmentInfo.workAddrSeqNo());
        entity.setWorkUnitNo(employmentInfo.workUnitNo());
        entity.setWorkRoadNm(employmentInfo.workRoadNm());
        entity.setWorkHouNo(employmentInfo.workHouNo());
        entity.setSrlAmt(employmentInfo.srlAmt());

        IncomeInfo incomeInfo = application.getIncomeInfo();
        entity.setDivdIncYn(incomeInfo.divdIncYn());
        entity.setRentIncYn(incomeInfo.rentIncYn());
        entity.setEtcIncCont(incomeInfo.etcIncCont());

        FinancialInfo financialInfo = application.getFinancialInfo();
        entity.setBankAccNo(financialInfo.bankAccNo());
        entity.setSoSeNo(financialInfo.soSeNo());
    }

    PtinApplication toDomain(PtinApplicationJpaEntity entity) {
        PersonalInfo personalInfo = new PersonalInfo(
                entity.getLaboId(),
                entity.getTaxrGvNm(),
                entity.getTaxrFamNm(),
                entity.getGndTp(),
                entity.getNatTp(),
                entity.getBday(),
                entity.getIndId(),
                entity.getIndIdTp(),
                entity.getFambIssuPlc());
        ContactInfo contactInfo =
                new ContactInfo(entity.getTelNo(), entity.getHpNo(), entity.getFaxNo(), entity.getEmail());
        AddressInfo addressInfo = new AddressInfo(
                entity.getAddrSeqno(), entity.getUnitNo(), entity.getRoadNm(), entity.getHouNo(), entity.getPboxNo());
        EmploymentInfo employmentInfo = new EmploymentInfo(
                entity.getPubOffiYn(),
                entity.getIndBusnOprYn(),
                entity.getPvtCoEmpYn(),
                entity.getEtcJobCont(),
                entity.getWorkTin(),
                entity.getWorkAddrSeqno(),
                entity.getWorkUnitNo(),
                entity.getWorkRoadNm(),
                entity.getWorkHouNo(),
                entity.getSrlAmt());
        IncomeInfo incomeInfo =
                new IncomeInfo(entity.getDivdIncYn(), entity.getRentIncYn(), entity.getEtcIncCont());
        FinancialInfo financialInfo = new FinancialInfo(entity.getBankAccNo(), entity.getSoSeNo());
        TaxpayerIdentificationNumber tin = entity.getTin() == null ? null : new TaxpayerIdentificationNumber(entity.getTin());

        return PtinApplication.reconstitute(
                new PtinApplicationId(entity.getId()),
                new UserId(entity.getUserId()),
                PtinStatus.valueOf(entity.getStatus()),
                personalInfo,
                contactInfo,
                addressInfo,
                employmentInfo,
                incomeInfo,
                financialInfo,
                tin,
                entity.getSubmittedAt(),
                entity.getApprovedBy() == null ? null : new UserId(entity.getApprovedBy()),
                entity.getApprovedAt(),
                entity.getRejectedBy() == null ? null : new UserId(entity.getRejectedBy()),
                entity.getRejectedAt(),
                entity.getRejectionReason(),
                entity.getIssuedAt(),
                entity.getFailureReason(),
                entity.getFailedAt(),
                entity.getRetryCount());
    }
}
