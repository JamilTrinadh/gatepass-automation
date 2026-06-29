package com.gatepass_automation.gatepass_automation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MicrosoftAttachmentsResponse(
        List<AttachmentDTO> value
) {}