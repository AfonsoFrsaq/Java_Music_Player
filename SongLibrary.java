import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SongLibrary implements Serializable {
    private static final Logger logger = LogManager.getLogger(SongLibrary.class);
    private static final long serialVersionUID = 1L;
    private ArrayList<Song> songs;
    private ArrayList<Playlist> playlists;
    private transient MusicPlayer musicPlayer;

    public SongLibrary() {
        this.songs = new ArrayList<>();
        this.playlists = new ArrayList<>();
    }

    public void addSong(Song song) {
        songs.add(song);
        logger.info("Added song: " + song);
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        logger.info("Added playlist: " + playlist);
    }

    public void removeSongFromPlaylist(Playlist playlist, Song song) {
        playlist.getSongs().remove(song);
        logger.info("Removed song: " + song + " from playlist: " + playlist.getName());
    }

    public void playSong(String title, MusicPlayerGUI gui) {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.stop();
        }
        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(title)) {
                logger.info("Playing song: " + song);
                try {
                    musicPlayer = new MusicPlayer(song.getFilePath(), gui);
                    musicPlayer.play();
                } catch (Exception e) {
                    logger.error("Error playing song: " + e.getMessage(), e);
                }
                return;
            }
        }
        logger.warn("Song not found: " + title);
    }

    public void togglePauseResume() {
        if (musicPlayer != null) {
            if (musicPlayer.isPaused()) {
                try {
                    musicPlayer.resume();
                    logger.info("Song resumed");
                } catch (Exception e) {
                    logger.error("Error resuming song: " + e.getMessage(), e);
                }
            } else {
                musicPlayer.pause();
                logger.info("Song paused");
            }
        }
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public static void saveLibrary(SongLibrary library, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(library);
            logger.info("Library saved successfully.");
        } catch (IOException e) {
            logger.error("Failed to save library: " + e.getMessage(), e);
        }
    }

    public static SongLibrary loadLibrary(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (SongLibrary) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load library: " + e.getMessage(), e);
            return new SongLibrary();
        }
    }

    public void loadSongsFromDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            List<File> files = getAllFiles(directory);
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".mp3")) {
                    String[] parts = fileName.split(" - ");
                    if (parts.length >= 2) {
                        String artist = parts[0];
                        String title = parts[1].replace(".mp3", "");
                        Song song = new Song(title, artist, "Unknown Album", directory.getName(), 0, file.getAbsolutePath());
                        addSong(song);
                    }
                }
            }
            logger.info("Songs loaded from directory: " + directoryPath);
        } else {
            logger.warn("Directory not found: " + directoryPath);
        }
    }

    private List<File> getAllFiles(File directory) {
        List<File> files = new ArrayList<>();
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                if (file.isDirectory()) {
                    files.addAll(getAllFiles(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }
}