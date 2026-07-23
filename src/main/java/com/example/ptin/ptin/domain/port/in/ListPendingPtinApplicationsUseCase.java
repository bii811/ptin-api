package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplication;
import java.util.List;

public interface ListPendingPtinApplicationsUseCase {

    List<PtinApplication> listPending();
}
