package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import editor.Waypoint;

public class Player {
    private double x;
    private double y;
    private BufferedImage idleImage;
    private BufferedImage blinkImage;
    private BufferedImage backIdleImage;
    private ArrayList<BufferedImage> frontWalkFrames;
    private String currentDirection;
    private long lastFrameTime;
    private boolean isBlinking;
    private long animationInterval;
    private double scale;
    private ArrayList<Waypoint> currentPath;
    private int currentWaypointIndex;
    private double speed;
    private boolean isMoving;
    private boolean isAnimating;
    private int currentAnimFrame;
    private long lastAnimTime;
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastFrameTime = System.currentTimeMillis();
        this.isBlinking = false;
        this.animationInterval = 300;
        this.scale = 0.4;
        this.currentPath = new ArrayList<>();
        this.currentWaypointIndex = 0;
        this.speed = 2.0;
        this.isMoving = false;
        this.currentDirection = "FRONT";
        this.isAnimating = false;
        this.currentAnimFrame = 0;
        this.lastAnimTime = System.currentTimeMillis();
        this.frontWalkFrames = new ArrayList<>();
        
        loadImages();
    }
    
    private void loadImages() {
        try {
            String idlePath = "assets" + File.separator + "player" + File.separator + "male_p1" + File.separator + "PAWN_ani_FRONTIDLE.png";
            File idleFile = new File(idlePath);
            if (!idleFile.exists()) {
                idleFile = new File(System.getProperty("user.dir") + File.separator + idlePath);
            }
            if (idleFile.exists()) {
                idleImage = ImageIO.read(idleFile);
            }
            
            String blinkPath = "assets" + File.separator + "player" + File.separator + "male_p1" + File.separator + "PAWN_ani_FRONTEYEBLINK.png";
            File blinkFile = new File(blinkPath);
            if (!blinkFile.exists()) {
                blinkFile = new File(System.getProperty("user.dir") + File.separator + blinkPath);
            }
            if (blinkFile.exists()) {
                blinkImage = ImageIO.read(blinkFile);
            }
            
            String backIdlePath = "assets" + File.separator + "player" + File.separator + "male_p1" + File.separator + "PAWN_ani_BACKIDLE.png";
            File backIdleFile = new File(backIdlePath);
            if (!backIdleFile.exists()) {
                backIdleFile = new File(System.getProperty("user.dir") + File.separator + backIdlePath);
            }
            if (backIdleFile.exists()) {
                backIdleImage = ImageIO.read(backIdleFile);
            }
            
            for (int i = 1; i <= 6; i++) {
                String walkPath = "assets" + File.separator + "player" + File.separator + "male_p1" + File.separator + "PAWN_ani_FRONTWALK00" + i + ".png";
                File walkFile = new File(walkPath);
                if (!walkFile.exists()) {
                    walkFile = new File(System.getProperty("user.dir") + File.separator + walkPath);
                }
                if (walkFile.exists()) {
                    frontWalkFrames.add(ImageIO.read(walkFile));
                }
            }
        } catch (Exception ex) {
        }
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        if (isAnimating) {
            if (currentTime - lastAnimTime >= 100) {
                currentAnimFrame++;
                lastAnimTime = currentTime;
                
                if (currentAnimFrame >= frontWalkFrames.size()) {
                    isAnimating = false;
                    currentAnimFrame = 0;
                }
            }
            
            if (!isAnimating && isMoving && currentPath != null && currentWaypointIndex < currentPath.size()) {
                Waypoint target = currentPath.get(currentWaypointIndex);
                double targetX = target.getX();
                double targetY = target.getY();
                
                x = targetX;
                y = targetY;
                currentWaypointIndex++;
                
                if (currentWaypointIndex >= currentPath.size()) {
                    isMoving = false;
                    currentWaypointIndex = 0;
                }
            }
        } else {
            if (currentTime - lastFrameTime >= animationInterval) {
                isBlinking = !isBlinking;
                lastFrameTime = currentTime;
            }
        }
    }
    
    public void setDestination(double targetX, double targetY, ArrayList<ArrayList<Waypoint>> allPaths) {
        System.out.println("Setting destination to (" + targetX + ", " + targetY + ")");
        
        currentPath = new ArrayList<>();
        currentPath.add(new Waypoint(targetX, targetY));
        currentWaypointIndex = 0;
        isMoving = true;
        isAnimating = true;
        currentAnimFrame = 0;
        lastAnimTime = System.currentTimeMillis();
    }
    
    
    public boolean isMoving() {
        return isMoving || isAnimating;
    }
    
    public void setDirection(String direction) {
        this.currentDirection = direction;
    }
    
    public void render(Graphics2D g2d) {
        BufferedImage currentImage;
        
        if (isAnimating && currentAnimFrame < frontWalkFrames.size()) {
            currentImage = frontWalkFrames.get(currentAnimFrame);
        } else if ("BACK".equals(currentDirection)) {
            currentImage = backIdleImage;
        } else {
            currentImage = isBlinking ? blinkImage : idleImage;
        }
        
        if (currentImage != null) {
            int scaledWidth = (int)(currentImage.getWidth() * scale);
            int scaledHeight = (int)(currentImage.getHeight() * scale);
            
            int drawX = (int)x - scaledWidth / 2;
            int drawY = (int)y - scaledHeight;
            
            Object oldHint = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(currentImage, drawX, drawY, scaledWidth, scaledHeight, null);
            
            if (oldHint != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
            }
        }
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void setY(double y) {
        this.y = y;
    }
}

