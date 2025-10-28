package game;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private static SoundManager instance;
    
    private Clip backgroundMusic;
    private float masterVolume = 1.0f;
    private float musicVolume = 0.3f;
    private float sfxVolume = 1.0f;
    
    private SoundManager() {
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    public void setMasterVolume(float volume) {
        this.masterVolume = volume;
        updateBackgroundMusicVolume();
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        updateBackgroundMusicVolume();
    }
    
    public void setSFXVolume(float volume) {
        this.sfxVolume = volume;
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public float getSFXVolume() {
        return sfxVolume;
    }
    
    private void updateBackgroundMusicVolume() {
        if (backgroundMusic != null && backgroundMusic.isOpen()) {
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            float finalVolume = masterVolume * musicVolume;
            float dB = (float) (Math.log(finalVolume) / Math.log(10.0) * 20.0);
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        }
    }
    
    public void playBackgroundMusic(String musicPath) {
        stopBackgroundMusic();
        
        try {
            File soundFile = new File(musicPath);
            if (!soundFile.exists()) {
                soundFile = new File(System.getProperty("user.dir") + File.separator + musicPath);
            }
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioStream);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                
                FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                float finalVolume = masterVolume * musicVolume;
                float dB = (float) (Math.log(finalVolume) / Math.log(10.0) * 20.0);
                dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                gainControl.setValue(dB);
                
                backgroundMusic.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }
    
    public void playSFX(String soundPath) {
        new Thread(() -> {
            try {
                File soundFile = new File(soundPath);
                if (!soundFile.exists()) {
                    soundFile = new File(System.getProperty("user.dir") + File.separator + soundPath);
                }
                if (soundFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float finalVolume = masterVolume * sfxVolume;
                        float dB = (float) (Math.log(finalVolume) / Math.log(10.0) * 20.0);
                        dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                        gainControl.setValue(dB);
                    }
                    
                    clip.start();
                    
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}

