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
    private BufferedImage indicatorIcon;
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
    private boolean showIndicator;
    
    private int playerId = 1;
    
    private int skill = 0;
    private int education = 0;
    private int health = 100;
    private int money = 500;
    
    private double remainingTime = 24.0;
    
    public Player(double x, double y) {
        this(x, y, 1);
    }
    
    public Player(double x, double y, int playerId) {
        this.x = x;
        this.y = y;
        this.playerId = playerId;
        this.lastFrameTime = System.currentTimeMillis();
        this.isBlinking = false;
        this.animationInterval = 300;
        this.scale = 0.4;
        this.currentPath = new ArrayList<>();
        this.currentWaypointIndex = 0;
        this.speed = 3.0;
        this.isMoving = false;
        this.currentDirection = "FRONT";
        this.isAnimating = false;
        this.currentAnimFrame = 0;
        this.lastAnimTime = System.currentTimeMillis();
        this.frontWalkFrames = new ArrayList<>();
        this.showIndicator = false;
        
        loadImages();
    }
    
    private void loadImages() {
        String playerFolder = "male_p" + playerId;
        
        try {
            String idlePath = "assets" + File.separator + "player" + File.separator + playerFolder + File.separator + "PAWN_ani_FRONTIDLE.png";
            File idleFile = new File(idlePath);
            if (!idleFile.exists()) {
                idleFile = new File(System.getProperty("user.dir") + File.separator + idlePath);
            }
            if (idleFile.exists()) {
                idleImage = ImageIO.read(idleFile);
            }
            
            String blinkPath = "assets" + File.separator + "player" + File.separator + playerFolder + File.separator + "PAWN_ani_FRONTEYEBLINK.png";
            File blinkFile = new File(blinkPath);
            if (!blinkFile.exists()) {
                blinkFile = new File(System.getProperty("user.dir") + File.separator + blinkPath);
            }
            if (blinkFile.exists()) {
                blinkImage = ImageIO.read(blinkFile);
            }
            
            String backIdlePath = "assets" + File.separator + "player" + File.separator + playerFolder + File.separator + "PAWN_ani_BACKIDLE.png";
            File backIdleFile = new File(backIdlePath);
            if (!backIdleFile.exists()) {
                backIdleFile = new File(System.getProperty("user.dir") + File.separator + backIdlePath);
            }
            if (backIdleFile.exists()) {
                backIdleImage = ImageIO.read(backIdleFile);
            }
            
            for (int i = 1; i <= 6; i++) {
                String walkPath = "assets" + File.separator + "player" + File.separator + playerFolder + File.separator + "PAWN_ani_FRONTWALK00" + i + ".png";
                File walkFile = new File(walkPath);
                if (!walkFile.exists()) {
                    walkFile = new File(System.getProperty("user.dir") + File.separator + walkPath);
                }
                if (walkFile.exists()) {
                    frontWalkFrames.add(ImageIO.read(walkFile));
                }
            }
            
            String iconPath = "assets" + File.separator + "player" + File.separator + "peonparticle-arrowdown.png";
            File iconFile = new File(iconPath);
            if (!iconFile.exists()) {
                iconFile = new File(System.getProperty("user.dir") + File.separator + iconPath);
            }
            if (iconFile.exists()) {
                indicatorIcon = ImageIO.read(iconFile);
            }
        } catch (Exception ex) {
        }
    }
    
    private boolean isRemotePlayer = false;
    
    public void setRemotePlayer(boolean remote) {
        this.isRemotePlayer = remote;
    }
    
    public boolean isRemotePlayer() {
        return isRemotePlayer;
    }
    
    public void update() {
        if (isRemotePlayer) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        if (isAnimating) {
            if (currentTime - lastAnimTime >= 100) {
                currentAnimFrame++;
                lastAnimTime = currentTime;
                
                if (currentAnimFrame >= frontWalkFrames.size()) {
                    isAnimating = false;
                    currentAnimFrame = 0;
                    isMoving = false;
                    
                    if (currentPath != null && currentWaypointIndex < currentPath.size()) {
                        Waypoint target = currentPath.get(currentWaypointIndex);
                        x = target.getX();
                        y = target.getY();
                        currentPath = null;
                        currentWaypointIndex = 0;
                    }
                }
            }
        } else {
            if (currentTime - lastFrameTime >= animationInterval) {
                isBlinking = !isBlinking;
                lastFrameTime = currentTime;
            }
        }
    }
    
    public double consumeTime(double distance) {
        double timeCost = 5.0 + (distance / 100.0);
        if (timeCost > 7.0) {
            timeCost = 7.0;
        }
        
        double oldTime = remainingTime;
        remainingTime -= timeCost;
        if (remainingTime < 0.0) {
            remainingTime = 0.0;
        }
        
        double actualTimeCost = oldTime - remainingTime;
        System.out.println("Player " + playerId + " consumed " + actualTimeCost + " hours (distance: " + distance + "), remaining: " + remainingTime);
        
        return actualTimeCost;
    }
    
    public void setDestination(double targetX, double targetY, ArrayList<ArrayList<Waypoint>> allPaths) {
        setDestination(targetX, targetY, allPaths, true);
    }
    
    public void setDestination(double targetX, double targetY, ArrayList<ArrayList<Waypoint>> allPaths, boolean consumeTime) {
        if (isMoving || isAnimating) {
            System.out.println("Player " + playerId + " is already moving, ignoring setDestination");
            return;
        }
        
        double distance = Math.sqrt((targetX - x) * (targetX - x) + (targetY - y) * (targetY - y));
        if (distance < 10.0) {
            System.out.println("Player " + playerId + " destination too close (distance: " + distance + "), ignoring");
            return;
        }
        
        System.out.println("Setting destination to (" + targetX + ", " + targetY + "), consumeTime=" + consumeTime);
        
        if (consumeTime && !isRemotePlayer) {
            consumeTime(distance);
        }
        
        currentPath = new ArrayList<>();
        currentPath.add(new Waypoint(targetX, targetY));
        currentWaypointIndex = 0;
        isMoving = true;
        isAnimating = true;
        currentAnimFrame = 0;
        lastAnimTime = System.currentTimeMillis();
    }
    
    public boolean isAnimatingNow() {
        return isAnimating && currentAnimFrame < frontWalkFrames.size();
    }
    
    
    public boolean isMoving() {
        return isMoving || isAnimating;
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
    
    public boolean hasDestination() {
        return currentPath != null && !currentPath.isEmpty() && currentWaypointIndex < currentPath.size();
    }
    
    public void setMoving(boolean moving) {
        this.isMoving = moving;
        this.isAnimating = moving;
        if (!moving) {
            this.currentAnimFrame = 0;
        }
    }
    
    public void forceUpdateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (isAnimating) {
            if (currentTime - lastAnimTime >= 100) {
                currentAnimFrame++;
                lastAnimTime = currentTime;
                
                if (currentAnimFrame >= frontWalkFrames.size()) {
                    isAnimating = false;
                    currentAnimFrame = 0;
                    
                    if (currentPath != null && currentWaypointIndex < currentPath.size()) {
                        Waypoint target = currentPath.get(currentWaypointIndex);
                        x = target.getX();
                        y = target.getY();
                    }
                    
                    isMoving = false;
                }
            }
        } else {
            if (currentTime - lastFrameTime >= animationInterval) {
                isBlinking = !isBlinking;
                lastFrameTime = currentTime;
            }
        }
    }
    
    public void setDirection(String direction) {
        this.currentDirection = direction;
    }
    
    public String getDirection() {
        return currentDirection;
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
        
        if (showIndicator && indicatorIcon != null && !isRemotePlayer) {
            double iconScale = 0.08;
            int iconWidth = (int)(indicatorIcon.getWidth() * iconScale);
            int iconHeight = (int)(indicatorIcon.getHeight() * iconScale);
            int iconX = (int)x - iconWidth / 2;
            int iconY = (int)y - (currentImage != null ? (int)(currentImage.getHeight() * scale) : 100) - iconHeight - 5;
            
            g2d.drawImage(indicatorIcon, iconX, iconY, iconWidth, iconHeight, null);
        }
    }
    
    public void setShowIndicator(boolean show) {
        this.showIndicator = show;
    }
    
    public boolean isShowIndicator() {
        return showIndicator;
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
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(int playerId) {
        if (this.playerId != playerId) {
            this.playerId = playerId;
            loadImages();
        }
    }
    
    public int getSkill() {
        return skill;
    }
    
    public void setSkill(int skill) {
        this.skill = skill;
    }
    
    public void increaseSkill(int amount) {
        skill += amount;
    }

    public int getEducation() {
        return education;
    }
    
    public void setEducation(int education) {
        this.education = education;
    }
    public void increaseEducation(int amount) {
        education += amount;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void increaseHealth(int amount) {
        health += amount;
    }

    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getMoney() {
        return money;
    }
    
    public void increaseMoney(int amount) {
        money += amount;
    }
    public void setMoney(int money) {
        this.money = money;
    }
    
    public double getRemainingTime() {
        return remainingTime;
    }
    
    public void setRemainingTime(double time) {
        this.remainingTime = time;
        if (this.remainingTime < 0.0) {
            this.remainingTime = 0.0;
        }
        if (this.remainingTime > 24.0) {
            this.remainingTime = 24.0;
        }
    }
    
    public boolean hasTimeRemaining() {
        return remainingTime > 0.0;
    }
}

