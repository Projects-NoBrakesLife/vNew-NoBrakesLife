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
        String message = String.format("UPDATE_STATS:%d:%d:%d:%d:%d",
                playerInfo.playerId,
                playerInfo.skill,
                playerInfo.education,
                playerInfo.health,
                playerInfo.money);

        synchronized (players) {
            for (PlayerInfo p : players) {
                if (p.playerId == playerInfo.playerId) {
                    p.skill = playerInfo.skill;
                    p.education = playerInfo.education;
                    p.health = playerInfo.health;
                    p.money = playerInfo.money;
                    break;
                }
            }
        }

        new Thread(() -> {
            synchronized (clients) {
                List<ClientHandler> clientsCopy = new ArrayList<>(clients);
                for (ClientHandler client : clientsCopy) {
                    try {
                        client.sendText(message);
                        NetworkLogger.getInstance()
                                .log("=== GameServer: Sent UPDATE_STATS to client: " + message + " ===");
                    } catch (Exception e) {
                        NetworkLogger.getInstance()
                                .log("=== GameServer: Error sending UPDATE_STATS: " + e.getMessage() + " ===");
                    }
                }
            }
        }).start();

        logWindow.addLog("Updated stats for Player_" + playerInfo.playerId + " - Skill:" + playerInfo.skill
                + " Education:" + playerInfo.education + " Health:" + playerInfo.health + " Money:" + playerInfo.money);
    }

    public void start() {
        NetworkLogger.getInstance().log("=== GameServer: start() called ===");
        running = true;
        try {
            serverSocket = new ServerSocket(GameConfig.SERVER_PORT);
            logWindow.addLog("Server started on port " + GameConfig.SERVER_PORT);
            NetworkLogger.getInstance().log("=== GameServer: Server started, waiting for clients ===");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                NetworkLogger.getInstance()
                        .log("=== GameServer: Client connected: " + clientSocket.getInetAddress() + " ===");
                logWindow.addLog("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                NetworkLogger.getInstance().log("=== GameServer: Starting handler thread ===");

                Thread handlerThread = new Thread(handler, "Handler-" + clientSocket.getInetAddress());
                handlerThread.setDaemon(true);
                handlerThread.start();
                NetworkLogger.getInstance().log("=== GameServer: Handler thread started, returning to accept ===");
            }
        } catch (IOException e) {
            logWindow.addLog("Server error: " + e.getMessage());
            NetworkLogger.getInstance().log("=== GameServer: Error: " + e.getMessage() + " ===");
        }
    }

    public void handleClientJoin(ClientHandler client) {
        NetworkLogger.getInstance().log("=== GameServer.handleClientJoin called ===");

        if (gameStarted) {
            NetworkLogger.getInstance().log("=== GameServer: Game already started, rejecting client ===");
            client.sendText("GAME_STARTED");
            return;
        }

        int connectedCount = 0;
        for (PlayerInfo p : players) {
            if (p.isConnected)
                connectedCount++;
        }
        NetworkLogger.getInstance().log("=== GameServer: Current connected players: " + connectedCount + " ===");

        if (connectedCount < GameConfig.MAX_PLAYERS) {
            int playerId = getNextAvailablePlayerId();
            NetworkLogger.getInstance().log("=== GameServer: Got playerId = " + playerId + " ===");
            if (playerId > 0) {
                client.setPlayerId(playerId);
                players.get(playerId - 1).isConnected = true;
                logWindow.addLog("Player " + playerId + " joined");

                int newConnectedCount = 0;
                for (PlayerInfo p : players) {
                    if (p.isConnected)
                        newConnectedCount++;
                }
                NetworkLogger.getInstance()
                        .log("=== GameServer: Connected count AFTER update: " + newConnectedCount + " ===");

                List<PlayerInfo> lobbyData = new ArrayList<>(players);
                NetworkLogger.getInstance()
                        .log("=== GameServer: Preparing lobby update with " + lobbyData.size() + " players ===");
                for (PlayerInfo p : lobbyData) {
                    NetworkLogger.getInstance().log("  Player " + p.playerId + ": " + p.playerName + " - "
                            + (p.isConnected ? "CONNECTED" : "DISCONNECTED"));
                }

                GameMessage message = new GameMessage(GameMessage.MessageType.UPDATE_LOBBY, lobbyData, -1);

                NetworkLogger.getInstance().log("=== GameServer: Broadcasting to all clients ===");
                broadcastToAll(message);
                logWindow.addLog("Broadcasted lobby update to all clients");
                NetworkLogger.getInstance().log("=== GameServer: Message sent successfully ===");

                client.sendText("PLAYER_ID:" + playerId);
                NetworkLogger.getInstance().log("=== GameServer: Sent player ID " + playerId + " to client ===");

                final int checkCount = newConnectedCount;
                NetworkLogger.getInstance().log("checkCount " + checkCount);
                if (checkCount >= GameConfig.MIN_PLAYERS_TO_START) {
                    NetworkLogger.getInstance().log(
                            "=== GameServer: Player(s) joined (" + checkCount + "), will start game in 3 seconds ===");
                    gameStarted = true;

                    final List<ClientHandler> clientsToNotify = new ArrayList<>(clients);
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            NetworkLogger.getInstance().log("=== GameServer: Sending START_GAME message to "
                                    + clientsToNotify.size() + " client(s) ===");
                            for (ClientHandler c : clientsToNotify) {
                                c.sendText("START_GAME");
                            }
                            logWindow.addLog("Game started! Broadcasting to " + clientsToNotify.size() + " client(s)");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            NetworkLogger.getInstance().log("=== GameServer: Thread interrupted ===");
                        }
                    }).start();
                }
            }
        } else {
            NetworkLogger.getInstance().log("=== GameServer: Max players reached (" + connectedCount + "/"
                    + GameConfig.MAX_PLAYERS + "), cannot join ===");
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
        NetworkLogger.getInstance().log("=== GameServer: Game reset ===");
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
            NetworkLogger.getInstance().log("=== ClientHandler.run() STARTED ===");

            try {
                NetworkLogger.getInstance().log("=== ClientHandler: Creating streams ===");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                NetworkLogger.getInstance().log("=== ClientHandler: Streams created successfully ===");

                NetworkLogger.getInstance().log("=== ClientHandler: Calling handleClientJoin ===");
                server.handleClientJoin(this);
                NetworkLogger.getInstance().log("=== ClientHandler: handleClientJoin completed ===");

                NetworkLogger.getInstance().log("=== ClientHandler: Starting message loop ===");
                while (connected && !socket.isClosed()) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            NetworkLogger.getInstance().log("=== ClientHandler: Client disconnected ===");
                            break;
                        }
                        NetworkLogger.getInstance().log("=== ClientHandler: Received: " + line + " ===");

                        if (line != null && line.startsWith("PLAYER_MOVE:")) {

                            NetworkLogger.getInstance()
                                    .log("=== ClientHandler: Broadcasting player move to other clients ===");
                            for (ClientHandler client : server.clients) {
                                if (client != this) {
                                    client.sendText(line);
                                }
                            }
                        } else if (line != null && line.startsWith("PLAYER_HOVER:")) {
                            NetworkLogger.getInstance()
                                    .log("=== ClientHandler: Broadcasting player hover to other clients ===");
                            for (ClientHandler client : server.clients) {
                                if (client != this) {
                                    client.sendText(line);
                                }
                            }
                        } else if (line != null && line.startsWith("SYNC_PLAYER:")) {
                            NetworkLogger.getInstance()
                                    .log("=== ClientHandler: Broadcasting player sync to all clients ===");
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
                                NetworkLogger.getInstance()
                                        .log("=== ClientHandler: Broadcasting turn update: " + turnUpdate + " ===");
                                for (ClientHandler client : server.clients) {
                                    client.sendText(turnUpdate);
                                }
                            }
                        } else if ("RESET_GAME".equals(line)) {
                            NetworkLogger.getInstance().log("=== ClientHandler: RESET_GAME received ===");
                            server.resetGame();
                        }

                    } catch (IOException e) {
                        if (connected) {
                            NetworkLogger.getInstance().log("=== ClientHandler: IO error: " + e.getMessage() + " ===");
                        }
                        break;
                    }
                }
                NetworkLogger.getInstance().log("=== ClientHandler: Message loop ended ===");
            } catch (IOException e) {
                NetworkLogger.getInstance().log("=== ClientHandler: IOException: " + e.getMessage() + " ===");
                server.logWindow.addLog("Client error: " + e.getMessage());
            } finally {
                NetworkLogger.getInstance().log("=== ClientHandler: Cleaning up ===");
                connected = false;

                int disconnectedPlayerId = getPlayerId();
                if (disconnectedPlayerId > 0) {
                    NetworkLogger.getInstance().log(
                            "=== ClientHandler: Broadcasting disconnect for player " + disconnectedPlayerId + " ===");
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
                        NetworkLogger.getInstance().log("=== ClientHandler: Socket closed ===");
                    }
                } catch (IOException e) {
                    NetworkLogger.getInstance()
                            .log("=== ClientHandler: Error closing socket: " + e.getMessage() + " ===");
                }
                NetworkLogger.getInstance().log("=== ClientHandler.run() ENDED ===");
            }
        }

        @SuppressWarnings("unchecked")
        public void sendMessage(GameMessage message) {
            if (out != null) {
                try {
                    NetworkLogger.getInstance().log("=== ClientHandler: Sending LOBBY_UPDATE ===");
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
                        NetworkLogger.getInstance()
                                .log("=== ClientHandler: Sent player count: " + connectedCount + " ===");
                    }
                } catch (Exception e) {
                    server.logWindow.addLog("Error sending message: " + e.getMessage());
                    NetworkLogger.getInstance().log("=== ClientHandler: Error sending: " + e.getMessage() + " ===");
                }
            } else {
                NetworkLogger.getInstance().log("=== ClientHandler: ERROR - out is null! ===");
            }
        }

        public synchronized void sendText(String message) {
            if (out != null && connected) {
                try {
                    NetworkLogger.getInstance().log("=== ClientHandler: Sending text: " + message + " ===");
                    out.println(message);
                    out.flush();
                    NetworkLogger.getInstance().log("=== ClientHandler: Text sent successfully ===");
                } catch (Exception e) {
                    server.logWindow.addLog("Error sending text: " + e.getMessage());
                    NetworkLogger.getInstance()
                            .log("=== ClientHandler: Error sending text: " + e.getMessage() + " ===");
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
