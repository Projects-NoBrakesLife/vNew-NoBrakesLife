package network;

import game.GameConfig;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private List<PlayerInfo> players;
    private boolean running = false;
    private boolean gameStarted = false;
    private ServerLogWindow logWindow;
    
    public GameServer(int port) {
        clients = new CopyOnWriteArrayList<>();
        players = new ArrayList<>();
        
        for (int i = 1; i <= GameConfig.MAX_PLAYERS; i++) {
            players.add(new PlayerInfo(i, "Player_" + i, false));
        }
        
        logWindow = new ServerLogWindow();
        logWindow.setVisible(true);
        System.out.println("=== GameServer: Constructor completed ===");
    }
    
    public void start() {
        System.out.println("=== GameServer: start() called ===");
        running = true;
        try {
            serverSocket = new ServerSocket(GameConfig.SERVER_PORT);
            logWindow.addLog("Server started on port " + GameConfig.SERVER_PORT);
            System.out.println("=== GameServer: Server started, waiting for clients ===");
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("=== GameServer: Client connected: " + clientSocket.getInetAddress() + " ===");
                logWindow.addLog("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                System.out.println("=== GameServer: Starting handler thread ===");
                
                Thread handlerThread = new Thread(handler, "Handler-" + clientSocket.getInetAddress());
                handlerThread.setDaemon(true);
                handlerThread.start();
                System.out.println("=== GameServer: Handler thread started, returning to accept ===");
            }
        } catch (IOException e) {
            logWindow.addLog("Server error: " + e.getMessage());
            System.out.println("=== GameServer: Error: " + e.getMessage() + " ===");
            e.printStackTrace();
        }
    }
    
    public void handleClientJoin(ClientHandler client) {
        System.out.println("=== GameServer.handleClientJoin called ===");
     
        if (gameStarted) {
            System.out.println("=== GameServer: Game already started, rejecting client ===");
            client.sendText("GAME_STARTED");
            return;
        }
        
        int connectedCount = 0;
        for (PlayerInfo p : players) {
            if (p.isConnected) connectedCount++;
        }
        System.out.println("=== GameServer: Current connected players: " + connectedCount + " ===");
        
        if (connectedCount < GameConfig.MAX_PLAYERS) {
            int playerId = getNextAvailablePlayerId();
            System.out.println("=== GameServer: Got playerId = " + playerId + " ===");
            if (playerId > 0) {
                client.setPlayerId(playerId);
                players.get(playerId - 1).isConnected = true;
                logWindow.addLog("Player " + playerId + " joined");
             
                int newConnectedCount = 0;
                for (PlayerInfo p : players) {
                    if (p.isConnected) newConnectedCount++;
                }
                System.out.println("=== GameServer: Connected count AFTER update: " + newConnectedCount + " ===");
                
                List<PlayerInfo> lobbyData = new ArrayList<>(players);
                System.out.println("=== GameServer: Preparing lobby update with " + lobbyData.size() + " players ===");
                for (PlayerInfo p : lobbyData) {
                    System.out.println("  Player " + p.playerId + ": " + p.playerName + " - " + (p.isConnected ? "CONNECTED" : "DISCONNECTED"));
                }
                
                GameMessage message = new GameMessage(GameMessage.MessageType.UPDATE_LOBBY, lobbyData, -1);
                
                System.out.println("=== GameServer: Broadcasting to all clients ===");
                broadcastToAll(message);
                logWindow.addLog("Broadcasted lobby update to all clients");
                System.out.println("=== GameServer: Message sent successfully ===");
                
               
                client.sendText("PLAYER_ID:" + playerId);
                System.out.println("=== GameServer: Sent player ID " + playerId + " to client ===");
                
              
                final int checkCount = newConnectedCount;
                System.out.println("checkCount "+checkCount);
                if (checkCount >= GameConfig.MIN_PLAYERS_TO_START) {
                    System.out.println("=== GameServer: Player(s) joined (" + checkCount + "), will start game in 3 seconds ===");
                    gameStarted = true;
                    
                    final List<ClientHandler> clientsToNotify = new ArrayList<>(clients);
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            System.out.println("=== GameServer: Sending START_GAME message to " + clientsToNotify.size() + " client(s) ===");
                            for (ClientHandler c : clientsToNotify) {
                                c.sendText("START_GAME");
                            }
                            logWindow.addLog("Game started! Broadcasting to " + clientsToNotify.size() + " client(s)");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        } else {
            System.out.println("=== GameServer: Max players reached (" + connectedCount + "/" + GameConfig.MAX_PLAYERS + "), cannot join ===");
        }
    }
    
        public void handleClientDisconnect(ClientHandler client) {
            clients.remove(client);
            int playerId = client.getPlayerId();
            if (playerId > 0 && playerId <= players.size()) {
                players.get(playerId - 1).isConnected = false;
                broadcastToAll(new GameMessage(GameMessage.MessageType.UPDATE_LOBBY, new ArrayList<>(players), -1));
                logWindow.addLog("Player " + playerId + " left");
            }
            client.disconnect();
        }
    
    private int getNextAvailablePlayerId() {
        for (int i = 1; i <= GameConfig.MAX_PLAYERS; i++) {
            if (!players.get(i - 1).isConnected) {
                return i;
            }
        }
        return -1;
    }
    
    public void broadcastToAll(GameMessage message) {
        System.out.println("=== GameServer: Broadcasting to " + clients.size() + " clients ===");
        for (ClientHandler client : clients) {
            System.out.println("=== GameServer: Sending to client " + client.getPlayerId() + " ===");
            client.sendMessage(message);
        }
        System.out.println("=== GameServer: Broadcast complete ===");
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            logWindow.addLog("Server stopped");
        } catch (IOException e) {
            logWindow.addLog("Error stopping server: " + e.getMessage());
        }
    }
    
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private GameServer server;
        private int playerId = -1;
        private boolean connected = true;
        
        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }
        
        @Override
        public void run() {
            System.out.println("=== ClientHandler.run() STARTED ===");
            
            try {
                System.out.println("=== ClientHandler: Creating streams ===");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("=== ClientHandler: Streams created successfully ===");
                
                System.out.println("=== ClientHandler: Calling handleClientJoin ===");
                server.handleClientJoin(this);
                System.out.println("=== ClientHandler: handleClientJoin completed ===");
                
                System.out.println("=== ClientHandler: Starting message loop ===");
                while (connected && !socket.isClosed()) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            System.out.println("=== ClientHandler: Client disconnected ===");
                            break;
                        }
                        System.out.println("=== ClientHandler: Received: " + line + " ===");
                        
                     
                        if (line != null && line.startsWith("PLAYER_MOVE:")) {
                      
                            System.out.println("=== ClientHandler: Broadcasting player move to other clients ===");
                            for (ClientHandler client : server.clients) {
                                if (client != this) {
                                    client.sendText(line);
                                }
                            }
                        } else if (line != null && line.startsWith("TURN_COMPLETE:")) {
                            String[] parts = line.split(":");
                            if (parts.length >= 3) {
                                int turnPlayerId = Integer.parseInt(parts[1]);
                                int turnNumber = Integer.parseInt(parts[2]);
                                
                                int connectedCount = 0;
                                for (PlayerInfo p : server.players) {
                                    if (p.isConnected) connectedCount++;
                                }
                                
                                int nextTurnPlayerId = turnPlayerId + 1;
                                boolean isNewWeek = false;
                                
                                if (nextTurnPlayerId > connectedCount) {
                                    nextTurnPlayerId = 1;
                                    turnNumber++;
                                    isNewWeek = true;
                                    
                                    if (turnNumber > GameConfig.MAX_TURNS) {
                                        turnNumber = GameConfig.MAX_TURNS;
                                    }
                                }
                                
                                String turnUpdate = "TURN_UPDATE:" + nextTurnPlayerId + ":" + turnNumber + ":" + (isNewWeek ? "WEEK" : "TURN");
                                System.out.println("=== ClientHandler: Broadcasting turn update: " + turnUpdate + " ===");
                                for (ClientHandler client : server.clients) {
                                    client.sendText(turnUpdate);
                                }
                            }
                        }
                     
                    } catch (IOException e) {
                        if (connected) {
                            System.out.println("=== ClientHandler: IO error: " + e.getMessage() + " ===");
                        }
                        break;
                    }
                }
                System.out.println("=== ClientHandler: Message loop ended ===");
            } catch (IOException e) {
                System.out.println("=== ClientHandler: IOException: " + e.getMessage() + " ===");
                e.printStackTrace();
                server.logWindow.addLog("Client error: " + e.getMessage());
            } finally {
                System.out.println("=== ClientHandler: Cleaning up ===");
                connected = false;
                
                int disconnectedPlayerId = getPlayerId();
                if (disconnectedPlayerId > 0) {
                    System.out.println("=== ClientHandler: Broadcasting disconnect for player " + disconnectedPlayerId + " ===");
                    for (ClientHandler client : server.clients) {
                        if (client != this && client.connected) {
                            client.sendText("PLAYER_DISCONNECT:" + disconnectedPlayerId);
                        }
                    }
                }
                
                server.handleClientDisconnect(this);
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        System.out.println("=== ClientHandler: Socket closed ===");
                    }
                } catch (IOException e) {
                    System.out.println("=== ClientHandler: Error closing socket: " + e.getMessage() + " ===");
                }
                System.out.println("=== ClientHandler.run() ENDED ===");
            }
        }
        
        @SuppressWarnings("unchecked")
        public void sendMessage(GameMessage message) {
            if (out != null) {
                try {
                    System.out.println("=== ClientHandler: Sending LOBBY_UPDATE ===");
                    if (message.data instanceof List) {
                        List<PlayerInfo> players = (List<PlayerInfo>) message.data;
                        int connectedCount = 0;
                        for (PlayerInfo p : players) {
                            if (p.isConnected) connectedCount++;
                        }
                        
                        out.println("LOBBY_UPDATE");
                        out.println("PLAYER_COUNT:" + connectedCount);
                        out.flush();
                        System.out.println("=== ClientHandler: Sent player count: " + connectedCount + " ===");
                    }
                } catch (Exception e) {
                    server.logWindow.addLog("Error sending message: " + e.getMessage());
                    System.out.println("=== ClientHandler: Error sending: " + e.getMessage() + " ===");
                    e.printStackTrace();
                }
            } else {
                System.out.println("=== ClientHandler: ERROR - out is null! ===");
            }
        }
        
        public void sendText(String message) {
            if (out != null) {
                try {
                    System.out.println("=== ClientHandler: Sending text: " + message + " ===");
                    out.println(message);
                    out.flush();
                    System.out.println("=== ClientHandler: Text sent successfully ===");
                } catch (Exception e) {
                    server.logWindow.addLog("Error sending text: " + e.getMessage());
                    System.out.println("=== ClientHandler: Error sending text: " + e.getMessage() + " ===");
                }
            }
        }
        
        public void disconnect() {
            connected = false;
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                server.logWindow.addLog("Error disconnecting client: " + e.getMessage());
            }
        }
        
        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }
        
        public int getPlayerId() {
            return playerId;
        }
    }
}

