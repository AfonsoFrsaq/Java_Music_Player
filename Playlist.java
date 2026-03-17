import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private ArrayList<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    @Override
    public String toString() {
        return name + " (" + songs.size() + " songs)";
    }
}