package com.gatepass_automation.gatepass_automation.scheduler;

import com.gatepass_automation.gatepass_automation.controller.OutlookController;
import com.gatepass_automation.gatepass_automation.dto.TokenResponse;
import com.gatepass_automation.gatepass_automation.service.AadhaarExtractionService;
import com.gatepass_automation.gatepass_automation.model.Worker;
import com.gatepass_automation.gatepass_automation.model.ProcessedEmail;
import com.gatepass_automation.gatepass_automation.repository.ProcessedEmailRepository;
import com.gatepass_automation.gatepass_automation.service.BrowserAutomationService;
import com.gatepass_automation.gatepass_automation.service.MicrosoftTokenService;
import com.gatepass_automation.gatepass_automation.service.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatepass_automation.gatepass_automation.dto.MicrosoftGraphResponse;
import com.gatepass_automation.gatepass_automation.dto.EmailMessageDTO;
import com.gatepass_automation.gatepass_automation.dto.MicrosoftAttachmentsResponse;
import com.gatepass_automation.gatepass_automation.dto.AttachmentDTO;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GatePassScheduler {

    private final MicrosoftTokenService tokenService;
    private final OutlookController outlookController;
    private final TokenStore tokenStore;
    private final AadhaarExtractionService aadhaarExtractionService;
    private final BrowserAutomationService browserAutomationService;
    private final ProcessedEmailRepository processedEmailRepository;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("Password\\s*-\\s*([A-Za-z0-9_]+)");
    private static final Pattern FUZZY_SUBJECT_PATTERN = Pattern.compile(".*gate\\s*pass.*", Pattern.CASE_INSENSITIVE);

    @Value("${gatepass.automation.download-dir}")
    private String downloadDir;

    @Scheduled(fixedRate = 60000)
    public void runAutomatedGatePassCheck() {
        System.out.println("⏰ Automated Background Task Started: Checking for new Gate Passes...");

        try {
            String currentRefreshToken = tokenStore.getRefreshToken();
            if (currentRefreshToken == null || currentRefreshToken.isEmpty()) {
                System.out.println("🛑 Missing base refresh token.");
                return;
            }

            TokenResponse tokenResponse = tokenService.refreshAccessToken(currentRefreshToken);
            tokenStore.setAccessToken(tokenResponse.accessToken());
            tokenStore.setRefreshToken(tokenResponse.refreshToken());

            String rawJsonEmails = outlookController.emails();
            MicrosoftGraphResponse responseData = objectMapper.readValue(rawJsonEmails, MicrosoftGraphResponse.class);

            for (EmailMessageDTO email : responseData.value()) {

                // 🌟 DATABASE CHECK: Query MySQL instead of in-memory HashSet
                if (processedEmailRepository.existsById(email.id())) {
                    continue;
                }

                String subjectText = email.subject() != null ? email.subject() : "";
                Matcher subjectMatcher = FUZZY_SUBJECT_PATTERN.matcher(subjectText);

                if (!subjectMatcher.matches()) {
                    String previewText = email.bodyPreview() != null ? email.bodyPreview().toLowerCase() : "";
                    if (!previewText.contains("gate pass") && !previewText.contains("gatepass")) {
                        continue;
                    }
                }

                if (!email.hasAttachments()) {
                    continue;
                }

                String textToScan = (email.bodyPreview() != null ? email.bodyPreview() : "") + " " +
                        (email.body() != null && email.body().content() != null ? email.body().content() : "");

                Matcher matcher = PASSWORD_PATTERN.matcher(textToScan);
                if (matcher.find()) {
                    String dynamicPassword = matcher.group(1);

                    System.out.println("\n🎯 VALIDATED GATE PASS DETECTED!");
                    System.out.println("🔑 Password Found: " + dynamicPassword);

                    processedEmailRepository.save(new ProcessedEmail(email.id(), LocalDateTime.now()));

                    System.out.println("🎬 Pulling down structural documents...");
                    downloadAndExtractAttachments(email.id(), dynamicPassword);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Automation Error occurred: " + e.getMessage());
        }
    }

    private void downloadAndExtractAttachments(String messageId, String folderKey) {
        try {
            String url = "https://graph.microsoft.com/v1.0/me/messages/" + messageId + "/attachments";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenStore.getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                MicrosoftAttachmentsResponse attachmentsData = objectMapper.readValue(response.getBody(), MicrosoftAttachmentsResponse.class);

                for (AttachmentDTO attachment : attachmentsData.value()) {
                    if (attachment.contentBytes() != null) {

                        String structuredFolderPath = downloadDir + folderKey + "/";
                        Files.createDirectories(Paths.get(structuredFolderPath));

                        String cleanFileName = attachment.name().replaceAll("[^a-zA-Z0-9.\\-_]", "_");
                        String destinationFilePath = structuredFolderPath + cleanFileName;

                        byte[] fileBytes = Base64.getDecoder().decode(attachment.contentBytes());
                        Files.write(Paths.get(destinationFilePath), fileBytes);

                        System.out.println("💾 File Saved Safely! Path -> " + destinationFilePath);

                        if (cleanFileName.toLowerCase().endsWith(".pdf")) {
                            System.out.println("📝 PDF detected. Initiating Document Parsing Service...");

                            Worker processedWorker = aadhaarExtractionService.extract(destinationFilePath, folderKey);
                            System.out.println("🚀 Pipeline Completed for worker: " + processedWorker.getName());

                            System.out.println("⚡ Launching automated web form submission engine...");
                            browserAutomationService.executeFormAutomation(processedWorker);

                            System.out.println("🧹 Cleaning up inbox status...");
                            outlookController.markEmailAsRead(messageId, tokenStore.getAccessToken());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to process or extract file payload: " + e.getMessage());
        }
    }
}