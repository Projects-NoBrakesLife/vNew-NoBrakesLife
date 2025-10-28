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
        
        for (Player player : players) {
            if (!player.isRemotePlayer()) {
                player.update();
            } else {
                player.forceUpdateAnimation();
            }
            player.render(g2d);
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
                
            
                if (!players.isEmpty()) {
                    int localPlayerIndex = isOnlineMode ? networkManager.getPlayerId() - 1 : 0;
                    if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
                        Player localPlayer = players.get(localPlayerIndex);
                        localPlayer.setDirection(config.direction);
                        localPlayer.setDestination(targetX, targetY, allPaths);
                        
                        if (isOnlineMode && networkManager.isConnected()) {
                            networkManager.sendPlayerMove(localPlayer);
                            networkManager.setLastUpdateTime(System.currentTimeMillis());
                        }
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
    
    public void updateRemotePlayer(int playerId, double x, double y, String direction, boolean isMoving) {
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
            
            boolean wasMoving = remotePlayer.isMoving();
            
            if (isMoving && !wasMoving) {
                remotePlayer.setDirection(direction);
                remotePlayer.setDestination(x, y, allPaths);
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

