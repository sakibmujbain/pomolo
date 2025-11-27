package com;

import java.io.File; // Added
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class UserProperties {

    // --- New ---
    // Store the config file in the same dedicated folder
    private static final String APP_DIR = System.getProperty("user.home") + File.separator + ".LofiMusicPlayer";
    private static final String CONFIG_PATH = APP_DIR + File.separator + "config.properties";
    // --- End New ---

    public Properties loadProperties(){
        Properties config = new Properties();
        String bg_image = "/default_bg.png"; //By default
        String default_opacity = "0.0"; //Default Opacity
        String default_width = "960";
        String default_height = "540";

        // --- New ---
        // Ensure the directory exists before trying to load
        new File(APP_DIR).mkdirs();
        // --- End New ---

        // --- Updated: Now loads from the full CONFIG_PATH ---
        try(FileInputStream in = new FileInputStream(CONFIG_PATH)){
            config.load(in);
        } catch (IOException e){
            // This is safe: if config fails to load, create defaults in memory
            config.setProperty("default_background", bg_image);
            config.setProperty("background", bg_image);
        }

        //Check if overlay opacity exists, if not make one
        if(!config.containsKey("overlay_opacity")){
            config.setProperty("overlay_opacity", default_opacity);
        }

        //Check if window width and height exists, if not make one
        if(!config.containsKey("window_width")){
            config.setProperty("window_width", default_width);
        }
        if(!config.containsKey("window_height")){
            config.setProperty("window_height", default_height);
        }
        // --- End Updated ---

        return config;
    }

    private void saveConfig(Properties config) throws IOException {
        try(FileOutputStream out = new FileOutputStream(CONFIG_PATH)){
            config.store(out, "User Settings");
        }
    }

    // --- Updated: Added 'throws IOException' ---
    public void SetProperties(String backgroundImagePath) throws IOException {
        Properties config = loadProperties();
        config.setProperty("background", backgroundImagePath);
        saveConfig(config);
    }

    // Opacity Set
    public void setOverlayOpacity(double opacity) throws IOException{
        Properties config = loadProperties();
        config.setProperty("overlay_opacity", String.valueOf(opacity));
        saveConfig(config);
    }

    // Get Opacity
    public double getOverlayOpacity(){
        Properties config = loadProperties();
        String val = config.getProperty("overlay_opacity", "0.0");
        try{
            return Double.parseDouble(val);
        } catch (NumberFormatException e){
            return 0.0;
        }
    }

    // Set width
    public void setWindowWidth(double val) throws IOException{
        Properties config = loadProperties();
        config.setProperty("window_width", String.valueOf(val));
        saveConfig(config);
    }

    // Get width
    public double getWindowWidth(){
        Properties config = loadProperties();
        String val = config.getProperty("window_width", "960.0");
        try{
            return Double.parseDouble(val);
        } catch (NumberFormatException e){
            return 960.0;
        }
    }

    // Set height
    public void setWindowHeight(double val) throws IOException{
        Properties config = loadProperties();
        config.setProperty("window_height", String.valueOf(val));
        saveConfig(config);
    }

    // Get height
    public double getWindowHeight(){
        Properties config = loadProperties();
        String val = config.getProperty("window_height", "540.0");
        try{
            return Double.parseDouble(val);
        } catch (NumberFormatException e){
            return 540.0;
        }
    }

}