package com.fcrm.fraud.x9parser.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeTest extends SeleniumTestBase{
    @Test
    void theLoginPageOpensInARealBrowser() {
        openHome();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        assertTrue(driver.getPageSource().contains("Sign in"));
    }
}