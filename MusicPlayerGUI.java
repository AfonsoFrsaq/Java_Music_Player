import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerGUI {
    private static final Logger logger = LogManager.getLogger(MusicPlayerGUI.class);
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel playlistPanel;
    private JPanel playlistDetailPanel;
    private JButton playButton;
    private JButton pauseButton;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private JList<String> songList;
    private DefaultListModel<String> listModel;
    private JList<String> playlistList;
    private DefaultListModel<String> playlistModel;
    private SongLibrary library;
    private ArrayList<Playlist> playlists;
    private static final String LIBRARY_FILE = "musiclibrary.ser";
    private static final String MUSIC_DIRECTORY = "Java_Project/Music";
    private static final int BUTTON_SIZE = 60; 

    public MusicPlayerGUI() {
        library = SongLibrary.loadLibrary(LIBRARY_FILE);
        library.loadSongsFromDirectory(MUSIC_DIRECTORY);

        frame = new JFrame("Music Player");
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        playButton = createButton("play.png");
        pauseButton = createButton("pause.png");

        currentTimeLabel = new JLabel("0:00");
        totalTimeLabel = new JLabel("0:00");
        currentTimeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalTimeLabel.setFont(new Font("Arial", Font.BOLD, 14));

        listModel = new DefaultListModel<>();
        for (Song song : library.getSongs()) {
            listModel.addElement(song.getTitle() + " by " + song.getArtist());
        }
        songList = new JList<>(listModel);
        songList.setBorder(new TitledBorder("Songs"));

        playlistModel = new DefaultListModel<>();
        playlists = library.getPlaylists();
        for (Playlist playlist : playlists) {
            playlistModel.addElement(playlist.getName());
        }
        playlistList = new JList<>(playlistModel);
        playlistList.setBorder(new TitledBorder("Playlists"));

        JPanel songPanel = new JPanel(new BorderLayout());
        songPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        songPanel.add(new JScrollPane(songList), BorderLayout.CENTER);

        JPanel playlistOuterPanel = new JPanel(new BorderLayout());
        playlistOuterPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        playlistPanel = new JPanel(new BorderLayout());
        playlistPanel.add(new JScrollPane(playlistList), BorderLayout.CENTER);
        JButton createPlaylistButton = new JButton("Create Playlist");
        playlistPanel.add(createPlaylistButton, BorderLayout.SOUTH);
        playlistOuterPanel.add(playlistPanel, BorderLayout.CENTER);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
        timePanel.add(currentTimeLabel);
        timePanel.add(Box.createHorizontalGlue());
        timePanel.add(totalTimeLabel);

        mainPanel.add(songPanel, BorderLayout.CENTER);
        mainPanel.add(playlistOuterPanel, BorderLayout.EAST);
        mainPanel.add(timePanel, BorderLayout.NORTH);
        mainPanel.add(createControlsPanel(), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setSize(900, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // Make the frame size immovable
        frame.setVisible(true);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = songList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedSongTitle = listModel.getElementAt(selectedIndex).split(" by ")[0];
                    library.playSong(selectedSongTitle, MusicPlayerGUI.this);
                    logger.info("Playing song: " + selectedSongTitle);
                } else {
                    logger.warn("No song selected to play.");
                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                library.togglePauseResume();
                logger.info("Toggled pause/resume");
            }
        });

        createPlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playlistName = JOptionPane.showInputDialog(frame, "Enter playlist name:");
                if (playlistName != null && !playlistName.trim().isEmpty()) {
                    Playlist playlist = new Playlist(playlistName);
                    library.addPlaylist(playlist);
                    playlistModel.addElement(playlistName);
                    SongLibrary.saveLibrary(library, LIBRARY_FILE);
                    logger.info("Created playlist: " + playlistName);
                } else {
                    logger.warn("Playlist name cannot be empty.");
                }
            }
        });

        playlistList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedPlaylistName = playlistList.getSelectedValue();
                    if (selectedPlaylistName != null) {
                        Playlist selectedPlaylist = playlists.stream()
                                .filter(playlist -> playlist.getName().equals(selectedPlaylistName))
                                .findFirst()
                                .orElse(null);
                        if (selectedPlaylist != null) {
                            showPlaylistPanel(selectedPlaylist);
                        }
                    }
                }
            }
        });

    }

    private JButton createButton(String iconName) {
        JButton button = new JButton();
        try {
            Image img = ImageIO.read(new File(iconName));
            Image scaledImg = img.getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImg));
        } catch (IOException ex) {
            logger.error("Icon not found: " + iconName, ex);
        }
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        return button;
    }

    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
        controlsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        controlsPanel.add(Box.createHorizontalGlue());
        controlsPanel.add(playButton);
        controlsPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        controlsPanel.add(pauseButton);
        controlsPanel.add(Box.createHorizontalGlue());

        return controlsPanel;
    }

    private void showPlaylistPanel(Playlist selectedPlaylist) {
        playlistDetailPanel = new JPanel(new BorderLayout(10, 10));
        playlistDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        DefaultListModel<String> playlistSongsModel = new DefaultListModel<>();
        for (Song song : selectedPlaylist.getSongs()) {
            playlistSongsModel.addElement(song.getTitle() + " by " + song.getArtist());
        }
        JList<String> playlistSongsList = new JList<>(playlistSongsModel);
        playlistSongsList.setBorder(new TitledBorder("Playlist Songs"));
        playlistDetailPanel.add(new JScrollPane(playlistSongsList), BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.remove(playlistDetailPanel);
                frame.add(mainPanel);
                addControlsPanel(mainPanel); // Ensure controls are added back
                frame.revalidate();
                frame.repaint();
                playlistList.clearSelection(); // Clear selection to allow re-selection
            }
        });
        playlistDetailPanel.add(backButton, BorderLayout.NORTH);

        JButton addSongButton = new JButton("Add Song");
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddSongFrame(selectedPlaylist, playlistSongsModel);
            }
        });
        playlistDetailPanel.add(addSongButton, BorderLayout.WEST);

        JButton removeSongButton = new JButton("Remove Song");
        removeSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = playlistSongsList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedSongTitle = playlistSongsModel.getElementAt(selectedIndex).split(" by ")[0];
                    Song songToRemove = selectedPlaylist.getSongs().stream()
                            .filter(song -> song.getTitle().equals(selectedSongTitle))
                            .findFirst()
                            .orElse(null);
                    if (songToRemove != null) {
                        library.removeSongFromPlaylist(selectedPlaylist, songToRemove);
                        playlistSongsModel.removeElementAt(selectedIndex);
                        SongLibrary.saveLibrary(library, LIBRARY_FILE);
                        logger.info("Removed song from playlist: " + songToRemove);
                    }
                } else {
                    logger.warn("No song selected to remove.");
                }
            }
        });
        playlistDetailPanel.add(removeSongButton, BorderLayout.EAST);

        playlistSongsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = playlistSongsList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedSongTitle = playlistSongsModel.getElementAt(selectedIndex).split(" by ")[0];
                        library.playSong(selectedSongTitle, MusicPlayerGUI.this);
                        logger.info("Playing song: " + selectedSongTitle);
                    }
                }
            }
        });

        playlistDetailPanel.add(createControlsPanel(), BorderLayout.SOUTH);

        frame.remove(mainPanel);
        frame.add(playlistDetailPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void addControlsPanel(JPanel panel) {
        panel.add(createControlsPanel(), BorderLayout.SOUTH);
    }

    private void showAddSongFrame(Playlist selectedPlaylist, DefaultListModel<String> playlistSongsModel) {
        JFrame addSongFrame = new JFrame("Add Songs to Playlist");
        JPanel addSongPanel = new JPanel(new BorderLayout());

        DefaultListModel<String> allSongsModel = new DefaultListModel<>();
        for (Song song : library.getSongs()) {
            allSongsModel.addElement(song.getTitle() + " by " + song.getArtist());
        }
        JList<String> allSongsList = new JList<>(allSongsModel);
        allSongsList.setBorder(new TitledBorder("All Songs"));
        addSongPanel.add(new JScrollPane(allSongsList), BorderLayout.CENTER);

        JButton addSelectedSongsButton = new JButton("Add Selected Songs");
        addSelectedSongsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (String selectedSong : allSongsList.getSelectedValuesList()) {
                    String songTitle = selectedSong.split(" by ")[0];
                    Song songToAdd = library.getSongs().stream()
                            .filter(song -> song.getTitle().equals(songTitle))
                            .findFirst()
                            .orElse(null);
                    if (songToAdd != null) {
                        selectedPlaylist.addSong(songToAdd);
                        playlistSongsModel.addElement(songToAdd.getTitle() + " by " + songToAdd.getArtist());
                        SongLibrary.saveLibrary(library, LIBRARY_FILE);
                        logger.info("Added song to playlist: " + songToAdd);
                    }
                }
                addSongFrame.dispose();
            }
        });
        addSongPanel.add(addSelectedSongsButton, BorderLayout.SOUTH);

        addSongFrame.add(addSongPanel);
        addSongFrame.setSize(400, 300);
        addSongFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addSongFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MusicPlayerGUI();
                logger.info("Music Player GUI started.");
            }
        });
    }
}