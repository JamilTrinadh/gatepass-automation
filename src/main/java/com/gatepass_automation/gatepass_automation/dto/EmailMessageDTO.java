package com.gatepass_automation.gatepass_automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailMessageDTO(
        String id,
        String subject,
        String receivedDateTime,
        String bodyPreview,
        MailBodyDTO body,
        boolean hasAttachments
) {}