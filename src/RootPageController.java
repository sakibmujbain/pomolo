import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.util.Properties;

public class RootPageController {
    @FXML private StackPane root;
    @FXML private ImageView backgroundImage;

    UserProperties up = new UserProperties();

    @FXML
    private void initialize() throws Exception{
        //Set background
        Properties settings = up.loadProperties();
        String imagePath = settings.getProperty("background");

        // Check if the background image does exist, if not then set it to default one
        File img = new File(imagePath);
        if(!img.exists()){
            imagePath = settings.getProperty("default_background");
            up.SetProperties(imagePath);
            img = new File(imagePath);
        }
        backgroundImage.setImage(new Image(img.toURI().toString()));

        //Load home page
        Parent home = FXMLLoader.load(getClass().getResource("home.fxml"));
        root.getChildren().add(home);
    }

    //Changes the page
    public void setPage(Parent node) {
        double animationSpeed = 300;
        //Fade out
        FadeTransition fadeout = new FadeTransition(Duration.millis(animationSpeed), root);
        fadeout.setFromValue(1.0);
        fadeout.setToValue(0.0);

        fadeout.setOnFinished(e-> {
            //Change page
            root.getChildren().setAll(node);

            //Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(animationSpeed), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeout.play();
    }

    // Change Background
    public void SetBackgroundImage(String path){
        backgroundImage.setImage(new Image(path));
    }


}
