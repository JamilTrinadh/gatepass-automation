package com.gatepass_automation.gatepass_automation.service;

import com.gatepass_automation.gatepass_automation.model.Worker;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AadhaarExtractionServiceImpl implements AadhaarExtractionService {

    private static final Pattern DOB_PATTERN = Pattern.compile("DOB:\\s*(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("Mobile:\\s*(\\d{10})");

    @Override
    public Worker extract(String pdfPath, String password) {
        // Try-with-resources auto-closes the document safely under all conditions
        try (PDDocument document = Loader.loadPDF(new File(pdfPath), password)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            Worker worker = new Worker();
            String[] lines = text.split("\\R");

            // Extraction Logic: Identifier Parsing
            for (String line : lines) {
                line = line.trim();
                if (line.matches("\\d{4}\\s\\d{4}\\s\\d{4}")) {
                    if (!line.startsWith("9100")) {
                        worker.setAadhaar(line);
                        break;
                    }
                }
            }

            // Extraction Logic: DOB Parsing
            Matcher dobMatcher = DOB_PATTERN.matcher(text);
            if (dobMatcher.find()) {
                worker.setDob(dobMatcher.group(1));
            }

            // Extraction Logic: Mobile Parsing
            Matcher mobileMatcher = MOBILE_PATTERN.matcher(text);
            if (mobileMatcher.find()) {
                worker.setMobile(mobileMatcher.group(1));
            }

            // Extraction Logic: Gender Mapping
            if (text.contains("MALE")) {
                worker.setGender("Male");
            } else if (text.contains("FEMALE")) {
                worker.setGender("Female");
            }

            // Extraction Logic: Name Identification
            for (int i = 1; i < lines.length; i++) {
                String currentLine = lines[i].trim();
                if (currentLine.startsWith("C/O")) {
                    String name = lines[i - 1].trim();
                    if (!name.isBlank() && name.matches("[A-Za-z ]+")) {
                        worker.setName(name);
                        break;
                    }
                }
            }

            return worker;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing identity document payload: " + e.getMessage(), e);
        }
    }
}