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
            NetworkLogger.getInstance().log("=== NetworkManager: Connecting to " + host + ":" + port + " ===");
            socket = new Socket(host, port);
            NetworkLogger.getInstance().log("=== NetworkManager: Socket connected ===");
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            NetworkLogger.getInstance().log("=== NetworkManager: Streams created ===");
            
            connected = true;
            listenerThread = new Thread(() -> listenToServer());
            listenerThread.start();
            
            NetworkLogger.getInstance().log("=== NetworkManager: Connection ready ===");
            return true;
        } catch (IOException e) {
            NetworkLogger.getInstance().log("=== NetworkManager: Connection failed: " + e.getMessage() + " ===");
            connected = false;
            return false;
        } catch (Exception e) {
            NetworkLogger.getInstance().log("=== NetworkManager: Unexpected error: " + e.getMessage() + " ===");
            connected = false;
            return false;
        }
    }
    
    private void listenToServer() {
        NetworkLogger.getInstance().log("=== NetworkManager: listenToServer started ===");
        while (connected) {
            try {
                String line = in.readLine();
                if (line == null) break;
                NetworkLogger.getInstance().log("=== NetworkManager: Received: " + line + " ===");
                
                if (line.equals("LOBBY_UPDATE")) {
                  
                    String countLine = in.readLine();
                    if (countLine != null && countLine.startsWith("PLAYER_COUNT:")) {
                        int count = Integer.parseInt(countLine.split(":")[1]);
                        NetworkLogger.getInstance().log("=== NetworkManager: Player count: " + count + " ===");
                        List<PlayerInfo> newPlayers = new ArrayList<>();
                        for (int i = 1; i <= count; i++) {
                            newPlayers.add(new PlayerInfo(i, "Player_" + i, true));
                        }
                        handleLobbyUpdate(newPlayers);
                    }
                } else if (line.equals("START_GAME")) {
                    NetworkLogger.getInstance().log("=== NetworkManager: Received START_GAME ===");
                    SwingUtilities.invokeLater(() -> {
                        if (lobbyMenu != null) {
                            NetworkLogger.getInstance().log("=== NetworkManager: Starting game ===");
                            lobbyMenu.startGame();
                        }
                    });
                } else if (line != null && line.startsWith("PLAYER_ID:")) {
         
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        int assignedPlayerId = Integer.parseInt(parts[1]);
                        playerId = assignedPlayerId;
                        NetworkLogger.getInstance().log("=== NetworkManager: Assigned player ID: " + playerId + " ===");
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
                        double destX = parts.length >= 8 ? Double.parseDouble(parts[7]) : x;
                        double destY = parts.length >= 9 ? Double.parseDouble(parts[8]) : y;
                        
                        NetworkLogger.getInstance().log("=== NetworkManager: Received PLAYER_MOVE for player " + remotePlayerId + " (time: " + remainingTime + ") ===");
                        handlePlayerMove(remotePlayerId, x, y, direction, isMoving, remainingTime, destX, destY);
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
                } else if (line != null && line.equals("GAME_RESET")) {
                    SwingUtilities.invokeLater(() -> {
                        players.clear();
                        if (lobbyMenu != null) {
                            lobbyMenu.updateLobbyInfo(players);
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
                    } else if (line != null && line.startsWith("PLAYER_HOVER:")) {
                        String[] parts = line.split(":");
                        if (parts.length >= 3) {
                            int hoverPlayerId = Integer.parseInt(parts[1]);
                            int hoverIndex = Integer.parseInt(parts[2]);
                            handlePlayerHover(hoverPlayerId, hoverIndex);
                        }
                    } else if (line != null && line.startsWith("UPDATE_STATS:")) {
                        String[] parts = line.split(":");
                        if (parts.length >= 6) {
                            int targetPlayerId = Integer.parseInt(parts[1]);
                            int skill = Integer.parseInt(parts[2]);
                            int education = Integer.parseInt(parts[3]);
                            int health = Integer.parseInt(parts[4]);
                            int money = Integer.parseInt(parts[5]);
                            handlePlayerStatsUpdate(targetPlayerId, skill, education, health, money);
                        }
                    }
            } catch (IOException e) {
                if (connected) {
                    NetworkLogger.getInstance().log("=== NetworkManager: Error receiving: " + e.getMessage() + " ===");
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
        NetworkLogger.getInstance().log("=== NetworkManager: Stopped listening ===");
    }
    
    private void handleLobbyUpdate(List<PlayerInfo> newPlayers) {
        players = newPlayers;
        NetworkLogger.getInstance().log("=== NetworkManager: Updated players list ===");
        SwingUtilities.invokeLater(() -> {
            if (lobbyMenu != null) {
                lobbyMenu.updateLobbyInfo(players);
            }
        });
    }
    
    private void handlePlayerMove(int playerId, double x, double y, String direction, boolean isMoving, double remainingTime, double destX, double destY) {
        NetworkLogger.getInstance().log("=== NetworkManager: Handling move for player " + playerId + " (time: " + remainingTime + ") ===");
        
        SwingUtilities.invokeLater(() -> {
            if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                if (scene != null) {
                    scene.updateRemotePlayer(playerId, x, y, direction, isMoving, remainingTime, destX, destY);
                }
            }
        });
    }
    
    private void handlePlayerDisconnect(int disconnectedPlayerId) {
        NetworkLogger.getInstance().log("=== NetworkManager: Handling disconnect for player " + disconnectedPlayerId + " ===");
        
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
        NetworkLogger.getInstance().log("=== NetworkManager: Handling turn update - Player " + turnPlayerId + ", Turn " + turnNumber + ", Type: " + updateType + " ===");
        
        SwingUtilities.invokeLater(() -> {
            if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                if (scene != null) {
                    scene.setTurn(turnPlayerId, turnNumber, updateType);
                }
            }
        });
    }
    
    private void handlePlayerHover(int playerId, int hoverIndex) {
        NetworkLogger.getInstance().log("=== NetworkManager: Player " + playerId + " hover index: " + hoverIndex + " ===");
        
        SwingUtilities.invokeLater(() -> {
            if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                if (scene != null) {
                    scene.setRemotePlayerHover(playerId, hoverIndex);
                }
            }
        });
    }
    
    private void handlePlayerStatsUpdate(int playerId, int skill, int education, int health, int money) {
        NetworkLogger.getInstance().log("=== NetworkManager: Updating stats for player " + playerId + " - Skill:" + skill + " Education:" + education + " Health:" + health + " Money:" + money + " ===");
        
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                if (currentGameWindow != null && currentGameWindow.getGamePanel() != null) {
                    game.GameScene scene = currentGameWindow.getGamePanel().getGameScene();
                    if (scene != null) {
                        scene.updatePlayerStats(playerId, skill, education, health, money);
                        currentGameWindow.getGamePanel().repaint();
                    }
                }
            });
        }).start();
    }
    
    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
            out.flush();
        }
    }
    
    public void sendPlayerMove(game.Player player) {
        if (connected && out != null && playerId > 0) {
            String moveData = String.format("PLAYER_MOVE:%d:%.2f:%.2f:%s:%s:%.2f:%.2f:%.2f",
                playerId,
                player.getX(),
                player.getY(),
                player.getDirection(),
                player.isMoving() ? "true" : "false",
                player.getRemainingTime(),
                player.getDestinationX(),
                player.getDestinationY());
            out.println(moveData);
            out.flush();
        }
    }
    
    public void sendPlayerHover(int playerId, int hoverIndex) {
        if (connected && out != null && playerId > 0) {
            String hoverData = String.format("PLAYER_HOVER:%d:%d", playerId, hoverIndex);
            out.println(hoverData);
            out.flush();
        }
    }
    
    public void sendPlayerStats(int playerId, int skill, int education, int health, int money, int bankDeposit) {
        if (connected && out != null && playerId > 0) {
            String statsData = String.format("UPDATE_STATS:%d:%d:%d:%d:%d:%d", playerId, skill, education, health, money, bankDeposit);
            out.println(statsData);
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
            NetworkLogger.getInstance().log("Error closing socket: " + e.getMessage());
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

