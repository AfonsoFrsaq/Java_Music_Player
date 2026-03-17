import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MusicPlayer {
    private String filePath;
    private AdvancedPlayer player;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private boolean isPaused;
    private boolean isPlaying;
    private int pausedFrame;
    public MusicPlayer(String filePath, MusicPlayerGUI gui) {
        this.filePath = filePath;
        this.isPaused = false;
        this.isPlaying = false;
        this.pausedFrame = 0;
    }

    public void play() throws JavaLayerException, FileNotFoundException {
        if (isPaused) {
            resume();
        } else {
            stop();
            fileInputStream = new FileInputStream(filePath);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            player = new AdvancedPlayer(bufferedInputStream);
            isPlaying = true;
            new Thread(() -> {
                try {
                    player.setPlayBackListener(new PlaybackListener() {
                        @Override
                        public void playbackFinished(PlaybackEvent evt) {
                            pausedFrame = evt.getFrame();
                            if (!isPaused) {
                                pausedFrame = 0;
                                isPlaying = false;
                            }
                        }
                    });
                    player.play(pausedFrame, Integer.MAX_VALUE);
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void pause() {
        if (player != null) {
            isPaused = true;
            player.close();
            try {
                fileInputStream.close();
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void resume() throws JavaLayerException, FileNotFoundException {
        fileInputStream = new FileInputStream(filePath);
        bufferedInputStream = new BufferedInputStream(fileInputStream);
        player = new AdvancedPlayer(bufferedInputStream);
        isPaused = false;
        isPlaying = true;
        new Thread(() -> {
            try {
                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        pausedFrame = evt.getFrame();
                        if (!isPaused) {
                            pausedFrame = 0;
                            isPlaying = false;
                        }
                    }
                });
                player.play(pausedFrame, Integer.MAX_VALUE);
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        if (player != null) {
            isPaused = false;
            isPlaying = false;
            player.close();
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}