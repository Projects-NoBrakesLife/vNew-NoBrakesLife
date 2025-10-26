
package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundManager {
    private BufferedImage mapImage;
    
    public BackgroundManager() {
        loadMapImage();
    }
    
    private void loadMapImage() {
        try {
            String fullPath = System.getProperty("user.dir") + java.io.File.separator + GameConfig.BACKGROUND_IMAGE;
            Image tempImage = Toolkit.getDefaultToolkit().createImage(fullPath);
            
            MediaTracker tracker = new MediaTracker(new java.awt.Container());
            tracker.addImage(tempImage, 0);
            tracker.waitForID(0);
            
            mapImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = mapImage.createGraphics();
            g.drawImage(tempImage, 0, 0, null);
            g.dispose();
        } catch (Exception ex) {
        }
    }
    
    public void render(Graphics2D g2d, int screenWidth, int screenHeight) {
        if (mapImage == null) {
            renderDefaultBackground(g2d, screenWidth, screenHeight);
            return;
        }
        
        int imageWidth = mapImage.getWidth();
        int imageHeight = mapImage.getHeight();
        
        double scaleX = (double) screenWidth / imageWidth;
        double scaleY = (double) screenHeight / imageHeight;
        double scale = Math.max(scaleX, scaleY);
        
        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);
        
        int x = (screenWidth - scaledWidth) / 2;
        int y = (screenHeight - scaledHeight) / 2;
        
        g2d.drawImage(mapImage, x, y, scaledWidth, scaledHeight, null);
    }
    
    private void renderDefaultBackground(Graphics2D g2d, int screenWidth, int screenHeight) {
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, screenHeight * 4 / 5, screenWidth, screenHeight / 5);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(FontManager.getThaiFont(Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        String errorMessage = "Map image not found";
        int x = (screenWidth - fm.stringWidth(errorMessage)) / 2;
        int y = screenHeight / 2;
        g2d.drawString(errorMessage, x, y);
    }
    
    public BufferedImage getMapImage() {
        return mapImage;
    }
}

