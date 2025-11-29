package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class DownloadManager {

    public static void downloadAudio(String url, String outputPath, Consumer<String> outputCallback) throws Exception {
        String output = outputPath + "/%(title)s.%(ext)s";
        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Users\\zkfua\\AppData\\Local\\Microsoft\\WinGet\\Packages\\yt-dlp.yt-dlp_Microsoft.Winget.Source_8wekyb3d8bbwe\\yt-dlp.exe",
                "-x", "--audio-format", "mp3",
                "--ffmpeg-location", "C:\\Users\\zkfua\\AppData\\Local\\Microsoft\\WinGet\\Packages\\yt-dlp.FFmpeg_Microsoft.Winget.Source_8wekyb3d8bbwe\\ffmpeg-N-121583-g4348bde2d2-win64-gpl\\bin\\ffmpeg.exe",
                "-o", output,
                url
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (outputCallback != null) {
                    outputCallback.accept(line + "\n");
                }
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp failed with exit code " + exitCode);
        }
    }

}
