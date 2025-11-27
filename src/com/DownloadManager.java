package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadManager {

    public static String downloadAudio(String url, String outputPath, Consumer<String> outputCallback) throws Exception {
        String outputTemplate;
        if (outputPath == null || outputPath.isEmpty()) {
            outputTemplate = "%(title)s.%(ext)s";
        } else {
            outputTemplate = outputPath + "/%(title)s.%(ext)s";
        }
        ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "-x", "--audio-format", "mp3",
                "-o", outputTemplate,
                url
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        final String[] finalFilePath = {null};
        Pattern pattern = Pattern.compile("\\[ExtractAudio\\] Destination: (.*)");


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (outputCallback != null) {
                    outputCallback.accept(line + "\n");
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    finalFilePath[0] = matcher.group(1);
                }
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp failed with exit code " + exitCode);
        }
        if (finalFilePath[0] == null) {
            throw new RuntimeException("Could not determine the downloaded file path.");
        }
        return finalFilePath[0];
    }

}
