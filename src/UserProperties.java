import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class UserProperties {
    public Properties loadProperties(){
        Properties config = new Properties();
        String bg_image = "Background.jpg"; //By default
        try(FileInputStream in = new FileInputStream("config.properties")){
            config.load(in);
        } catch (IOException e){
            config.setProperty("default_background", bg_image);
            config.setProperty("background", bg_image);
        }

        return config;
    }

    public void SetProperties(String backgroundImagePath){
        Properties config = loadProperties();
        config.setProperty("background", backgroundImagePath);

        //Save it
        try(FileOutputStream out = new FileOutputStream("config.properties")){
            config.store(out, "Set Settings");
        } catch (IOException e){
            e.printStackTrace();
        }

    }


}
