package com.fcrm.fraud.x9parser.selenium;

import com.fcrm.fraud.x9parser.selenium.pages.LoginPage;
import com.fcrm.fraud.x9parser.selenium.pages.ParsePage;
import com.fcrm.fraud.x9parser.selenium.pages.ResultPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class X9AutomationTest extends SeleniumTestBase{
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
        screenshots.capture("parse-result");

        assertTrue(resultPage.isDisplayed());
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
        screenshots.capture("normal-user-blocked");

        assertTrue(driver.getPageSource().contains("You do not have permission to parse files."));

        ParsePage parsePage = new ParsePage(driver, wait);
        assertFalse(parsePage.isDisplayed());
    }
    @Test
    void aBadFilePathShowsAnError() throws IOException {
        openHome();
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs(adminUsername, adminPassword);

        ParsePage parsePage = new ParsePage(driver, wait);
        parsePage.parseFile("/no/such/file.x9");
        System.out.println("URL after bad parse: " + driver.getCurrentUrl());
        screenshots.capture("bad-path-error");

        assertTrue(parsePage.getErrorMessage().contains("not found"));
    }
}
