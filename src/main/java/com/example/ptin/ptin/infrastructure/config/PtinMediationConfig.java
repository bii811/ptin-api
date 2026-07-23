package com.example.ptin.ptin.infrastructure.config;

import com.example.ptin.ptin.infrastructure.mediation.TinMediationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TinMediationProperties.class)
public class PtinMediationConfig {
}
