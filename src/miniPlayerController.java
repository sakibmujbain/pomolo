import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class miniPlayerController {

    @FXML
    private StackPane root;

    @FXML
    private Label songLabel;

    @FXML
    private Button playPauseButton;

    private double xOffset = 0;
    private double yOffset = 0;

    private final MusicPlayerManager musicManager = MusicPlayerManager.getInstance();

    @FXML
    public void initialize() {
        // === DRAG ANYWHERE ===
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // === HOVER ANIMATION ===
        root.setOnMouseEntered(e -> animateScale(1.0));  // Normal size on hover
        root.setOnMouseExited(e -> animateScale(0.85));  // Shrink when not hovered

        root.setScaleX(0.85);
        root.setScaleY(0.85);

        // === SONG TITLE ===
        var song = musicManager.currentSongProperty().get();
        if (song != null) songLabel.setText(song.fileName);

        musicManager.currentSongProperty().addListener((obs, oldSong, newSong) -> {
            if (newSong != null) songLabel.setText(newSong.fileName);
        });

        // === PLAY/PAUSE ICON SYNC ===
        updatePlayPauseIcon();

        musicManager.isPlayingProperty().addListener((obs, wasPlaying, isNowPlaying) -> {
            updatePlayPauseIcon();
        });
    }

    private void animateScale(double targetScale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(250), root);
        st.setToX(targetScale);
        st.setToY(targetScale);
        st.play();
    }

    private void updatePlayPauseIcon() {
        if (MusicPlayerManager.getInstance().isPlayingProperty().get()) {
            // Currently playing → show pause icon
            playPauseButton.setText("⏸");
            playPauseButton.setStyle(
                    "-fx-font-size: 22px; -fx-background-color: #E53935; -fx-text-fill: white; -fx-background-radius: 50%; -fx-pref-width: 50; -fx-pref-height: 50;"
            );
        } else {
            // Currently paused → show play icon
            playPauseButton.setText("▶");
            playPauseButton.setStyle(
                    "-fx-font-size: 22px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 50%; -fx-pref-width: 50; -fx-pref-height: 50;"
            );
        }
    }

    @FXML
    private void playPause() {
        musicManager.playPause();
        updatePlayPauseIcon();
    }

    @FXML
    private void next() {
        musicManager.next();
    }

    @FXML
    private void previous() {
        musicManager.previous();
    }

    @FXML
    private void restoreMain() {
        Stage miniStage = (Stage) root.getScene().getWindow();
        miniStage.close();

        Stage mainStage = (Stage) Main.getRootController().getRootPane().getScene().getWindow();
        mainStage.show();
    }
}
