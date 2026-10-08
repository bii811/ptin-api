package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.AddressInfo;
import com.example.ptin.ptin.domain.model.ContactInfo;
import com.example.ptin.ptin.domain.model.EmploymentInfo;
import com.example.ptin.ptin.domain.model.FinancialInfo;
import com.example.ptin.ptin.domain.model.IncomeInfo;
import com.example.ptin.ptin.domain.model.PersonalInfo;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.PtinType;

public interface UpdatePtinApplicationUseCase {

    PtinApplication update(UpdatePtinApplicationCommand command);

    record UpdatePtinApplicationCommand(
            PtinApplicationId applicationId,
            PtinType ptinType,
            PersonalInfo personalInfo,
            ContactInfo contactInfo,
            AddressInfo addressInfo,
            EmploymentInfo employmentInfo,
            IncomeInfo incomeInfo,
            FinancialInfo financialInfo) {
    }
}
