package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchPtinApplicationsUseCase {

    Page<PtinApplication> search(PtinApplicationSearchCriteria criteria, Pageable pageable);
}
