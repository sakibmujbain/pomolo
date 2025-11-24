package pages.root;

import com.Main;
import com.SongManager;
import com.SqliteDBManager;
import com.UserProperties;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import pages.all_songs.AllSongsPageController;
import pages.home.HomeController;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
        root.setOpacity(0.0);

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

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pages/home/home.fxml"));
            Parent home = loader.load();
            home.getProperties().put("controller", loader.getController());
            pageContainer.getChildren().add(home);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Application Error", "An unexpected error occurred during initialization: " + e.getMessage());
        }

        backgroundImage.fitWidthProperty().bind(root.widthProperty());
        backgroundImage.fitHeightProperty().bind(root.heightProperty());
        overlayRect.widthProperty().bind(((Region) overlayRect.getParent()).widthProperty());
        overlayRect.heightProperty().bind(((Region) overlayRect.getParent()).heightProperty());
        setOverlayOpacity(up.getOverlayOpacity());
        pageContainer.prefWidthProperty().bind(root.widthProperty());
        pageContainer.prefHeightProperty().bind(root.heightProperty().subtract(130));
        setupResizeGrip();
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        final String dragOverStyle = "drag-over";

        root.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                if (!root.getStyleClass().contains(dragOverStyle)) {
                    root.getStyleClass().add(dragOverStyle);
                }
            } else {
                event.consume();
            }
        });

        root.setOnDragExited(event -> {
            root.getStyleClass().remove(dragOverStyle);
        });

        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                List<File> supportedFiles = files.stream()
                        .filter(f -> {
                            String lowerCaseName = f.getName().toLowerCase();
                            return lowerCaseName.endsWith(".mp3")
                                    || lowerCaseName.endsWith(".wav")
                                    || lowerCaseName.endsWith(".flac");
                        })
                        .collect(Collectors.toList());

                int importedCount = 0;
                for (File file : supportedFiles) {
                    try {
                        // --- FIX: CHECK FOR DUPLICATES BEFORE INSERTING ---
                        if (!SqliteDBManager.songExists(file.getAbsolutePath())) {
                            SongManager.SongInfo songInfo = SongManager.readMp3(file);
                            if (songInfo != null) {
                                SqliteDBManager.insertNewSong(songInfo);
                                importedCount++;
                            }
                        }
                        // --- END FIX ---
                    } catch (Exception e) {
                        System.err.println("Failed to import file: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
                System.out.println("Successfully imported " + importedCount + " new files.");
                success = true;
                refreshCurrentPage();
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void refreshCurrentPage() {
        if (pageContainer.getChildren().isEmpty()) return;

        Parent currentPage = (Parent) pageContainer.getChildren().get(0);
        Object controller = currentPage.getProperties().get("controller");

        if (controller instanceof HomeController) {
            ((HomeController) controller).loadSongs();
        } else if (controller instanceof AllSongsPageController) {
            ((AllSongsPageController) controller).loadSongs();
        }
    }

    public void setPage(Parent node) {
        Parent currentPage = pageContainer.getChildren().isEmpty() ? null : (Parent) pageContainer.getChildren().get(0);

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
            showError("Image Error", "An error occurred while setting the background image: " + e.getMessage());
        }
    }

    public void setOverlayOpacity(double value) {
        overlayRect.setOpacity(value);
    }

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public StackPane getPageContainer() { return pageContainer; }
    public StackPane getRootPane() { return root; }

    private void setupResizeGrip() {
        if (resizeGrip == null) return;
        final Delta d = new Delta();
        final boolean[] resizing = {false};
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
            stage.setWidth(ev.getSceneX() + d.x);
            stage.setHeight(ev.getSceneY() + d.y);
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

class Delta { double x, y; }
