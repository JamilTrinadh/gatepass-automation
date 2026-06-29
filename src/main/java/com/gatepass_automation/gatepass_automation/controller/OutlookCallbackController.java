package com.gatepass_automation.gatepass_automation.controller;

import com.gatepass_automation.gatepass_automation.dto.TokenResponse;
import com.gatepass_automation.gatepass_automation.service.MicrosoftTokenService;
import com.gatepass_automation.gatepass_automation.service.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/outlook")
@RequiredArgsConstructor
public class OutlookCallbackController {

    private final MicrosoftTokenService tokenService;
    private final TokenStore tokenStore;

    @GetMapping("/callback")
    public String callback(@RequestParam String code) {
        try {
            // 1. Fetch tokens using your token service
            TokenResponse tokenResponse = tokenService.getAccessToken(code);

            // 2. Updated to use direct record accessors: scope(), accessToken(), etc.
            System.out.println("SCOPES ACTUALLY GRANTED BY MICROSOFT: " + tokenResponse.scope());
            System.out.println("ACCESS TOKEN RECEIVED: " + tokenResponse.accessToken().substring(0, Math.min(20, tokenResponse.accessToken().length())) + "...");
            System.out.println("REFRESH TOKEN RECEIVED: " + (tokenResponse.refreshToken() != null ? "Yes (Stored)" : "No"));
            System.out.println("TOKEN TYPE = " + tokenResponse.tokenType());

            // 3. Persist tokens into memory store
            tokenStore.setAccessToken(tokenResponse.accessToken());
            if (tokenResponse.refreshToken() != null) {
                tokenStore.setRefreshToken(tokenResponse.refreshToken());
            }

            // 4. Return visual validation markup success page
            return "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>"
                    + "<h1 style='color:#2e7d32;'>Login Successful! ✅</h1>"
                    + "<p style='color:#555; font-size:16px;'>Your tokens have been stored securely.</p>"
                    + "<p style='color:#888;'>You can now safely close this browser tab and let the background scheduler work.</p>"
                    + "</body></html>";

        } catch (Exception e) {
            System.err.println("❌ Error processing OAuth callback: " + e.getMessage());
            return "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>"
                    + "<h1 style='color:#d32f2f;'>Authentication Failed ❌</h1>"
                    + "<p style='color:#555;'>Error details: " + e.getMessage() + "</p>"
                    + "</body></html>";
        }
    }
}