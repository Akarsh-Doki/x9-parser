package com.fcrm.fraud.x9parser.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ResultPage {
    private static final By HEADING = By.tagName("h1");
    private static final By SUMMARY = By.cssSelector(".summary");
    private static final By OUTPUT_TABLE = By.cssSelector(".images-table");
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ResultPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isDisplayed() {
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    HEADING, "File parsed successfully"));
            return true;
        } 
        catch (Exception e) {
            return false;
        }
    }
    public String getSummaryText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(SUMMARY)).getText();
    }

    public String getOutputPaths() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(OUTPUT_TABLE)).getText();
    }
}