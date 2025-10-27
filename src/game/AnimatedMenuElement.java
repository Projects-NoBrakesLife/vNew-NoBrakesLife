package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class AnimatedMenuElement {
    private ArrayList<BufferedImage> frames;
    private double x;
    private double y;
    private double width;
    private double height;
    private int currentFrame;
    private long lastFrameTime;
    private long frameDelay;
    
    public AnimatedMenuElement(String basePath, double x, double y, double width, double height, long frameDelay) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.frameDelay = frameDelay;
        this.frames = new ArrayList<>();
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        loadAnimationFrames(basePath);
    }
    
    private void loadAnimationFrames(String basePath) {
        try {
            String[] frameFiles = {
                "Settings-Bar-Knob.png",
                "SliderHead-01.png",
                "SliderHead-02.png",
                "SliderHead-03.png"
            };
            
            for (String frameFile : frameFiles) {
                String fullPath = System.getProperty("user.dir") + File.separator + basePath + File.separator + frameFile;
                File file = new File(fullPath);
                if (file.exists()) {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        frames.add(image);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= frameDelay && frames.size() > 0) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastFrameTime = currentTime;
        }
    }
    
    public void render(Graphics2D g2d) {
        if (frames.size() > 0) {
            BufferedImage currentImage = frames.get(currentFrame);
            g2d.drawImage(currentImage, (int)x, (int)y, (int)width, (int)height, null);
        }
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
}

