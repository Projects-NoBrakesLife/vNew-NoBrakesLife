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
        logWindow.setServer(this);
        logWindow.setVisible(true);
        NetworkLogger.getInstance().log("=== GameServer: Constructor completed ===");
    }

    public List<PlayerInfo> getPlayers() {
        return new ArrayList<>(players);
    }

    public ServerLogWindow getLogWindow() {
        return logWindow;
    }

    public void broadcastPlayerStatsUpdate(PlayerInfo playerInfo) {
        String message = String.format("UPDATE_STATS:%d:%d:%d:%d:%d:%d",
                playerInfo.playerId,
                playerInfo.skill,
                playerInfo.education,
                playerInfo.health,
                playerInfo.money,
                playerInfo.bankDeposit);

        synchronized (players) {
            for (PlayerInfo p : players) {
                if (p.playerId == playerInfo.playerId) {
                    p.skill = playerInfo.skill;
                    p.education = playerInfo.education;
                    p.health = playerInfo.health;
                    p.money = playerInfo.money;
                    p.bankDeposit = playerInfo.bankDeposit;
                    break;
                }
            }
        }

        synchronized (clients) {
            List<ClientHandler> clientsCopy = new ArrayList<>(clients);
            for (ClientHandler client : clientsCopy) {
                try {
                    client.sendText(message);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(GameConfig.SERVER_PORT);
            logWindow.addLog("Server started on port " + GameConfig.SERVER_PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                logWindow.addLog("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);

                Thread handlerThread = new Thread(handler, "Handler-" + clientSocket.getInetAddress());
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
        } catch (IOException e) {
            logWindow.addLog("Server error: " + e.getMessage());
        }
    }

    public void handleClientJoin(ClientHandler client) {
        if (gameStarted) {
            client.sendText("GAME_STARTED");
            return;
        }

        int connectedCount = 0;
        for (PlayerInfo p : players) {
            if (p.isConnected)
                connectedCount++;
        }

        if (connectedCount < GameConfig.MAX_PLAYERS) {
            int playerId = getNextAvailablePlayerId();
            if (playerId > 0) {
                client.setPlayerId(playerId);
                players.get(playerId - 1).isConnected = true;
                logWindow.addLog("Player " + playerId + " joined");

                int newConnectedCount = 0;
                for (PlayerInfo p : players) {
                    if (p.isConnected)
                        newConnectedCount++;
                }

                List<PlayerInfo> lobbyData = new ArrayList<>(players);
                GameMessage message = new GameMessage(GameMessage.MessageType.UPDATE_LOBBY, lobbyData, -1);

                broadcastToAll(message);
                logWindow.addLog("Broadcasted lobby update to all clients");

                client.sendText("PLAYER_ID:" + playerId);

                final int checkCount = newConnectedCount;
                if (checkCount >= GameConfig.MIN_PLAYERS_TO_START) {
                    gameStarted = true;

                    final List<ClientHandler> clientsToNotify = new ArrayList<>(clients);
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            for (ClientHandler c : clientsToNotify) {
                                c.sendText("START_GAME");
                            }
                            logWindow.addLog("Game started! Broadcasting to " + clientsToNotify.size() + " client(s)");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            }
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
        NetworkLogger.getInstance().log("=== GameServer: Broadcasting to " + clients.size() + " clients ===");
        for (ClientHandler client : clients) {
            NetworkLogger.getInstance().log("=== GameServer: Sending to client " + client.getPlayerId() + " ===");
            client.sendMessage(message);
        }
        NetworkLogger.getInstance().log("=== GameServer: Broadcast complete ===");
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

    public synchronized void resetGame() {
        gameStarted = false;
        synchronized (players) {
            for (PlayerInfo p : players) {
                p.isConnected = false;
            }
        }
        List<ClientHandler> clientsCopy = new ArrayList<>(clients);
        for (ClientHandler c : clientsCopy) {
            try {
                c.sendText("GAME_RESET");
            } catch (Exception ignored) {
            }
        }
        if (logWindow != null) {
            logWindow.addLog("Game has been reset. Waiting for new players...");
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
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                server.handleClientJoin(this);

                while (connected && !socket.isClosed()) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }

                        if (line != null && line.startsWith("PLAYER_MOVE:")) {
                            for (ClientHandler client : server.clients) {
                                if (client != this) {
                                    client.sendText(line);
                                }
                            }
                        } else if (line != null && line.startsWith("PLAYER_HOVER:")) {
                            for (ClientHandler client : server.clients) {
                                if (client != this) {
                                    client.sendText(line);
                                }
                            }
                        } else if (line != null && line.startsWith("UPDATE_STATS:")) {
                            String[] parts = line.split(":");
                            if (parts.length >= 7) {
                                int targetPlayerId = Integer.parseInt(parts[1]);
                                int skill = Integer.parseInt(parts[2]);
                                int education = Integer.parseInt(parts[3]);
                                int health = Integer.parseInt(parts[4]);
                                int money = Integer.parseInt(parts[5]);
                                int bankDeposit = Integer.parseInt(parts[6]);

                                PlayerInfo pInfo = new PlayerInfo(targetPlayerId, "Player_" + targetPlayerId, true);
                                pInfo.skill = skill;
                                pInfo.education = education;
                                pInfo.health = health;
                                pInfo.money = money;
                                pInfo.bankDeposit = bankDeposit;
                                server.broadcastPlayerStatsUpdate(pInfo);
                            }
                        } else if (line != null && line.startsWith("SYNC_PLAYER:")) {
                            for (ClientHandler client : server.clients) {
                                client.sendText(line);
                            }
                        } else if (line != null && line.startsWith("TURN_COMPLETE:")) {
                            String[] parts = line.split(":");
                            if (parts.length >= 3) {
                                int turnPlayerId = Integer.parseInt(parts[1]);
                                int turnNumber = Integer.parseInt(parts[2]);

                                int connectedCount = 0;
                                for (PlayerInfo p : server.players) {
                                    if (p.isConnected)
                                        connectedCount++;
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

                                String turnUpdate = "TURN_UPDATE:" + nextTurnPlayerId + ":" + turnNumber + ":"
                                        + (isNewWeek ? "WEEK" : "TURN");
                                for (ClientHandler client : server.clients) {
                                    client.sendText(turnUpdate);
                                }
                            }
                        } else if ("RESET_GAME".equals(line)) {
                            server.resetGame();
                        }

                    } catch (IOException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                server.logWindow.addLog("Client error: " + e.getMessage());
            } finally {
                connected = false;

                int disconnectedPlayerId = getPlayerId();
                if (disconnectedPlayerId > 0) {
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
                    }
                } catch (IOException ignored) {
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void sendMessage(GameMessage message) {
            if (out != null) {
                try {
                    if (message.data instanceof List) {
                        List<PlayerInfo> players = (List<PlayerInfo>) message.data;
                        int connectedCount = 0;
                        for (PlayerInfo p : players) {
                            if (p.isConnected)
                                connectedCount++;
                        }

                        out.println("LOBBY_UPDATE");
                        out.println("PLAYER_COUNT:" + connectedCount);
                        out.flush();
                    }
                } catch (Exception e) {
                    server.logWindow.addLog("Error sending message: " + e.getMessage());
                }
            }
        }

        public synchronized void sendText(String message) {
            if (out != null && connected) {
                try {
                    out.println(message);
                    out.flush();
                } catch (Exception e) {
                    connected = false;
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
