package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SliderBar {
    private double x, y, width, height;
    private BufferedImage backImage;
    private BufferedImage frontImage;
    private BufferedImage knobImage;
    private double value; 
    private double knobX;
    private boolean isDragging;
    private double minX, maxX;
    
    public SliderBar(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.value = 1.0; 
        
        loadImages();
        updateKnobPosition();
    }
    
    private void loadImages() {
        try {
            String backPath = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "ui" + File.separator + "setting" + File.separator + "Settings-Bar-Back.png";
            File backFile = new File(backPath);
            if (backFile.exists()) {
                backImage = ImageIO.read(backFile);
            }
            
            String frontPath = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "ui" + File.separator + "setting" + File.separator + "Settings-Bar-Front.png";
            File frontFile = new File(frontPath);
            if (frontFile.exists()) {
                frontImage = ImageIO.read(frontFile);
            }
            
            String knobPath = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "ui" + File.separator + "icon" + File.separator + "Settings-Bar-Knob.png";
            File knobFile = new File(knobPath);
            if (knobFile.exists()) {
                knobImage = ImageIO.read(knobFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateKnobPosition() {
        minX = x;
        maxX = x + width - (height * 3.5);
        if (maxX <= minX) {
            knobX = minX;
        } else {
            knobX = minX + (maxX - minX) * value;
        }
    }
    
    public void render(Graphics2D g2d) {

        if (backImage != null) {
            g2d.drawImage(backImage, (int)x, (int)y, (int)width, (int)height, null);
        }
        
       
        if (frontImage != null) {
            double filledWidth = width * value;
            if (filledWidth > 0) {
             
                g2d.drawImage(frontImage, 
                    (int)x, (int)y, (int)(x + filledWidth), (int)(y + height), 
                    0, 0, (int)(frontImage.getWidth() * value), frontImage.getHeight(), 
                    null);
            }
        }
      
        if (knobImage != null) {
            double knobSize = height * 3.5;
            double knobY = y + (height - knobSize) / 2;
            g2d.drawImage(knobImage, (int)knobX, (int)knobY, (int)knobSize, (int)knobSize, null);
        }
    }
    
    public boolean contains(double mx, double my) {
        double knobSize = height *3.5;
        double knobY = y + (height - knobSize) / 2;
        return mx >= knobX && mx <= knobX + knobSize && my >= knobY && my <= knobY + knobSize;
    }
    
    public boolean startDrag(double mx, double my) {
        if (contains(mx, my)) {
            isDragging = true;
            return true;
        }
        return false;
    }
    
    public void drag(double mx, double my) {
        if (isDragging) {
            knobX = Math.max(minX, Math.min(maxX, mx - (height * 0.75)));
            if (maxX > minX) {
                value = (knobX - minX) / (maxX - minX);
                value = Math.max(0.0, Math.min(1.0, value));
            }
        }
    }
    
    public void endDrag() {
        isDragging = false;
    }
    
    public double getValue() {
        return value;
    }
    
    public float getValueFloat() {
        return (float)value;
    }
    
    public int getPercentage() {
        return (int)(value * 100);
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public void setValue(double value) {
        this.value = Math.max(0.0, Math.min(1.0, value));
        updateKnobPosition();
    }
}

