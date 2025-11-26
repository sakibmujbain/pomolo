package pages.download;

import com.Main;
import com.DownloadManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class DownloadPageController {

    @FXML TextField linkInput;
    @FXML TextArea terminalOutput;


    @FXML
    public void download(ActionEvent e) throws Exception{
        // Clear previous output
        terminalOutput.clear();

        // Run download in separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                DownloadManager.downloadAudio(
                        linkInput.getText(),
                        "",
                        line -> Platform.runLater(() -> terminalOutput.appendText(line))
                );
                Platform.runLater(() -> terminalOutput.appendText("\nDownload completed successfully!\n"));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    terminalOutput.appendText("\nError: " + ex.getMessage() + "\n");
                    showError("Download Error", "Failed to download: " + ex.getMessage());
                });
            }
        }).start();
    }

    @FXML
    public void goToHome(ActionEvent e) {
        try {
            Parent home = FXMLLoader.load(Main.class.getResource("/pages/home/home.fxml"));
            Main.getRootController().setPage(home);
        } catch (IOException ioException) {
            showError("Navigation Error", "Could not load home page: " + ioException.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
