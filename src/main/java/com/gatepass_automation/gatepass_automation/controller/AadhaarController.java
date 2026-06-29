package com.gatepass_automation.gatepass_automation.controller;

import com.gatepass_automation.gatepass_automation.service.AadhaarExtractionService;
import com.gatepass_automation.gatepass_automation.model.Worker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aadharextraction")
@RequiredArgsConstructor
public class AadhaarController {

    private final AadhaarExtractionService service;

    @GetMapping("/extract")
    public Worker extract(
            @RequestParam(defaultValue = "E-AadharJami.pdf") String fileName,
            @RequestParam(defaultValue = "JAMI2002") String folderKey) {

        // De-hardcoded: Allows dynamic testing (e.g., /extract?fileName=test.pdf&folderKey=ABC)
        return service.extract(fileName, folderKey);
    }
}