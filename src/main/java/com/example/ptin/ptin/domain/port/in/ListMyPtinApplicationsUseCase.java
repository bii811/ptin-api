package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.shared.identity.UserId;
import java.util.List;

public interface ListMyPtinApplicationsUseCase {

    List<PtinApplication> listMine(UserId userId);
}
