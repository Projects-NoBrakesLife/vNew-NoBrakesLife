package game;

import editor.Waypoint;

import java.util.ArrayList;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GameScene {
    private ArrayList<GameObject> hoverObjects;
    private ArrayList<String> objectNames;
    private Point mousePosition;
    private int currentHoverIndex;
    private Player player;
    private ArrayList<ArrayList<Waypoint>> allPaths;
    
    public GameScene() {
        hoverObjects = new ArrayList<>();
        objectNames = new ArrayList<>();
        mousePosition = new Point(0, 0);
        currentHoverIndex = -1;
        player = new Player(950, 370);
        allPaths = GameConfig.getWaypointPaths();
    }
    
    public void addHoverObject(GameObject obj, String name) {
        hoverObjects.add(obj);
        objectNames.add(name);
    }
    
    public void updateMousePosition(int x, int y) {
        mousePosition.setLocation(x, y);
    }
    
    public void render(Graphics2D g2d) {
        boolean anyHovering = false;
        String hoverName = null;
        int hoverIndex = -1;
        
        for (int i = 0; i < hoverObjects.size(); i++) {
            GameObject obj = hoverObjects.get(i);
            if (isHovering(obj)) {
                obj.render(g2d);
                anyHovering = true;
                hoverName = objectNames.get(i);
                hoverIndex = i;
                break;
            }
        }
        
        player.update();
        player.render(g2d);
        
        if (anyHovering && hoverIndex != currentHoverIndex) {
            playSound(GameConfig.HOVER_SOUND);
            currentHoverIndex = hoverIndex;
        } else if (!anyHovering) {
            currentHoverIndex = -1;
        }
        
        if (anyHovering && hoverName != null) {
            renderHoverUI(g2d, hoverName);
        }
    }
    
    private void playSound(String soundPath) {
        try {
            File soundFile = new File(soundPath);
            if (!soundFile.exists()) {
                soundFile = new File(System.getProperty("user.dir") + java.io.File.separator + soundPath);
            }
            if (soundFile.exists()) {
                javax.sound.sampled.AudioInputStream audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            }
        } catch (Exception ex) {
        }
    }
    
    private void renderHoverUI(Graphics2D g2d, String name) {
        BufferedImage uiField = loadUI();
        if (uiField == null) return;
        
        int uiWidth = 250;
        int uiHeight = 50;
        int uiX = mousePosition.x + 30;
        int uiY = mousePosition.y + 30;
        
        if (uiX + uiWidth > GameConfig.WINDOW_WIDTH) {
            uiX = mousePosition.x - uiWidth - 30;
        }
        if (uiY + uiHeight > GameConfig.WINDOW_HEIGHT) {
            uiY = mousePosition.y - uiHeight - 30;
        }
        
        g2d.drawImage(uiField, uiX, uiY, uiWidth, uiHeight, null);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(FontManager.getThaiFont(Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = uiX + (uiWidth - fm.stringWidth(name)) / 2;
        int textY = uiY + uiHeight / 2 + fm.getAscent() / 4;
        g2d.drawString(name, textX, textY);
    }
    
    private BufferedImage loadUI() {
        try {
            String path = "assets" + java.io.File.separator + "ui" + java.io.File.separator + "Input-Field-Small-White_0.png";
            File file = new File(path);
            if (!file.exists()) {
                file = new File(System.getProperty("user.dir") + java.io.File.separator + path);
            }
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (Exception ex) {
        }
        return null;
    }
    
    private boolean isHovering(GameObject obj) {
        double cx = obj.getX() + obj.getWidth() / 2;
        double cy = obj.getY() + obj.getHeight() / 2;
        
        double dx = mousePosition.x - cx;
        double dy = mousePosition.y - cy;
        
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double radius = Math.min(obj.getWidth(), obj.getHeight()) / 2;
        
        return distance <= radius;
    }
    
    public ArrayList<GameObject> getHoverObjects() {
        return hoverObjects;
    }
    
    public boolean isHoveringAny() {
        for (GameObject obj : hoverObjects) {
            if (isHovering(obj)) {
                return true;
            }
        }
        return false;
    }
    
    public Point getMousePosition() {
        return mousePosition;
    }
    
    public void handleClick(int x, int y) {
        for (int i = 0; i < hoverObjects.size(); i++) {
            GameObject obj = hoverObjects.get(i);
            if (isClicking(obj, x, y)) {
                GameConfig.HoverObject config = GameConfig.HOVER_OBJECTS[i];
                double targetX = config.playerX;
                double targetY = config.playerY;
                player.setDirection(config.direction);
                player.setDestination(targetX, targetY, allPaths);
                break;
            }
        }
    }
    
    private boolean isClicking(GameObject obj, int x, int y) {
        double cx = obj.getX() + obj.getWidth() / 2;
        double cy = obj.getY() + obj.getHeight() / 2;
        
        double dx = x - cx;
        double dy = y - cy;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double radius = Math.min(obj.getWidth(), obj.getHeight()) / 2;
        
        return distance <= radius;
    }
    
    public Player getPlayer() {
        return player;
    }
}

