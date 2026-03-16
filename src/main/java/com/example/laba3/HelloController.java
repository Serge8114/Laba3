package com.example.laba3;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    private ImageView imageView;
    @FXML
    private Button btnPrev, btnNext, btnFirst, btnLast, btnEffect, btnPlay;
    @FXML
    private Label lblPosition, lblFileName, lblFileInfo, lblEffect;
    @FXML
    private StackPane imageContainer;

    private ImageCollection imageCollection;
    private ImageCollection.ImageFileIterator iterator;
    private ImageLoader imageLoader;
    private boolean isPlaying = false;
    private Thread slideshowThread;
    private int currentEffect = 0;
    private final String[] EFFECT_NAMES = {"Fade", "Scale", "Translate", "Rotate"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageLoader = new ImageLoader();
        imageView.setPreserveRatio(true);

        File imagesDir = new File("images");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            loadImagesFromDirectory(imagesDir);
        } else {
            showEmptyState();
        }

        setupButtonHandlers();
        updateEffectLabel();

        imageContainer.widthProperty().addListener((obs, oldVal, newVal) -> resizeImage());
        imageContainer.heightProperty().addListener((obs, oldVal, newVal) -> resizeImage());
    }

    private void setupButtonHandlers() {
        btnNext.setOnAction(e -> nextImage());
        btnPrev.setOnAction(e -> previousImage());
        btnFirst.setOnAction(e -> firstImage());
        btnLast.setOnAction(e -> lastImage());
        btnEffect.setOnAction(e -> switchEffect());
        btnPlay.setOnAction(e -> toggleSlideshow());
    }

    private void switchEffect() {
        currentEffect = (currentEffect + 1) % EFFECT_NAMES.length;
        updateEffectLabel();
    }

    private void updateEffectLabel() {
        lblEffect.setText("Эффект: " + EFFECT_NAMES[currentEffect]);
    }

    private void loadImagesFromDirectory(File directory) {
        imageCollection = new ImageCollection(directory);

        if (!imageCollection.isEmpty()) {
            iterator = (ImageCollection.ImageFileIterator) imageCollection.getIterator();
            displayCurrentImage();
            updateInfo();
        } else {
            showEmptyState();
        }
    }

    private void displayCurrentImage() {
        if (iterator != null && imageCollection != null && !imageCollection.isEmpty()) {
            File currentFile = iterator.getCurrentFile();
            if (currentFile != null) {
                Image image = imageLoader.loadFromFile(currentFile);
                if (image != null) {
                    imageView.setImage(image);
                    resizeImage();
                }
            }
        }
    }

    private void resizeImage() {
        if (imageView.getImage() != null) {
            double containerWidth = imageContainer.getWidth();
            double containerHeight = imageContainer.getHeight();

            if (containerWidth > 0 && containerHeight > 0) {
                Image img = imageView.getImage();
                double imgWidth = img.getWidth();
                double imgHeight = img.getHeight();
                double containerRatio = containerWidth / containerHeight;
                double imageRatio = imgWidth / imgHeight;

                if (imageRatio > containerRatio) {
                    imageView.setFitWidth(containerWidth);
                    imageView.setFitHeight(0);
                } else {
                    imageView.setFitHeight(containerHeight);
                    imageView.setFitWidth(0);
                }
            }
        }
    }

    private void nextImage() {
        if (iterator != null && iterator.hasNext()) {
            applyCurrentEffect();
            iterator.next();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void previousImage() {
        if (iterator != null && iterator.hasPrevious()) {
            applyCurrentEffect();
            iterator.previous();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void firstImage() {
        if (iterator != null && imageCollection != null && !imageCollection.isEmpty()) {
            applyCurrentEffect();
            iterator.goToFirst();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void lastImage() {
        if (iterator != null && imageCollection != null && !imageCollection.isEmpty()) {
            applyCurrentEffect();
            iterator.goToLast();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void applyCurrentEffect() {
        switch (currentEffect) {
            case 0:
                FadeTransition ft = new FadeTransition(Duration.millis(500), imageView);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
                break;
            case 1:
                ScaleTransition st = new ScaleTransition(Duration.millis(500), imageView);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
                break;
            case 2:
                TranslateTransition tt = new TranslateTransition(Duration.millis(400), imageView);
                tt.setFromX(50);
                tt.setToX(0);
                tt.play();
                break;
            case 3:
                RotateTransition rt = new RotateTransition(Duration.millis(500), imageView);
                rt.setFromAngle(-15);
                rt.setToAngle(0);
                rt.play();
                break;
        }
    }

    private void updateInfo() {
        if (iterator != null && imageCollection != null && !imageCollection.isEmpty()) {
            int current = iterator.getCurrentIndex() + 1;
            int total = imageCollection.size();
            lblPosition.setText(current + " из " + total);

            File file = iterator.getCurrentFile();
            if (file != null) {
                lblFileName.setText(file.getName());
                long fileSize = file.length() / 1024;
                String extension = getFileExtension(file.getName());
                lblFileInfo.setText(String.format("%s | %d Кб", extension.toUpperCase(), fileSize));
            }
        } else {
            lblPosition.setText("0 из 0");
            lblFileName.setText("нет картинок");
            lblFileInfo.setText("");
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    private void showEmptyState() {
        imageView.setImage(null);
        updateInfo();
    }

    private void toggleSlideshow() {
        if (imageCollection == null || imageCollection.isEmpty()) {
            return;
        }

        if (isPlaying) {
            stopSlideshow();
            btnPlay.setText("▶");
        } else {
            startSlideshow();
            btnPlay.setText("⏸");
        }
        isPlaying = !isPlaying;
    }

    private void startSlideshow() {
        slideshowThread = new Thread(() -> {
            while (isPlaying) {
                try {
                    Thread.sleep(3000);
                    if (isPlaying) {
                        javafx.application.Platform.runLater(() -> nextImage());
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        slideshowThread.setDaemon(true);
        slideshowThread.start();
    }

    private void stopSlideshow() {
        isPlaying = false;
        if (slideshowThread != null) {
            slideshowThread.interrupt();
            slideshowThread = null;
        }
    }
}