package com.example.ptin.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OtpProperties.class, PasswordLoginProperties.class})
public class AuthPropertiesConfig {
}
