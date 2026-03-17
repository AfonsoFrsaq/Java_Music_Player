import java.io.Serializable;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private int duration; // duration in seconds
    private String filePath;

    public Song(String title, String artist, String album, String genre, int duration, String filePath) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getGenre() {
        return genre;
    }

    public int getDuration() {
        return duration;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return title + " by " + artist + " [" + genre + "]";
    }
}