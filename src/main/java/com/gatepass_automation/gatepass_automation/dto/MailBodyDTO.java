package com.gatepass_automation.gatepass_automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MailBodyDTO(
        String contentType,
        String content
) {}