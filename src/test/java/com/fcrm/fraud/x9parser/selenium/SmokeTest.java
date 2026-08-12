package com.fcrm.fraud.x9parser.selenium;

import java.io.IOException;

import com.fcrm.fraud.x9parser.selenium.pages.LoginPage;
import com.fcrm.fraud.x9parser.selenium.pages.ParsePage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Proves the setup and the page objects work before the real scenarios are built.
class SmokeTest extends SeleniumTestBase {

    @Value("${selenium.admin-username}")
    private String adminUsername;

    @Value("${selenium.admin-password}")
    private String adminPassword;

    @Test
    void aVisitorWhoIsNotSignedInSeesTheLoginPage() {
        openHome();

        LoginPage loginPage = new LoginPage(driver, wait);

        assertTrue(loginPage.isDisplayed());
    }

    @Test
    void theAdminUserCanSignInAndReachTheParsePage() throws IOException{
        openHome();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs(adminUsername, adminPassword);
        screenshots.capture("smoke-check");
        ParsePage parsePage = new ParsePage(driver, wait);
        assertTrue(parsePage.isDisplayed());
        assertTrue(parsePage.getSignedInUser().contains(adminUsername));
    }
}