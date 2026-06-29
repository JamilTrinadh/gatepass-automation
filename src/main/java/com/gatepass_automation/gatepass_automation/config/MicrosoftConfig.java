package com.gatepass_automation.gatepass_automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "microsoft")
public record MicrosoftConfig(
        String clientId,
        String clientSecret,
        String tenantId
) {}