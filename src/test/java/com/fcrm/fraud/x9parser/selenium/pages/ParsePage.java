package com.fcrm.fraud.x9parser.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ParsePage {
    private static final By FILE_PATH = By.name("filePath");
    private static final By PARSE_BUTTON = By.cssSelector("form[action*='parse'] button[type='submit']");
    private static final By ERROR = By.cssSelector(".error");
    private static final By TOPBAR = By.cssSelector(".topbar");
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ParsePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void parseFile(String filePath) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(FILE_PATH)).sendKeys(filePath);
        driver.findElement(PARSE_BUTTON).click();
    }

    public boolean isDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(FILE_PATH));
            return true;
        } 
        catch (Exception e) {
            return false;
        }
    }
    public String getSignedInUser() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TOPBAR)).getText();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }
}