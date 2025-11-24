package pages.home;

import com.Main;
import com.MusicPlayerManager;
import com.SongManager;
import com.SqliteDBManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import pages.all_songs.AllSongsPageController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class HomeController {

    @FXML private AnchorPane rootPane;
    @FXML private VBox vbox;

    private MusicPlayerManager playerManager;
    private List<SongManager.SongInfo> loadedSongs;

    @FXML
    private void initialize(){
        playerManager = MusicPlayerManager.getInstance();
        loadSongs();
        if (loadedSongs != null) {
            playerManager.setQueue(loadedSongs);
        }
    }

    @FXML
    public void goToSettings(ActionEvent e) throws Exception{
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pages/settings/settings.fxml"));
        Parent settings = loader.load();
        settings.getProperties().put("controller", loader.getController());
        Main.getRootController().setPage(settings);
    }

    @FXML
    public void goToPlaylists() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pages/playlists/playlists.fxml"));
        Parent playlists = loader.load();
        playlists.getProperties().put("controller", loader.getController());
        Main.getRootController().setPage(playlists);
    }

    @FXML
    public void goToPomodoro(ActionEvent e) throws Exception{
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/pages/pomodoro/Pomodoro.fxml"));
        Parent pomodoro = loader.load();
        pomodoro.getProperties().put("controller", loader.getController());
        Main.getRootController().setPage(pomodoro);
    }

    @FXML
    private void AddNewMusic(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Music File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Music Files", "*.mp3", "*.wav", "*.flac")
        );
        Stage stage = (Stage) rootPane.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()){
            for (File file : files) {
                try {
                    if (!SqliteDBManager.songExists(file.getAbsolutePath())) {
                        SongManager.SongInfo music = SongManager.readMp3(file);
                        if (music != null) {
                            SqliteDBManager.insertNewSong(music);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            loadSongs();
            if (loadedSongs != null) {
                playerManager.setQueue(loadedSongs);
            }
        }
    }

    @FXML
    private void importFolder() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Folder to Import");
        Stage stage = (Stage) rootPane.getScene().getWindow();
        File dir = dirChooser.showDialog(stage);
        if (dir == null || !dir.isDirectory()) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                scanAndImport(dir);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                loadSongs();
                if (loadedSongs != null) {
                    playerManager.setQueue(loadedSongs);
                }
            });
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
        });

        Thread th = new Thread(task, "ImportFolderTask");
        th.setDaemon(true);
        th.start();
    }

    private void scanAndImport(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanAndImport(f);
            } else {
                String name = f.getName().toLowerCase();
                if ((name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac")) && !SqliteDBManager.songExists(f.getAbsolutePath())) {
                    try {
                        SongManager.SongInfo music = SongManager.readMp3(f);
                        if (music != null) {
                            SqliteDBManager.insertNewSong(music);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void loadSongs(){
        loadedSongs = SqliteDBManager.getAllSongs();
        vbox.getChildren().clear();
        int index = 0;
        for(SongManager.SongInfo s : loadedSongs){
            vbox.getChildren().add(createSongRow(s, index));
            index++;
        }
    }

    public HBox createSongRow(SongManager.SongInfo song, int index){
        HBox songRow = new HBox();
        songRow.setPrefHeight(40.0);
        songRow.setMaxWidth(Double.MAX_VALUE);
        songRow.getStyleClass().add("row-box");
        songRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Play song on click, but not if the delete button was the source
        songRow.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Button || e.getTarget() instanceof FontIcon)) {
                playerManager.setQueue(loadedSongs);
                playerManager.playSong(index);
            }
        });

        GridPane grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setPrefHeight(30.0);
        grid.setPadding(new Insets(0, 10, 0, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.LEFT);
        col1.setPercentWidth(40);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(HPos.CENTER);
        col2.setPercentWidth(30);
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHalignment(HPos.RIGHT);
        col3.setPercentWidth(30);
        col3.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        String fileName = song.fileName;
        if(fileName.length() > 30){
            fileName = fileName.substring(0, 27) + "...";
        }
        Text titleText = new Text(fileName);
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("Monospaced", 13));

        Text artistText = new Text(song.artist);
        artistText.setFill(Color.WHITE);
        artistText.setFont(Font.font("Monospaced", 13));

        Text durationText = new Text(formatDuration(song.duration));
        durationText.setFill(Color.WHITE);
        durationText.setFont(Font.font("Monospaced", 13));

        grid.add(titleText, 0, 0);
        grid.add(artistText, 1, 0);
        grid.add(durationText, 2, 0);

        // --- DELETE BUTTON ---
        Button deleteButton = new Button();
        deleteButton.setGraphic(new FontIcon("fas-trash-alt"));
        deleteButton.getStyleClass().add("delete-btn");
        deleteButton.setOnAction(e -> {
            handleDeleteSong(song);
            e.consume(); // Prevents the row's click event from firing
        });

        songRow.getChildren().addAll(grid, deleteButton);
        HBox.setHgrow(grid, Priority.ALWAYS);

        return songRow;
    }

    private void handleDeleteSong(SongManager.SongInfo song) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Song");
        confirmation.setHeaderText("Are you sure you want to delete this song?");
        confirmation.setContentText(song.fileName);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SqliteDBManager.deleteSong(song.path);
            loadSongs(); // Refresh the list
        }
    }

    private String formatDuration(int seconds){
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;

        if(h>0){
            return String.format("%d hr %02d min %02d s", h, m, s);
        }else{
            return String.format("%02d min %02d s", m, s);
        }
    }
}
