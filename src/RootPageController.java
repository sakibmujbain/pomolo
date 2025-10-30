import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class RootPageController {
    @FXML private BorderPane rootBorderPane;
    @FXML private StackPane root;
    @FXML private ImageView backgroundImage;
    @FXML private Node playerBar;

    UserProperties up = new UserProperties();

    @FXML
    private void initialize() {
        try {
            Properties settings = up.loadProperties();
            String imagePath = settings.getProperty("background");
            File img = new File(imagePath);
            if (!img.exists()) {
                imagePath = settings.getProperty("default_background");
                up.SetProperties(imagePath);
                img = new File(imagePath);
            }
            if (img.exists()) {
                backgroundImage.setImage(new Image(img.toURI().toString()));
            } else {
                showError("Image Error", "Could not load background image: " + imagePath);
            }

            // --- MODIFIED: Use Main.class instead of getClass() ---
            Parent home = FXMLLoader.load(Main.class.getResource("/home.fxml"));
            root.getChildren().add(home);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Application Error", "An unexpected error occurred during initialization: " + e.getMessage());
        }
    }

    public void setPage(Parent node) {
        Parent currentPage = null;
        if (root.getChildren().size() > 1) {
            currentPage = (Parent) root.getChildren().get(1);
        }

        if (currentPage != null) {
            FadeTransition fadeout = new FadeTransition(Duration.millis(200), currentPage);
            fadeout.setFromValue(1.0);
            fadeout.setToValue(0.0);
            fadeout.setOnFinished(e -> {
                root.getChildren().setAll(backgroundImage, node);
                node.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), node);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeout.play();
        } else {
            root.getChildren().setAll(backgroundImage, node);
            node.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), node);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    public void SetBackgroundImage(String path) {
        try {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                backgroundImage.setImage(new Image(imgFile.toURI().toString()));
            } else {
                showError("Image Error", "Could not find the selected image file: " + path);
            }
        } catch (Exception e) {
            showError("Image Error", "An error occurred while setting the background image: " + e.getMessage());
        }
    }

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void hidePlayerBar() {
        playerBar.setVisible(false);
        playerBar.setManaged(false);
    }

    public void showPlayerBar() {
        playerBar.setVisible(true);
        playerBar.setManaged(true);
    }

    public StackPane getPageContainer() {
        return root;
    }
}