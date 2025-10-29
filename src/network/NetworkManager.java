package network;

import game.GameLobbyMenu;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private int playerId = -1;
    private GameLobbyMenu lobbyMenu;
    private List<PlayerInfo> players;
    private Thread listenerThread;
    private long lastUpdateTime = 0;
    private game.GameWindow currentGameWindow;
    
    private NetworkManager() {
        players = new ArrayList<>();
    }
    
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }
    
    public boolean connect(String host, int port) {
        try {
            System.out.println("=== NetworkManager: Connecting to " + host + ":" + port + " ===");
            socket = new Socket(host, port);
            System.out.println("=== NetworkManager: Socket connected ===");
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("=== NetworkManager: Streams created ===");
            
            connected = true;
            listenerThread = new Thread(() -> listenToServer());
            listenerThread.start();
            
            System.out.println("=== NetworkManager: Connection ready ===");
            return true;
        } catch (IOException e) {
            System.err.println("=== NetworkManager: Connection failed: " + e.getMessage() + " ===");
            connected = false;
            return false;
        } catch (Exception e) {
            System.err.println("=== NetworkManager: Unexpected error: " + e.getMessage() + " ===");
            connected = false;
            return false;
        }
    }
    
    private void listenToServer() {
        System.out.println("=== NetworkManager: listenToServer started ===");
        while (connected) {
            try {
                String line = in.readLine();
                if (line == null) break;
                System.out.println("=== NetworkManager: Received: " + line + " ===");
                
                if (line.equals("LOBBY_UPDATE")) {
                  
                    String countLine = in.readLine();
                    if (countLine != null && countLine.startsWith("PLAYER_COUNT:")) {
                        int count = Integer.parseInt(countLine.split(":")[1]);
                        System.out.println("=== NetworkManager: Player count: " + count + " ===");
                        List<PlayerInfo> newPlayers = new ArrayList<>();
                        for (int i = 1; i <= count; i++) {
                            newPlayers.add(new PlayerInfo(i, "Player_" + i, true));
                        }
                        handleLobbyUpdate(newPlayers);
                    }
                } else if (line.equals("START_GAME")) {
                    System.out.println("=== NetworkManager: Received START_GAME ===");
                    SwingUtilities.invokeLater(() -> {
                        if (lobbyMenu != null) {
                            System.out.println("=== NetworkManager: Starting game ===");
                            lobbyMenu.startGame();
                        }
                    });
                } else if (line != null && line.startsWith("PLAYER_ID:")) {
         
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        int assignedPlayerId = Integer.parseInt(parts[1]);
                        playerId = assignedPlayerId;
                        System.out.println("=== NetworkManager: Assigned player ID: " + playerId + " ===");
                    }
                } else if (line != null && line.startsWith("PLAYER_MOVE:")) {
                
                    String[] parts = line.split(":");
                    if (parts.length >= 6) {
                        int remotePlayerId = Integer.parseInt(parts[1]);
                        double x = Double.parseDouble(parts[2]);
                        double y = Double.parseDouble(parts[3]);
                        String direction = parts[4];
                        boolean isMoving = "true".equals(parts[5]);
                        double remainingTime = parts.length >= 7 ? Double.parseDouble(parts[6]) : 24.0;
                        
                        System.out.println("=== NetworkManager: Received PLAYER_MOVE for player " + remotePlayerId + " (time: " + remainingTime + ") ===");
                        handlePlayerMove(remotePlayerId, x, y, direction, isMoving, remainingTime);
                    }
                } else if (line != null && line.startsWith("PLAYER_DISCONNECT:")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        int disconnectedPlayerId = Integer.parseInt(parts[1]);
                        handlePlayerDisconnect(disconnectedPlayerId);
                    }
                } else if (line != null && line.equals("GAME_STARTED")) {
                    SwingUtilities.invokeLater(() -> {
                        if (lobbyMenu != null) {
                            lobbyMenu.showGameStartedMessage();
                        }
                    });
                } else if (line != null && line.startsWith("TURN_UPDATE:")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 3) {
                        int turnPlayerId = Integer.parseInt(parts[1]);
                        int turnNumber = Integer.parseInt(parts[2]);
                        String updateType = parts.length >= 4 ? parts[3] : "TURN";
                        handleTurnUpdate(turnPlayerId, turnNumber, updateType);
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("=== NetworkManager: Error receiving: " + e.getMessage() + " ===");
                    e.printStackTrace();
                }
                connected = false;
                
                SwingUtilities.invokeLater(() -> {
                    if (lobbyMenu != null) {
                        lobbyMenu.showConnectionError();
                    }
                });
                
                break;
            }
        }
        System.out.println("=== NetworkManager: Stopped listening ===");
    }
    
    private void handleLobbyUpdate(List<PlayerInfo> newPlayers) {
        players = newPlayers;
        System.out.println("=== NetworkManager: Updated players list ===");
        SwingUtilities.invokeLater(() -> {
            if (lobbyMenu != null) {
                lobbyMenu.updateLobbyInfo(players);
            }
        });
    }
    
    private void handlePlayerMove(int playerId, double x, double y, String direction, boolean isMoving, double remainingTime) {
        System.out.println("=== NetworkManager: Handling move for player " + playerId + " (time: " + remainingTime + ") ===");
        
        SwingUtilities.invokeLater(() -> {
            if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                if (scene != null) {
                    scene.updateRemotePlayer(playerId, x, y, direction, isMoving, remainingTime);
                }
            }
        });
    }
    
    private void handlePlayerDisconnect(int disconnectedPlayerId) {
        System.out.println("=== NetworkManager: Handling disconnect for player " + disconnectedPlayerId + " ===");
        
        SwingUtilities.invokeLater(() -> {
            if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                if (scene != null) {
                    scene.removePlayer(disconnectedPlayerId);
                }
            }
        });
    }
    
    public void setLobbyMenu(GameLobbyMenu lobbyMenu) {
        this.lobbyMenu = lobbyMenu;
    }
    
    public void setGameWindow(game.GameWindow gameWindow) {
        this.currentGameWindow = gameWindow;
    }
    
    private void handleTurnUpdate(int turnPlayerId, int turnNumber, String updateType) {
        System.out.println("=== NetworkManager: Handling turn update - Player " + turnPlayerId + ", Turn " + turnNumber + ", Type: " + updateType + " ===");
        
        SwingUtilities.invokeLater(() -> {
            if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                if (scene != null) {
                    scene.setTurn(turnPlayerId, turnNumber, updateType);
                }
            }
        });
    }
    
    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
            out.flush();
        }
    }
    
    public void sendPlayerMove(game.Player player) {
        if (connected && out != null && playerId > 0) {
            String moveData = String.format("PLAYER_MOVE:%d:%.2f:%.2f:%s:%s:%.2f", 
                playerId, 
                player.getX(), 
                player.getY(),
                player.getDirection(),
                player.isMoving() ? "true" : "false",
                player.getRemainingTime());
            out.println(moveData);
            out.flush();
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
    public List<PlayerInfo> getPlayers() {
        return players;
    }
    
    public boolean hasLastUpdate() {
        return lastUpdateTime != 0;
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }
}

