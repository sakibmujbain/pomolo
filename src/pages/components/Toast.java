package pages.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {

    public static void show(String message, Stage stage) {
        show(message, stage, null);
    }

    public static void show(String message, Stage stage, Runnable onFinished) {
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        if (!(stage.getScene().getRoot() instanceof StackPane)) {
            System.err.println("The root of the scene must be a StackPane for Toast notifications to work.");
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        StackPane root = (StackPane) stage.getScene().getRoot();

        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: rgba(30, 30, 30, 0.9);" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10px 20px;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-color: #a481ee;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 10px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);"
        );
        label.setOpacity(0);

        Platform.runLater(() -> {
            root.getChildren().add(label);
            StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
            label.setTranslateY(-100);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), label);
            slideIn.setFromY(-90);
            slideIn.setToY(-100);

            fadeIn.play();
            slideIn.play();

            PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
            pause.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    root.getChildren().remove(label);
                    if (onFinished != null) {
                        onFinished.run();
                    }
                });
                fadeOut.play();
            });
            pause.play();
        });
    }
}
