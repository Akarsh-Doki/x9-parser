package com.fcrm.fraud.x9parser.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fcrm.fraud.x9parser.selenium.support.ScreenshotHelper;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SeleniumTestBase {
    private static final Logger log = LoggerFactory.getLogger(SeleniumTestBase.class);

    @LocalServerPort
    private int port;

    @Value("${selenium.headless}")
    private boolean headless;

    @Value("${selenium.wait-seconds}")
    private int waitSeconds;

    @Value("${selenium.screenshot-dir}")
    private String screenshotDir;

    protected ScreenshotHelper screenshots;
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @BeforeEach
    void startBrowser() throws IOException{
        baseUrl = "http://localhost:" + port;

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
        screenshots = new ScreenshotHelper(driver, screenshotDir);
        log.info("Browser open, testing against {}", baseUrl);
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            try {
                driver.quit();
            } 
            catch (Exception e) {
                log.warn("Browser did not shut down cleanly: {}", e.getMessage());
            }
        }
    }

    protected void openHome() {
        driver.get(baseUrl + "/");
        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/");
    }
}
