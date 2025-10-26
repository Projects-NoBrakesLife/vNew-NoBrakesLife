package game;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private Clip currentClip;
    private boolean wasHovering;
    
    public SoundManager() {
        wasHovering = false;
    }
    
    public void updateHoverState(boolean isHovering, String soundPath) {
        if (isHovering && !wasHovering) {
            stopAll();
            playSound(soundPath);
        }
        wasHovering = isHovering;
    }
    
    private void playSound(String soundPath) {
        try {
            File soundFile = new File(soundPath);
            if (!soundFile.exists()) {
                soundFile = new File(System.getProperty("user.dir") + java.io.File.separator + soundPath);
            }
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                currentClip = AudioSystem.getClip();
                currentClip.open(audioStream);
                currentClip.start();
            }
        } catch (Exception ex) {
        }
    }
    
    public void stopAll() {
        if (currentClip != null) {
            currentClip.stop();
        }
    }
}

