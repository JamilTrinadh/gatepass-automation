package com.gatepass_automation.gatepass_automation.service;

import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.gatepass_automation.gatepass_automation.model.Worker;
import java.io.File;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class BrowserAutomationService {

    @Value("${gatepass.automation.portal-url}")
    private String portalUrl;

    @Value("${gatepass.automation.headless:false}")
    private boolean headless;

    @Value("${gatepass.automation.admin-username}")
    private String adminUsername;

    @Value("${gatepass.automation.admin-password}")
    private String adminPassword;

    public void executeFormAutomation(Worker worker) {
        System.out.println("🌐 Initiating Portal Login & Direct Form Navigation Sequence...");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(400)
            );

            Page page = browser.newPage();
            page.navigate(portalUrl);

            // 1. Authentication Execution
            System.out.println("🔑 Injecting credentials...");
            page.locator("#client_Login_UserName").fill(adminUsername);
            page.locator("#client_Login_Password").fill(adminPassword);
            page.locator("#client_Login_btnlogin").click();

            // 2. Login Verification Check
            page.waitForURL("**/Dashboard.aspx", new Page.WaitForURLOptions().setTimeout(10000));
            System.out.println("✅ LOGIN SUCCESSFUL! Arrived at Dashboard.");

            // 3. Direct Sub-Page Navigation Bypass
            String formUrl = "https://wvs.gipl.in/SubContractor/AddManPower.aspx";
            System.out.println("✈️ Jumping straight to form page: " + formUrl);
            page.navigate(formUrl);
            page.waitForURL("**/AddManPower.aspx", new Page.WaitForURLOptions().setTimeout(10000));
            System.out.println("🎉 Form page loaded completely. Beginning data population...");

            // 4. Automated Data Entry Sequence

            // Step 4a: Selection Elements (Type of Manpower)
            System.out.println("📝 Selecting Type of Manpower...");
            page.selectOption("#body_ddlVisitorType", "2"); // Selects Option "2" (Worker)

            // ⏳ CRITICAL: Give the ASP.NET auto-population script 1.5 seconds to flash-fill the dates
            page.waitForTimeout(1500);

            // Step 4b: Text Fields (Personal Details)
            System.out.println("📝 Entering Personal Details...");
            page.locator("#body_txtFirstName").fill("John");
            page.locator("#body_txtMiddleName").fill("Robert");
            page.locator("#body_txtLastName").fill("Doe");

            // Step 4c: Date of Birth Picker
            page.locator("#body_txtDOB").fill("15/08/1995");

            // Step 4d: Dropdowns (Gender & Nationality)
            page.selectOption("#body_ddlGender", "1"); // Option "1" maps to Male
            page.selectOption("#body_ddlVisitorNationality", "1"); // Option "1" maps to Indian

            // ⏳ CRITICAL: Give the ASP.NET auto-population script 1.5 seconds to adjust dependent states
            page.waitForTimeout(1500);

            // Step 4e: Contact & Address Data
            page.locator("#body_txtMobileNo").fill("9876543210");
            page.locator("#body_txtPermanentAddress").fill("123 Main Street, Sector 4");
            page.selectOption("#body_ddlState", "7"); // Option "7" maps to Gujarat

            page.locator("#body_txtDistinguishingMark").fill("Mole on right hand forearm");

            // Step 4f: Document & Photo Attachment Upload Layer
            System.out.println("📸 Attaching profile photograph document...");
            File testImage = new File("sample_photo.jpg");
            if (testImage.exists() && testImage.isFile()) {
                page.setInputFiles("#body_fileuploadPhoto", Paths.get(testImage.getAbsolutePath()));
            } else {
                System.err.println("⚠️ Warning: sample_photo.jpg not found in project root directory!");
            }

            // Step 4g: Work Detail Layer
            System.out.println("💼 Filling Work Details segment...");
            page.locator("#body_txtNatureOfWork").fill("Project");
            page.selectOption("#body_ddlShiftofWork", "3"); // Option "3" maps directly to "Both"

            // Step 4h: Documents & Declarations Layer
            System.out.println("📁 Processing Document Upload and Electronic Gadgets...");

            // Upload mandatory PDF identification document
            File verificationPdf = new File("AadharCardJamiTrinadh.pdf");
            if (verificationPdf.exists() && verificationPdf.isFile()) {
                System.out.println("📤 Uploading verification document from: " + verificationPdf.getAbsolutePath());
                page.setInputFiles("#body_rptDocument_fileuploadDocument_0", Paths.get(verificationPdf.getAbsolutePath()));
            } else {
                System.err.println("⚠️ Warning: AadharCardJamiTrinadh.pdf not found in project root directory!");
            }

            // Electronic Gadget Checklist - Mobile Configuration
            System.out.println("📱 Setting Mobile allocations...");
            if (!page.isChecked("#body_chkIsMobile")) {
                page.check("#body_chkIsMobile");
            }
            page.locator("#body_txtMobileNos").fill("2");

            // Electronic Gadget Checklist - Laptop Configuration
            System.out.println("💻 Setting Laptop allocations...");
            if (!page.isChecked("#body_chkIsLaptop")) {
                page.check("#body_chkIsLaptop");
            }
            page.locator("#body_txtLaptopNos").fill("1");

            // Status Verification - Ensure worker is set to Active status
            if (!page.isChecked("#body_chkIsActive")) {
                page.check("#body_chkIsActive");
            }

            // Accept Final Legal Declaration Terms and Liabilities
            if (!page.isChecked("#body_chkActive")) {
                page.check("#body_chkActive");
            }
            System.out.println("✅ Document table, device configurations, and legal checkboxes completed.");

            System.out.println("🚀 Form data entry test execution complete!");

            // Pauses execution for 20 seconds so you can visually verify the entries
            page.waitForTimeout(20000);

            browser.close();
            System.out.println("🏁 Automation dry-run complete.");

        } catch (Exception e) {
            System.err.println("❌ Critical runtime exception captured: " + e.getMessage());
        }
    }
}