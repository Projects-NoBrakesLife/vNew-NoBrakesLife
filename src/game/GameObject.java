package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.awt.MediaTracker;

public class GameObject {
    private BufferedImage image;
    private double x;
    private double y;
    private double width;
    private double height;
    private double rotation;
    
    public GameObject(String imagePath, double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = 0.0;
        
        loadImage(imagePath);
        
        if (image != null && this.width == 0 && this.height == 0) {
            this.width = image.getWidth();
            this.height = image.getHeight();
        }
    }
    
    public GameObject(BufferedImage image, double x, double y, double width, double height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = 0.0;
    }
    
    private void loadImage(String imagePath) {
        try {
            BufferedImage img = null;
            
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                img = ImageIO.read(imgFile);
            } else {
                String fullPath = System.getProperty("user.dir") + java.io.File.separator + imagePath;
                File fullPathFile = new File(fullPath);
                if (fullPathFile.exists()) {
                    img = ImageIO.read(fullPathFile);
                }
            }
            
            if (img != null) {
                image = img;
            }
        } catch (Exception ex) {
        }
    }
    
    public void render(Graphics2D g2d) {
        if (image == null) {
            return;
        }
        
        AffineTransform originalTransform = g2d.getTransform();
        
        AffineTransform transform = new AffineTransform();
        transform.translate(x + width / 2, y + height / 2);
        transform.rotate(Math.toRadians(rotation));
        transform.translate(-width / 2, -height / 2);
        
        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g2d.setTransform(originalTransform);
    }
    
    public boolean contains(double mx, double my) {
        double dx = mx - (x + width / 2);
        double dy = my - (y + height / 2);
        double angle = -Math.toRadians(rotation);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double nx = dx * cos - dy * sin;
        double ny = dx * sin + dy * cos;
        return nx >= -width / 2 && nx <= width / 2 && ny >= -height / 2 && ny <= height / 2;
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
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getRotation() {
        return rotation;
    }
    
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }
    
    public void scale(double factor) {
        this.width *= factor;
        this.height *= factor;
    }
    
    public void rotate(double angle) {
        this.rotation += angle;
    }
}

