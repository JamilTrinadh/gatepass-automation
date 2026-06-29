package com.gatepass_automation.gatepass_automation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gatepass_automation.gatepass_automation.service.BrowserAutomationService;

@RestController
@RequestMapping("/automation")
@RequiredArgsConstructor
public class AutomationController {

    private final BrowserAutomationService service;

    @GetMapping("/status")
    public String getStatus() {
        return "Browser Automation Service is Active and Initialized.";
    }

    /**
     * Triggers the real portal authentication check manually via browser execution.
     * Accessible at: HTTP GET http://localhost:8080/automation/test-login
     */
    @GetMapping("/test-login")
    public String triggerLoginVerification() {
        System.out.println("🕹️ Manual execution requested via REST Controller API endpoint...");

        // Passing null for now since we stripped the logic to focus strictly on login validation
        service.executeFormAutomation(null);

        return "Portal login verification sequence triggered. Monitor your terminal logs and viewport display!";
    }
}