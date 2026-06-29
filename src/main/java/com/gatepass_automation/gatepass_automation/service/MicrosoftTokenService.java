package com.gatepass_automation.gatepass_automation.service;

import com.gatepass_automation.gatepass_automation.config.MicrosoftConfig;
import com.gatepass_automation.gatepass_automation.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MicrosoftTokenService {

    private final MicrosoftConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    private static final String SCOPES = "openid profile email User.Read Mail.Read Mail.ReadWrite offline_access";

    public TokenResponse getAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.clientId());
        body.add("client_secret", config.clientSecret());
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "http://localhost:8080/outlook/callback");
        body.add("scope", SCOPES);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URL, request, TokenResponse.class);

        return response.getBody();
    }

    public TokenResponse refreshAccessToken(String storedRefreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.clientId());
        body.add("client_secret", config.clientSecret());
        body.add("refresh_token", storedRefreshToken);
        body.add("grant_type", "refresh_token");
        body.add("scope", SCOPES);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URL, request, TokenResponse.class);

        return response.getBody();
    }
}