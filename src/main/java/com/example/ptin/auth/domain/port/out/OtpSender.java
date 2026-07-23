package com.example.ptin.auth.domain.port.out;

import com.example.ptin.auth.domain.model.MobileNumber;

public interface OtpSender {

    void send(MobileNumber mobileNumber, String plainOtpCode);
}
