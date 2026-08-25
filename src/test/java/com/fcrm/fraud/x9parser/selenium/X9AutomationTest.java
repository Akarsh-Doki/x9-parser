package com.fcrm.fraud.x9parser.selenium;

import com.fcrm.fraud.x9parser.selenium.pages.LoginPage;
import com.fcrm.fraud.x9parser.selenium.pages.ParsePage;
import com.fcrm.fraud.x9parser.selenium.pages.ResultPage;
import com.fcrm.fraud.x9parser.selenium.support.EmailReporter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class X9AutomationTest extends SeleniumTestBase{
    private static final Logger log = LoggerFactory.getLogger(X9AutomationTest.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Value("${report.from}")
    private String from;

    @Value("${report.to}")
    private String[] recipients;

    @Value("${selenium.screenshot-dir}")
    private String screenshotDir;
    
    @Value("${selenium.admin-username}")
    private String adminUsername;

    @Value("${selenium.admin-password}")
    private String adminPassword;

    @Value("${selenium.user-username}")
    private String userUsername;

    @Value("${selenium.user-password}")
    private String userPassword;

    @Value("${selenium.x9-file}")
    private String x9File;

    @Test
    void adminCanParseAnX9File() throws IOException{
        openHome();
        screenshots.capture("login-page");

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs(adminUsername, adminPassword);
        screenshots.capture("admin-signed-in");
        ParsePage parsePage = new ParsePage(driver, wait);
        assertTrue(parsePage.isDisplayed());

        parsePage.parseFile(x9File);

        ResultPage resultPage = new ResultPage(driver, wait);
        assertTrue(resultPage.isDisplayed());

        screenshots.capture("parse-result"); 
        String summary = resultPage.getSummaryText();
        assertTrue(summary.contains("10"));
        assertTrue(summary.contains("20"));
        assertTrue(resultPage.getOutputPaths().contains(".csv"));
    }
    @Test
    void normalUserCannotParse() throws IOException {
        openHome();
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs(userUsername, userPassword);

        wait.until(ExpectedConditions.urlContains("/no-permission"));

        screenshots.capture("normal-user-blocked");
        assertTrue(driver.getPageSource().contains("You do not have permission to parse files."));
        assertTrue(driver.getCurrentUrl().contains("/no-permission"));
    }
    @Test
    void aBadFilePathShowsAnError() throws IOException {
        openHome();
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs(adminUsername, adminPassword);

        ParsePage parsePage = new ParsePage(driver, wait);
        parsePage.parseFile("/no/such/file.x9");

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            org.openqa.selenium.By.tagName("body"), "File not found"));

        screenshots.capture("bad-path-error");

        assertTrue(driver.getPageSource().contains("File not found"));
    }

    @AfterAll
    void emailTheReport() {
        String body = "X9 Parser automated test run.\n\n"
                + "1. Admin signs in and parses an X9 file\n"
                + "2. Normal user is blocked from parsing\n"
                + "3. A bad file path shows an error\n\n"
                + "Screenshots from each step are attached.";
        try {
            EmailReporter reporter = new EmailReporter(mailSender, from, recipients, screenshotDir);
            reporter.sendReport("X9 Parser automated test report", body);
        } 
        catch (Exception e) {
            log.warn("Could not send the email report: {}", e.getMessage());
        }
    }
}
