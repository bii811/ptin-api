package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinStatus;
import java.util.List;

public interface ListPtinApplicationsByStatusUseCase {

    /** @param status the status to filter by, or {@code null} to return applications in every status. */
    List<PtinApplication> list(PtinStatus status);
}
