package com.example.laba3;

import javafx.animation.*;
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
    private Button btnFirst, btnPrev, btnNext, btnLast, btnEffect, btnPlay;
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

    private final String[] EFFECT_NAMES = {
            " Вспышка",
            " Полный оборот",
            " Свайп"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageLoader = new ImageLoader();
        imageView.setPreserveRatio(true);

        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());

        File imagesDir = new File("images");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            loadImagesFromDirectory(imagesDir);
        } else {
            showEmptyState();
        }

        setupButtonHandlers();
        updateEffectLabel();
    }

    private void setupButtonHandlers() {
        btnNext.setOnAction(event -> nextImage());
        btnPrev.setOnAction(event -> previousImage());
        btnFirst.setOnAction(event -> firstImage());
        btnLast.setOnAction(event -> lastImage());
        btnEffect.setOnAction(event -> switchEffect());
        btnPlay.setOnAction(event -> toggleSlideshow());
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
                    imageContainer.setRotate(0);
                    imageContainer.setScaleX(1);
                    imageContainer.setScaleY(1);
                    imageContainer.setTranslateX(0);
                    imageContainer.setOpacity(1.0);
                }
            }
        }
    }

    private void nextImage() {
        if (iterator != null && iterator.hasNext()) {
            applyEffect();
            iterator.next();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void previousImage() {
        if (iterator != null && iterator.hasPrevious()) {
            applyEffect();
            iterator.previous();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void firstImage() {
        if (iterator != null && imageCollection != null && !imageCollection.isEmpty()) {
            applyEffect();
            iterator.goToFirst();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void lastImage() {
        if (iterator != null && imageCollection != null && !imageCollection.isEmpty()) {
            applyEffect();
            iterator.goToLast();
            displayCurrentImage();
            updateInfo();
        }
    }

    private void applyEffect() {
        switch (currentEffect) {
            case 0:
                flashEffect();
                break;
            case 1:
                rotateEffect();
                break;
            case 2:
                swipeEffect();
                break;
        }
    }

    private void flashEffect() {
        ParallelTransition pt = new ParallelTransition(imageContainer);

        FadeTransition ft = new FadeTransition(Duration.millis(300), imageContainer);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(300), imageContainer);
        st.setFromX(1.2);
        st.setFromY(1.2);
        st.setToX(1.0);
        st.setToY(1.0);

        pt.getChildren().addAll(ft, st);
        pt.play();
    }

    private void rotateEffect() {
        RotateTransition rt = new RotateTransition(Duration.millis(600), imageContainer);
        rt.setFromAngle(0);
        rt.setToAngle(360);
        rt.setInterpolator(Interpolator.EASE_BOTH);
        rt.play();
    }

    private void swipeEffect() {
        boolean fromRight = Math.random() > 0.5;

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), imageContainer);
        if (fromRight) {
            tt.setFromX(100);
        } else {
            tt.setFromX(-100);
        }
        tt.setToX(0);

        FadeTransition ft = new FadeTransition(Duration.millis(400), imageContainer);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(imageContainer);
        pt.getChildren().addAll(tt, ft);
        pt.play();
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
                    Thread.sleep(2000);
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