package com.example.ptin.ptin.domain.port.out;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.shared.identity.UserId;
import java.util.List;
import java.util.Optional;

public interface PtinApplicationRepository {

    Optional<PtinApplication> findById(PtinApplicationId id);

    List<PtinApplication> findByUserId(UserId userId);

    List<PtinApplication> findByStatus(PtinStatus status);

    PtinApplication save(PtinApplication application);
}
