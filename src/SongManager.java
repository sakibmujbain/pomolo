import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class SongManager {

    public static class SongInfo{
        public final String fileName;
        public final String path;
        public final String artist;
        public final Integer duration;

        SongInfo (String fileName, String path, String artist, Integer duration){
            this.fileName = fileName;
            this.path = path;
            this.artist = artist;
            this.duration = duration;
        }
    }

    public static SongInfo readMp3(File mp3){
        String fileName = mp3.getName();
        String path = mp3.getAbsolutePath();
        String artist = "Unknown Artist";
        Integer durationSeconds = null;

        try{
            AudioFile audioFile = AudioFileIO.read(mp3);

            // Get Artist Name
            Tag tag = audioFile.getTag();
            if(tag != null){
                String a = tag.getFirst(FieldKey.ARTIST);
                if(a != null && !a.trim().isBlank())
                    artist = a.trim();
            }

            // Get audio duration
            AudioHeader header = audioFile.getAudioHeader();
            if(header!=null){
                int len = header.getTrackLength(); // in seconds
                if(len > 0)
                    durationSeconds = Integer.valueOf(len);
            }

            return new SongInfo(fileName, path, artist, durationSeconds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        File file = new File("Calm Your Anxiety.mp3");
        SongInfo info = readMp3(file);
        System.out.println("Filename: " + info.fileName);
        System.out.println("path: " + info.path);
        System.out.println("artist: " + info.artist);
        System.out.println("duration: " + info.duration);
    }
}
