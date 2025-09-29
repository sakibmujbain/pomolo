import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Properties;

public class SettingsController {
    @FXML private AnchorPane rootPane;
    @FXML private ImageView backgroundImage;
    @FXML private Text path_text;

    UserProperties up = new UserProperties();

    @FXML
    private void initialize(){
        Properties settings = up.loadProperties();
        String imagePath = settings.getProperty("background");

        // Check if the background image does exist, if not then set it to default one
        File img = new File(imagePath);
        if(!img.exists()){
            imagePath = settings.getProperty("default_background");
            up.SetProperties(imagePath);
            img = new File(imagePath);
        }

        path_text.setText(imagePath);
    }

    @FXML
    private void chooseBackground(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Background Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) rootPane.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            String imagePath = file.getAbsolutePath();
            up.SetProperties(imagePath);
            //backgroundImage.setImage(new Image(file.toURI().toString()));
            Main.getRootController().SetBackgroundImage(file.toURI().toString());
            path_text.setText(imagePath);
            //System.out.println(imagePath);
        }


    }

    @FXML
    public void goToHome(ActionEvent e) throws Exception{
        Parent home = FXMLLoader.load(getClass().getResource("home.fxml"));
        Main.getRootController().setPage(home);

    }
}
