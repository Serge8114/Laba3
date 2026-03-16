package com.example.laba3;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;

public class ImageLoader {
    public Image loadFromFile(File file) {
        try {
            if (file != null && file.exists()) {
                return new Image(new FileInputStream(file));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + (file != null ? file.getName() : "null"));
        }
        return null;
    }

    public Image loadFromResource(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            return null;
        }
    }
}