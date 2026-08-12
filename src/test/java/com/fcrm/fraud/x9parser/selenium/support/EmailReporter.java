package com.fcrm.fraud.x9parser.selenium.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EmailReporter {

    private static final Logger log = LoggerFactory.getLogger(EmailReporter.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String[] recipients;
    private final File screenshotFolder;

    public EmailReporter(JavaMailSender mailSender, String from, String[] recipients, String screenshotDir) {
        this.mailSender = mailSender;
        this.from = from;
        this.recipients = recipients;
        this.screenshotFolder = new File(screenshotDir);
    }

    public void sendReport(String subject, String body) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(body);

        List<File> images = findScreenshots();
        for (File image : images) {
            helper.addAttachment(image.getName(), image);
        }

        mailSender.send(message);
        log.info("Sent report with {} screenshots", images.size());
    }

    private List<File> findScreenshots() {
        List<File> images = new ArrayList<>();

        File[] files = screenshotFolder.listFiles();
        if (files == null) {
            return images;
        }
        for (File file : files) {
            if (file.getName().endsWith(".png")) {
                images.add(file);
            }
        }
        images.sort((a, b) -> a.getName().compareTo(b.getName()));
        return images;
    }
}