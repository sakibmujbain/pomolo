package pages.root;

import com.Main;
import com.UserProperties;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.util.Properties;

public class RootPageController {

    @FXML private StackPane root;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane pageContainer;
    @FXML private VBox playerBar;
    @FXML private Rectangle overlayRect;
    @FXML private javafx.scene.layout.Region resizeGrip;

    UserProperties up = new UserProperties();

    @FXML
    private void initialize() {
        // --- THIS IS THE FIX ---
        // 1. Make the root invisible, so it can be faded in later.
        root.setOpacity(0.0);
        // --- END FIX ---

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

            // Load the home page
            Parent home = FXMLLoader.load(Main.class.getResource("/pages/home/home.fxml"));

            // 2. Add the home page (it will be invisible as it's part of 'root
            pageContainer.getChildren().add(home);

            // 3. --- REMOVED FADE-IN LOGIC FROM HERE ---

        } catch (Exception e) {
            e.printStackTrace();
            showError("Application Error", "An unexpected error occurred during initialization: " + e.getMessage());
        }

        backgroundImage.fitWidthProperty().bind(root.widthProperty());
        backgroundImage.fitHeightProperty().bind(root.heightProperty());

        overlayRect.widthProperty().bind(((Region) overlayRect.getParent()).widthProperty());
        overlayRect.heightProperty().bind(((Region) overlayRect.getParent()).heightProperty());
        setOverlayOpacity(up.getOverlayOpacity());

        // Bind pageContainer to fill width and adjust height (leaving 100px for player bar)
        pageContainer.prefWidthProperty().bind(root.widthProperty());
        pageContainer.prefHeightProperty().bind(root.heightProperty().subtract(130)); // 30 for title bar + 100 for player bar

        // Setup bottom-right resize grip drag handling
        if (resizeGrip != null) {
            // apply CSS class so dark-theme.css can style the grip
            resizeGrip.getStyleClass().add("resize-grip");

            // Keep the grip non-interactive so it never blocks clicks; we'll handle cursor and drag on the Scene.
            resizeGrip.setMouseTransparent(true);
            resizeGrip.setOpacity(0.12);
            // inset the grip slightly so it sits inside the UI corner
            resizeGrip.setTranslateX(-8);
            resizeGrip.setTranslateY(-8);

            // Now attach handlers directly to the small Region so only that corner area changes cursor and resizes.
            final Delta d = new Delta();
            final boolean[] resizing = {false};

            // Make sure the Region actually receives mouse events (not mouseTransparent)
            resizeGrip.setMouseTransparent(false);
            resizeGrip.toFront();

            resizeGrip.setOnMouseEntered(ev -> {
                resizeGrip.setOpacity(0.9);
                resizeGrip.setCursor(javafx.scene.Cursor.SE_RESIZE);
            });

            resizeGrip.setOnMouseExited(ev -> {
                if (!resizing[0]) {
                    resizeGrip.setOpacity(0.12);
                    resizeGrip.setCursor(javafx.scene.Cursor.DEFAULT);
                }
            });

            resizeGrip.setOnMousePressed(ev -> {
                if (!ev.isPrimaryButtonDown()) return;
                var stage = (javafx.stage.Stage) root.getScene().getWindow();
                d.x = stage.getWidth() - ev.getSceneX();
                d.y = stage.getHeight() - ev.getSceneY();
                resizing[0] = true;
                ev.consume();
            });

            resizeGrip.setOnMouseDragged(ev -> {
                if (!resizing[0]) return;
                var stage = (javafx.stage.Stage) root.getScene().getWindow();
                double newW = ev.getSceneX() + d.x;
                double newH = ev.getSceneY() + d.y;
                newW = Math.max(300, newW);
                newH = Math.max(200, newH);
                try {
                    UserProperties up = new UserProperties();
                    if (up.getFixedAspectEnabled()) {
                        double ratio = readSanitizedRatio(up);
                        newH = Math.round(newW / ratio);
                    }
                } catch (Exception ignored) {}
                stage.setWidth(newW);
                stage.setHeight(newH);
                ev.consume();
            });

            resizeGrip.setOnMouseReleased(ev -> {
                resizing[0] = false;
                resizeGrip.setOpacity(0.12);
                resizeGrip.setCursor(javafx.scene.Cursor.DEFAULT);
                ev.consume();
            });
        }
    }

    // Page Navigation (This part is for page-to-page and is correct)
    public void setPage(Parent node) {
        Parent currentPage = null;
        if (pageContainer.getChildren().size() > 0) {
            currentPage = (Parent) pageContainer.getChildren().get(0);
        }

        if (currentPage != null) {
            FadeTransition fadeout = new FadeTransition(Duration.millis(300), currentPage);
            fadeout.setFromValue(1.0);
            fadeout.setToValue(0.0);
            fadeout.setOnFinished(e -> {
                pageContainer.getChildren().setAll(node);
                node.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeout.play();
        } else {
            pageContainer.getChildren().setAll(node);
            node.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
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
            // --- FIXING TYPO: Removed stray "D" ---
            showError("Image Error", "An error occurred while setting the background image: " + e.getMessage());
        }
    }

    // Control Overlay Rect opacity
    public void setOverlayOpacity(double value) {
        overlayRect.setOpacity(value);
    }

    public double getOverlayOpacity() {
        return overlayRect.getOpacity();
    }

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void hidePlayerBar() {
        if (playerBar != null) {
            playerBar.setVisible(false);
            playerBar.setManaged(false);
        }
    }

    public void showPlayerBar() {
        if (playerBar != null) {
            playerBar.setVisible(true);
            playerBar.setManaged(true);
        }
    }

    public StackPane getPageContainer() {
        return pageContainer;
    }

    public StackPane getRootPane() {
        return root;
    }

    // Read ratio from properties, sanitize it, and persist correction if out of bounds.
    private double readSanitizedRatio(UserProperties up) {
        double ratio = up.getFixedAspectRatio();
        // Acceptable bounds (width/height): between 0.5 (tall) and 3.0 (wide)
        if (Double.isNaN(ratio) || ratio <= 0 || ratio < 0.5 || ratio > 3.0) {
            double fallback = 4.0 / 3.0;
            try {
                up.setFixedAspectRatio(fallback);
            } catch (Exception ignored) {}
            return fallback;
        }
        return ratio;
    }
}

class Delta { double x, y; }
