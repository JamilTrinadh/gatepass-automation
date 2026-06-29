package com.gatepass_automation.gatepass_automation.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class TokenStore {
    private String accessToken;
    private String refreshToken;
}