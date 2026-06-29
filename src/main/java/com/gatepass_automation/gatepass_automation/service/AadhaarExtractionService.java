package com.gatepass_automation.gatepass_automation.service;

import com.gatepass_automation.gatepass_automation.model.Worker;

public interface AadhaarExtractionService {
    Worker extract(String pdfPath, String password);
}