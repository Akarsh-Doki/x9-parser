package com.fcrm.fraud.x9parser.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Writes each check image straight to disk as it's found, so the images are never all held in memory at once.
public class ImageWriter {

    private final Path imagesDir;

    public ImageWriter(Path imagesDir) throws IOException {
        this.imagesDir = imagesDir;
        Files.createDirectories(imagesDir);
    }
    public void write(String fileName, byte[] imageBytes) throws IOException {
        Files.write(imagesDir.resolve(fileName), imageBytes);
    }
}