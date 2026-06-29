package com.gatepass_automation.gatepass_automation.controller;

import com.gatepass_automation.gatepass_automation.config.MicrosoftConfig;
import com.gatepass_automation.gatepass_automation.service.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/outlook")
public class OutlookController {

    private final MicrosoftConfig config;
    private final TokenStore tokenStore;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/login-url")
    public RedirectView loginUrl() {
        // Updated to use the modern record accessors config.clientId()
        String targetUrl = String.format(
                "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                        + "?client_id=%s"
                        + "&response_type=code"
                        + "&redirect_uri=http://localhost:8080/outlook/callback"
                        + "&response_mode=query"
                        + "&scope=openid%%20profile%%20email%%20User.Read%%20Mail.Read%%20Mail.ReadWrite%%20offline_access",
                config.clientId()
        );

        System.out.println("🚀 Redirecting user automatically to Microsoft OAuth Portal with Read/Write elevation...");
        return new RedirectView(targetUrl);
    }

    @GetMapping("/emails")
    public String emails() {
        String accessToken = tokenStore.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String sevenDaysAgoIso = Instant.now()
                .minus(7, ChronoUnit.DAYS)
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        try {
            String url = String.format(
                    "https://graph.microsoft.com/v1.0/me/messages?$filter=receivedDateTime ge %s and isRead eq false&$orderby=receivedDateTime desc&$top=10",
                    sevenDaysAgoIso
            );

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Graph API Error status = " + e.getStatusCode());
            return e.getResponseBodyAsString();
        }
    }

    public void markEmailAsRead(String messageId, String accessToken) {
        try {
            String url = "https://graph.microsoft.com/v1.0/me/messages/" + messageId;
            String jsonBody = "{\"isRead\": true}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("📬 Microsoft Graph: Email marked as READ successfully (Status 200).");
            } else {
                System.err.println("⚠️ Microsoft Graph returned status code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to patch email status: " + e.getMessage());
        }
    }
}