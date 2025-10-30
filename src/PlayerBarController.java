// Save as: src/PlayerBarController.java (OVERWRITE)

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;

public class PlayerBarController {

    // --- Original FXML Fields ---
    @FXML private HBox playerBarPane;
    @FXML private Button prevButton;
    @FXML private Button playPauseButton;
    @FXML private Button nextButton;
    @FXML private Text currentSongText;

    // --- NEW FXML Fields ---
    @FXML private Text currentTimeText;
    @FXML private Text totalTimeText;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;

    private MusicPlayerManager playerManager;
    private boolean isSliderBeingDragged = false;

    @FXML
    private void initialize() {
        playerManager = MusicPlayerManager.getInstance();
        bindControls();

        // Add handler to go to the full player page when the text is clicked
        currentSongText.setOnMouseClicked(e -> goToPlayerPage());

        // --- NEW: Add listeners for seeking with the progress slider ---
        progressSlider.setOnMousePressed(e -> isSliderBeingDragged = true);
        progressSlider.setOnMouseReleased(e -> {
            if (playerManager.totalDurationProperty().get() != null) {
                Duration total = playerManager.totalDurationProperty().get();
                if (total != null && total.greaterThan(Duration.ZERO)) {
                    playerManager.seek(total.multiply(progressSlider.getValue() / 100.0));
                }
            }
            isSliderBeingDragged = false;
        });
    }

    private void bindControls() {
        // Bind Play/Pause button text
        playPauseButton.textProperty().bind(
                Bindings.when(playerManager.isPlayingProperty())
                        .then("Pause")
                        .otherwise("Play")
        );

        // Bind Current Song text
        currentSongText.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    SongManager.SongInfo song = playerManager.currentSongProperty().get();
                    if (song != null) {
                        String name = song.fileName;
                        if (name.length() > 30) { // Shorter length now
                            name = name.substring(0, 27) + "...";
                        }
                        return "Now Playing: " + name;
                    }
                    return "No song selected";
                }, playerManager.currentSongProperty())
        );

        // --- NEW: Bind Volume, Progress, and Time ---

        // Bind Volume Slider
        volumeSlider.valueProperty().bindBidirectional(playerManager.volumeProperty());

        // Bind Progress Slider and Time Texts
        playerManager.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!isSliderBeingDragged && playerManager.totalDurationProperty().get() != null) {
                Duration total = playerManager.totalDurationProperty().get();
                if (total != null && total.greaterThan(Duration.ZERO)) {
                    progressSlider.setValue(newTime.toSeconds() / total.toSeconds() * 100.0);
                }
            }
            currentTimeText.setText(formatDurationSimple(newTime));
        });

        playerManager.totalDurationProperty().addListener((obs, oldDur, newDur) -> {
            if (newDur != null && newDur.greaterThan(Duration.ZERO)) {
                totalTimeText.setText(formatDurationSimple(newDur));
            } else {
                totalTimeText.setText("00:00");
            }
        });
    }

    // --- Button Handlers ---
    @FXML
    private void handlePlayPause() {
        playerManager.playPause();
    }
    @FXML
    private void handleNext() {
        playerManager.next();
    }
    @FXML
    private void handlePrevious() {
        playerManager.previous();
    }

    // --- Navigation ---
    @FXML
    private void goToPlayerPage() {
        try {
            Parent currentPage = (Parent) Main.getRootController().getPageContainer().getChildren().get(1);
            if (currentPage.getUserData() != null && currentPage.getUserData().equals("playerPage")) {
                return;
            }
            Parent playerPage = FXMLLoader.load(Main.class.getResource("/PlayerPage.fxml"));
            Main.getRootController().setPage(playerPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- NEW: Helper method copied from PlayerPageController ---
    /**
     * Formats a Duration object to a simple M:S string.
     * @param duration The Duration object
     * @return Formatted string (e.g., "02:03")
     */
    private String formatDurationSimple(Duration duration) {
        if (duration == null || duration.isUnknown()) return "00:00";
        long seconds = (long) duration.toSeconds();
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}