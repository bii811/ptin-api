package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.AddressInfo;
import com.example.ptin.ptin.domain.model.ContactInfo;
import com.example.ptin.ptin.domain.model.EmploymentInfo;
import com.example.ptin.ptin.domain.model.FinancialInfo;
import com.example.ptin.ptin.domain.model.IncomeInfo;
import com.example.ptin.ptin.domain.model.PersonalInfo;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.identity.UserId;

public interface SubmitPtinApplicationUseCase {

    PtinApplicationId submit(SubmitPtinApplicationCommand command);

    record SubmitPtinApplicationCommand(
            UserId userId,
            PersonalInfo personalInfo,
            ContactInfo contactInfo,
            AddressInfo addressInfo,
            EmploymentInfo employmentInfo,
            IncomeInfo incomeInfo,
            FinancialInfo financialInfo) {
    }
}
