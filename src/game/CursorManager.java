package game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CursorManager {
    private static Cursor normalCursor;
    private static Cursor hoverCursor;
    private static Cursor pressCursor;
    
    private static final String POINTER_NORMAL = "assets" + File.separator + "pointer" + File.separator + "5_pointer_USE-THIS-ONE_0.png";
    private static final String POINTER_HOVER = "assets" + File.separator + "pointer" + File.separator + "2_pointer-denied2_USE-THIS-ONE_0.png";
    private static final String POINTER_PRESS = "assets" + File.separator + "pointer" + File.separator + "6_pointer-press_USE-THIS-ONE_0.png";
    
    public static Cursor getNormalCursor() {
        if (normalCursor == null) {
            normalCursor = createCursor(POINTER_NORMAL, 0, 0);
        }
        return normalCursor;
    }
    
    public static Cursor getHoverCursor() {
        if (hoverCursor == null) {
            hoverCursor = createCursor(POINTER_HOVER, 0, 0);
        }
        return hoverCursor;
    }
    
    public static Cursor getPressCursor() {
        if (pressCursor == null) {
            pressCursor = createCursor(POINTER_PRESS, 0, 0);
        }
        return pressCursor;
    }
    
    private static Cursor createCursor(String imagePath, int hotSpotX, int hotSpotY) {
        try {
            File cursorFile = new File(imagePath);
            if (!cursorFile.exists()) {
                cursorFile = new File(System.getProperty("user.dir") + File.separator + imagePath);
            }
            if (cursorFile.exists()) {
                BufferedImage originalImage = ImageIO.read(cursorFile);
                
                int scale = 2;
                int newWidth = originalImage.getWidth() * scale;
                int newHeight = originalImage.getHeight() * scale;
                
                BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                return toolkit.createCustomCursor(scaledImage, new Point(hotSpotX * scale, hotSpotY * scale), "custom");
            }
        } catch (Exception ex) {
        }
        return Cursor.getDefaultCursor();
    }
}

