import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class PlayerBarController {

    @FXML private HBox playerBarPane;
    @FXML private Button prevButton;
    @FXML private Button playPauseButton;
    @FXML private Button nextButton;
    @FXML private Text currentSongText;

    private MusicPlayerManager playerManager;

    @FXML
    private void initialize() {
        playerManager = MusicPlayerManager.getInstance();
        bindControls();
        currentSongText.setOnMouseClicked(e -> goToPlayerPage());
    }

    private void bindControls() {
        playPauseButton.textProperty().bind(
                Bindings.when(playerManager.isPlayingProperty())
                        .then("Pause")
                        .otherwise("Play")
        );

        currentSongText.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    SongManager.SongInfo song = playerManager.currentSongProperty().get();
                    if (song != null) {
                        String name = song.fileName;
                        if (name.length() > 60) {
                            name = name.substring(0, 57) + "...";
                        }
                        return "Now Playing: " + name;
                    }
                    return "No song selected";
                }, playerManager.currentSongProperty())
        );
    }

    @FXML private void handlePlayPause() { playerManager.playPause(); }
    @FXML private void handleNext() { playerManager.next(); }
    @FXML private void handlePrevious() { playerManager.previous(); }

    @FXML
    private void goToPlayerPage() {
        try {
            Parent currentPage = (Parent) Main.getRootController().getPageContainer().getChildren().get(1);
            if (currentPage.getUserData() != null && currentPage.getUserData().equals("playerPage")) {
                return;
            }
            // --- MODIFIED: Use Main.class ---
            Parent playerPage = FXMLLoader.load(Main.class.getResource("/PlayerPage.fxml"));
            Main.getRootController().setPage(playerPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}