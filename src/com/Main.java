package com;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import pages.root.RootPageController;
import com.UserProperties;

public class Main extends Application {
    public static RootPageController rootController;
    private static Stage primaryStage;
    private static javafx.beans.value.ChangeListener<Number> widthListener;
    private static javafx.beans.value.ChangeListener<Number> heightListener;
    private static boolean resizingAdjusting = false;
    private static double fixedRatio = 16.0 / 9.0; // default

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pages/root/rootPage.fxml"));
        Parent root = loader.load();
        rootController = loader.getController();
        primaryStage = stage;

        Scene scene = new Scene(root, 1280, 720);
        //Scene scene = new Scene(root, 960, 540);
        scene.getStylesheets().add(Main.class.getResource("/css/home.css").toExternalForm());

        // 1. Remove default window decorations
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Pomolo");
        // allow resizing so user can change window size; fixed-aspect will be enforced if enabled
        stage.setResizable(true);

        stage.setScene(scene);

        // Apply user preferred fixed-aspect settings if any (sanitize stored ratio)
        try {
            UserProperties up = new UserProperties();
            boolean enabled = up.getFixedAspectEnabled();
            double ratio = up.getFixedAspectRatio();
            // sanitize: acceptable bounds 0.5..3.0 (width/height). If out of bounds, reset to 16:9
            if (Double.isNaN(ratio) || ratio <= 0 || ratio < 0.5 || ratio > 3.0) {
                ratio = 16.0 / 9.0;
                up.setFixedAspectRatio(ratio);
            }
            setFixedAspectRatioEnabled(enabled, ratio);
        } catch (Exception ignored) {}

        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @Override
    public void stop() throws Exception {
        MusicPlayerManager.getInstance().shutdown();
        super.stop();
    }

    public static RootPageController getRootController(){
        return rootController;
    }

    /**
     * Enable or disable a fixed window aspect ratio. When enabled, window width/height are kept to ratio.
     * The ratio is width/height (e.g. 16.0/9.0).
     */
    public static void setFixedAspectRatioEnabled(boolean enabled, double ratio) {
        if (primaryStage == null) return;
        // remove existing listeners first
        if (widthListener != null) primaryStage.widthProperty().removeListener(widthListener);
        if (heightListener != null) primaryStage.heightProperty().removeListener(heightListener);
        widthListener = null; heightListener = null;
        fixedRatio = ratio;

        if (!enabled) return;

        widthListener = (obs, old, nw) -> {
            if (resizingAdjusting) return;
            try {
                resizingAdjusting = true;
                double newWidth = nw.doubleValue();
                double newHeight = Math.max(100, newWidth / fixedRatio);
                primaryStage.setHeight(newHeight);
            } finally {
                resizingAdjusting = false;
            }
        };

        heightListener = (obs, old, nh) -> {
            if (resizingAdjusting) return;
            try {
                resizingAdjusting = true;
                double newHeight = nh.doubleValue();
                double newWidth = Math.max(200, newHeight * fixedRatio);
                primaryStage.setWidth(newWidth);
            } finally {
                resizingAdjusting = false;
            }
        };

        primaryStage.widthProperty().addListener(widthListener);
        primaryStage.heightProperty().addListener(heightListener);
    }

    /**
     * Show the mini player in its own undecorated, non-resizable stage.
     * Keeps styling consistent by adding the dark theme stylesheet if available.
     */
    public static void showMiniPlayer(Parent miniRoot) {
        Stage miniStage = new Stage();
        // prefer the mini player's declared stylesheet, but ensure dark-theme is applied
        Scene miniScene = new Scene(miniRoot);
        try {
            var css = Main.class.getResource("/css/dark-theme.css");
            if (css != null) miniScene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {
        }
        miniStage.initStyle(StageStyle.UNDECORATED);
        miniStage.setResizable(false);
        miniStage.setAlwaysOnTop(true);
        miniStage.setScene(miniScene);
        miniStage.show();
    }
}