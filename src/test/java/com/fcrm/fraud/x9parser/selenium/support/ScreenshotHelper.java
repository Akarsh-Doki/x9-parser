package com.fcrm.fraud.x9parser.selenium.support;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotHelper {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotHelper.class);

    private final WebDriver driver;
    private final Path folder;
    private final List<Path> screenshots = new ArrayList<>();

    private int counter=0;
    public ScreenshotHelper(WebDriver driver, String screenshotDir) throws IOException {
       this.driver = driver;
       this.folder = Path.of(screenshotDir);
       Files.createDirectories(folder);
    }

    public void capture(String name) throws IOException{
        counter++;
        String fileName = String.format("%02d-%s.png", counter, name);
        File tempImage = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        Path destination = folder.resolve(fileName);
        Files.copy(tempImage.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        screenshots.add(destination);
        log.info("Saved screenshot {}", destination);
    }

    public List<Path> getScreenshots(){
        return screenshots;
    }


}
