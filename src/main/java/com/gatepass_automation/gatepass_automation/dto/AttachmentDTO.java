package com.gatepass_automation.gatepass_automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttachmentDTO(
        String name,
        String contentType,
        String contentBytes // Holds the base64 encoded file payload
) {}