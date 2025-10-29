package game;

import editor.Waypoint;
import network.NetworkManager;

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
    private ArrayList<Player> players;
    private ArrayList<ArrayList<Waypoint>> allPaths;
    private boolean isOnlineMode;
    private NetworkManager networkManager;
    private ArrayList<MenuElement> hudElements;
    
    private int currentTurnPlayerId = 1;
    private int currentTurnNumber = 1;
    private String turnDisplayText = "";
    private float turnDisplayAlpha = 0.0f;
    private long turnDisplayStartTime = 0;
    private static final long TURN_DISPLAY_DURATION = 4000;
    private static final long TURN_DISPLAY_FADE_IN = 500;
    private static final long TURN_DISPLAY_FADE_OUT = 500;
    private boolean waitingForTurnToComplete = false;
    
    public GameScene() {
        this(false);
    }
    
    public GameScene(boolean isOnlineMode) {
        this.isOnlineMode = isOnlineMode;
        hoverObjects = new ArrayList<>();
        objectNames = new ArrayList<>();
        mousePosition = new Point(0, 0);
        currentHoverIndex = -1;
        players = new ArrayList<>();
        allPaths = GameConfig.getWaypointPaths();
        networkManager = NetworkManager.getInstance();
        hudElements = new ArrayList<>();
        loadGameHUD();
        
        if (isOnlineMode) {
            int localPlayerId = networkManager.getPlayerId();
            int connectedCount = network.NetworkManager.getInstance().getPlayers().size();
            
            double[][] positions = {
                {970.0, 425.0},
                {1010.0, 425.0},
                {970.0, 450.0},
                {1010.0, 450.0}
            };
            
            for (int i = 0; i < connectedCount && i < 4; i++) {
                int playerNum = i + 1;
                Player newPlayer = new Player(positions[i][0], positions[i][1], playerNum);
                if (playerNum == localPlayerId) {
                    newPlayer.setRemotePlayer(false);
                    newPlayer.setShowIndicator(true);
                } else {
                    newPlayer.setRemotePlayer(true);
                    newPlayer.setShowIndicator(false);
                }
                players.add(newPlayer);
            }
        } else {
            players.add(new Player(980.0, 430.0));
        }
        
        showGameStartDisplay();
    }
    
    public void addHoverObject(GameObject obj, String name) {
        hoverObjects.add(obj);
        objectNames.add(name);
    }
    
    private void loadGameHUD() {
        MenuElement hudImg = new MenuElement(MenuElement.ElementType.IMAGE, "assets" + File.separator + "ui" + File.separator + "hud" + File.separator + "hud.png", -48.0, 736.0, 512.4, 325.7); // Y + 20
        hudElements.add(hudImg);
        
        Player localPlayer = getLocalPlayer();
        
        MenuElement text1 = new MenuElement("ค่าทักษะ: " + (localPlayer != null ? localPlayer.getSkill() : 0), 230.0, 853.0, 15); // Y + 20
        text1.setTextColor(new Color(30, 30, 40));
        hudElements.add(text1);
        
        MenuElement text2 = new MenuElement("ค่าสุขภาพ: " + (localPlayer != null ? localPlayer.getHealth() : 100), 230.0, 907.0, 15); // Y + 20
        text2.setTextColor(new Color(30, 30, 40));
        hudElements.add(text2);
        
        MenuElement text3 = new MenuElement("ค่าการเรียน: " + (localPlayer != null ? localPlayer.getEducation() : 0), 230.0, 961.0, 15); // Y + 20
        text3.setTextColor(new Color(30, 30, 40));
        hudElements.add(text3);
        
        MenuElement text4 = new MenuElement("ค่าเงิน: " + (localPlayer != null ? localPlayer.getMoney() : 500), 203.0, 1020.0, 15); // Y + 20
        text4.setTextColor(new Color(30, 30, 40));
        hudElements.add(text4);
        
        MenuElement imgtimer = new MenuElement(MenuElement.ElementType.IMAGE, "assets" + File.separator + "ui" + File.separator + "hud" + File.separator + "HUD info header.png", 730.3, -9.0, 459.4, 62.6);
        hudElements.add(imgtimer);
        MenuElement texttimer = new MenuElement("P1 เหลือเวลา 10/24 ชม", 794.5, 31.0, 32);
        texttimer.setTextColor(new Color(30, 30, 40));
        hudElements.add(texttimer);
    }
    
    private Player getLocalPlayer() {
        if (players.isEmpty()) return null;
        
        if (isOnlineMode) {
            int localPlayerId = networkManager.getPlayerId();
            if (localPlayerId > 0 && localPlayerId <= players.size()) {
                return players.get(localPlayerId - 1);
            }
        }
        
        return players.get(0);
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
        
        Player localPlayerForTurn = null;
        if (isOnlineMode) {
            int localPlayerIndex = networkManager.getPlayerId() - 1;
            if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
                localPlayerForTurn = players.get(localPlayerIndex);
            }
        } else if (!players.isEmpty()) {
            localPlayerForTurn = players.get(0);
        }
        
        for (Player player : players) {
            if (!player.isRemotePlayer()) {
                player.update();
            } else {
                player.forceUpdateAnimation();
            }
            player.render(g2d);
        }
        
        if (waitingForTurnToComplete && localPlayerForTurn != null) {
            boolean hasPath = localPlayerForTurn.hasDestination();
            boolean isCurrentlyMoving = localPlayerForTurn.isMoving() || localPlayerForTurn.isAnimating();
            boolean hasTime = localPlayerForTurn.hasTimeRemaining();
            
            if (!isCurrentlyMoving && !hasPath && !hasTime) {
                waitingForTurnToComplete = false;
                
                System.out.println("=== Turn completed (time exhausted), moving to next turn ===");
                
                nextTurn();
            } else if (!isCurrentlyMoving && !hasPath && hasTime) {
                waitingForTurnToComplete = false;
                System.out.println("=== Turn completed (action finished, time remaining: " + localPlayerForTurn.getRemainingTime() + " hours) ===");
            }
        }
        
        if (isOnlineMode && networkManager.isConnected()) {
            int localPlayerIndex = networkManager.getPlayerId() - 1;
            if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
                Player localPlayer = players.get(localPlayerIndex);
                long currentTime = System.currentTimeMillis();
                if (!networkManager.hasLastUpdate() || (currentTime - networkManager.getLastUpdateTime()) > 50) {
                    networkManager.sendPlayerMove(localPlayer);
                    networkManager.setLastUpdateTime(currentTime);
                }
            }
        }
        
        if (anyHovering && hoverIndex != currentHoverIndex) {
            SoundManager.getInstance().playSFX(GameConfig.HOVER_SOUND);
            currentHoverIndex = hoverIndex;
        } else if (!anyHovering) {
            currentHoverIndex = -1;
        }
        
        if (anyHovering && hoverName != null) {
            renderHoverUI(g2d, hoverName);
        }
        
        Player currentTurnPlayer = null;
        if (isOnlineMode) {
            if (currentTurnPlayerId > 0 && currentTurnPlayerId <= players.size()) {
                currentTurnPlayer = players.get(currentTurnPlayerId - 1);
            }
        } else if (!players.isEmpty()) {
            currentTurnPlayer = players.get(0);
        }
        
        for (int i = 0; i < hudElements.size(); i++) {
            MenuElement element = hudElements.get(i);
            
            if (i >= 1 && i <= 4 && element.getType() == MenuElement.ElementType.TEXT) {
                String newText = "";
                
                switch (i) {
                    case 1:
                        newText = "ค่าทักษะ: " + (currentTurnPlayer != null ? currentTurnPlayer.getSkill() : 0);
                        break;
                    case 2:
                        newText = "ค่าสุขภาพ: " + (currentTurnPlayer != null ? currentTurnPlayer.getHealth() : 100);
                        break;
                    case 3:
                        newText = "ค่าการเรียน: " + (currentTurnPlayer != null ? currentTurnPlayer.getEducation() : 0);
                        break;
                    case 4:
                        newText = "ค่าเงิน: " + (currentTurnPlayer != null ? currentTurnPlayer.getMoney() : 500);
                        break;
                }
                
                element.setText(newText);
            } else if (i == 6 && element.getType() == MenuElement.ElementType.TEXT) {
                if (currentTurnPlayer != null) {
                    double remaining = currentTurnPlayer.getRemainingTime();
                    int remainingInt = (int)Math.ceil(remaining);
                    element.setText("P" + currentTurnPlayerId + " เหลือเวลา " + remainingInt + "/24 ชม");
                } else {
                    element.setText("P" + currentTurnPlayerId + " เหลือเวลา 0/24 ชม");
                }
            }
            
            element.render(g2d);
        }
        
        renderTurnDisplay(g2d);
    }
    
    private void renderTurnDisplay(Graphics2D g2d) {
        if (turnDisplayText == null || turnDisplayText.isEmpty() || turnDisplayStartTime == 0) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - turnDisplayStartTime;
        long totalDuration = TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT;
        
        if (elapsed > totalDuration) {
            turnDisplayAlpha = 0.0f;
            return;
        }
        
        if (elapsed < TURN_DISPLAY_FADE_IN) {
            turnDisplayAlpha = (float)elapsed / TURN_DISPLAY_FADE_IN;
        } else if (elapsed > TURN_DISPLAY_DURATION) {
            float fadeOutProgress = (float)(elapsed - TURN_DISPLAY_DURATION) / TURN_DISPLAY_FADE_OUT;
            turnDisplayAlpha = Math.max(0.0f, 1.0f - fadeOutProgress);
        } else {
            turnDisplayAlpha = 1.0f;
        }
        
        if (turnDisplayAlpha > 0.01f) {
            float bgAlpha = turnDisplayAlpha * 0.6f;
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha));
            g2d.setColor(new Color(0, 0, 0));
            g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            Font originalFont = g2d.getFont();
            g2d.setFont(FontManager.getThaiFont(Font.BOLD, 72));
            FontMetrics fm = g2d.getFontMetrics();
            
            int textWidth = fm.stringWidth(turnDisplayText);
            int textHeight = fm.getHeight();
            
            int centerX = GameConfig.WINDOW_WIDTH / 2;
            int centerY = GameConfig.WINDOW_HEIGHT / 2;
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, turnDisplayAlpha));
            
            Color textColor = new Color(255, 255, 255, (int)(255 * turnDisplayAlpha));
            g2d.setColor(textColor);
            
            int textX = centerX - textWidth / 2;
            int textY = centerY + textHeight / 4;
            
            g2d.drawString(turnDisplayText, textX, textY);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.setFont(originalFont);
        }
    }
    
    private void showGameStartDisplay() {
        turnDisplayText = "เริ่มต้นการใช้ชีวิตวันที่ หนึ่ง";
        turnDisplayStartTime = System.currentTimeMillis();
        turnDisplayAlpha = 0.0f;
        
        SoundManager.getInstance().playSFX(GameConfig.TURN_SOUND);
        
        javax.swing.Timer timer = new javax.swing.Timer((int)(TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT), _ -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                showTurnDisplay();
            });
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showTurnDisplay() {
        boolean isMyTurnNow = isMyTurn();
        
        if (isMyTurnNow) {
            turnDisplayText = "ถึงตาคุณใช้ชีวิต";
        } else {
            turnDisplayText = "รอบของ P" + currentTurnPlayerId;
        }
        
        turnDisplayStartTime = System.currentTimeMillis();
        turnDisplayAlpha = 0.0f;
        
        SoundManager.getInstance().playSFX(GameConfig.TURN_SOUND);
    }
    
    private void showWeekDisplay() {
        int remainingWeeks = GameConfig.MAX_TURNS - currentTurnNumber + 1;
        if (remainingWeeks < 0) remainingWeeks = 0;
        turnDisplayText = "ผ่านไปแล้ว " + (currentTurnNumber - 1) + " สัปดาห์เหลืออีก " + remainingWeeks + "/" + GameConfig.MAX_TURNS;
        turnDisplayStartTime = System.currentTimeMillis();
        turnDisplayAlpha = 0.0f;
        
        SoundManager.getInstance().playSFX(GameConfig.TURN_SOUND);
        
        javax.swing.Timer timer = new javax.swing.Timer((int)(TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT), _ -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                showTurnDisplay();
            });
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public boolean isMyTurn() {
        if (isOnlineMode) {
            return networkManager.getPlayerId() == currentTurnPlayerId;
        }
        return currentTurnPlayerId == 1;
    }
    
    public void setTurn(int turnPlayerId, int turnNumber, String updateType) {
        currentTurnPlayerId = turnPlayerId;
        currentTurnNumber = turnNumber;
        
        for (Player player : players) {
            if (player.getPlayerId() == turnPlayerId) {
                player.setRemainingTime(24.0);
            }
        }
        
        if (turnNumber == 1 && turnPlayerId == 1 && turnDisplayStartTime == 0) {
            showGameStartDisplay();
        } else if ("WEEK".equals(updateType)) {
            showWeekDisplay();
        } else {
            showTurnDisplay();
        }
    }
    
    public void nextTurn() {
        if (isOnlineMode && networkManager.isConnected()) {
            networkManager.sendMessage("TURN_COMPLETE:" + currentTurnPlayerId + ":" + currentTurnNumber);
        } else {
            int connectedCount = 1;
            if (connectedCount == 0) return;
            
            boolean wasLastPlayer = (currentTurnPlayerId >= connectedCount);
            
            for (Player player : players) {
                if (player.getPlayerId() == currentTurnPlayerId) {
                    player.setRemainingTime(24.0);
                }
            }
            
            currentTurnPlayerId++;
            if (currentTurnPlayerId > connectedCount) {
                currentTurnPlayerId = 1;
                currentTurnNumber++;
                
                if (currentTurnNumber > GameConfig.MAX_TURNS) {
                    currentTurnNumber = GameConfig.MAX_TURNS;
                    return;
                }
                
                if (wasLastPlayer && currentTurnNumber <= GameConfig.MAX_TURNS) {
                    showWeekDisplay();
                    return;
                }
            }
            
            for (Player player : players) {
                if (player.getPlayerId() == currentTurnPlayerId) {
                    player.setRemainingTime(24.0);
                }
            }
            
            showTurnDisplay();
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
        if (!isMyTurn()) {
            return;
        }
        
        for (int i = 0; i < hoverObjects.size(); i++) {
            GameObject obj = hoverObjects.get(i);
            if (isClicking(obj, x, y)) {
                GameConfig.HoverObject config = GameConfig.HOVER_OBJECTS[i];
                double targetX = config.playerX;
                double targetY = config.playerY;
                
                if (!players.isEmpty()) {
                    int localPlayerIndex = isOnlineMode ? networkManager.getPlayerId() - 1 : 0;
                    if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
                        Player localPlayer = players.get(localPlayerIndex);
                        
                        if (!localPlayer.hasTimeRemaining()) {
                            System.out.println("=== Player " + localPlayer.getPlayerId() + " has no time remaining ===");
                            return;
                        }
                        
                        if (localPlayer.isMoving() || localPlayer.isAnimating()) {
                            System.out.println("=== Player " + localPlayer.getPlayerId() + " is already moving, ignoring click ===");
                            return;
                        }
                        
                        double currentX = localPlayer.getX();
                        double currentY = localPlayer.getY();
                        double distance = Math.sqrt((targetX - currentX) * (targetX - currentX) + (targetY - currentY) * (targetY - currentY));
                        
                        if (distance < 10.0) {
                            System.out.println("=== Player " + localPlayer.getPlayerId() + " clicked same location (distance: " + distance + "), ignoring ===");
                            return;
                        }
                        
                        localPlayer.setDirection(config.direction);
                        localPlayer.setDestination(targetX, targetY, allPaths);
                        
                        if (isOnlineMode && networkManager.isConnected()) {
                            networkManager.sendPlayerMove(localPlayer);
                            networkManager.setLastUpdateTime(System.currentTimeMillis());
                        }
                        
                        waitingForTurnToComplete = true;
                        
                        System.out.println("=== Turn started: Player " + (isOnlineMode ? networkManager.getPlayerId() : 1) + " clicked, remaining time: " + localPlayer.getRemainingTime() + " hours ===");
                    }
                }
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
        return players.isEmpty() ? null : players.get(0);
    }
    
    public ArrayList<Player> getPlayers() {
        return players;
    }
    
    public void updateRemotePlayer(int playerId, double x, double y, String direction, boolean isMoving, double remainingTime) {
        int localPlayerId = networkManager.getPlayerId();
        if (playerId == localPlayerId) {
            return;
        }
        
        int playerIndex = playerId - 1;
        
        if (playerIndex >= 0 && playerIndex < players.size()) {
            Player remotePlayer = players.get(playerIndex);
            
            if (remotePlayer.getPlayerId() != playerId) {
                remotePlayer.setPlayerId(playerId);
            }
            
            remotePlayer.setRemainingTime(remainingTime);
            
            boolean wasMoving = remotePlayer.isMoving();
            
            if (isMoving && !wasMoving) {
                remotePlayer.setDirection(direction);
                remotePlayer.setDestination(x, y, allPaths, false);
            } else if (!isMoving && wasMoving) {
                remotePlayer.forceUpdateAnimation();
                remotePlayer.setX(x);
                remotePlayer.setY(y);
                remotePlayer.setMoving(false);
            } else if (isMoving && wasMoving) {
                remotePlayer.setDirection(direction);
            } else if (!isMoving && !wasMoving) {
                double dx = x - remotePlayer.getX();
                double dy = y - remotePlayer.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0.01) {
                    remotePlayer.setX(x);
                    remotePlayer.setY(y);
                    remotePlayer.setDirection(direction);
                }
            }
        }
    }
    
    public void removePlayer(int playerId) {
        int playerIndex = playerId - 1;
        if (playerIndex >= 0 && playerIndex < players.size()) {
            players.remove(playerIndex);
        }
    }
}

