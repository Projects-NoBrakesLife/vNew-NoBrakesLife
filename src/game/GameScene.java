package game;

import network.NetworkManager;
import javax.swing.SwingUtilities;

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
    private boolean isOnlineMode;
    private NetworkManager networkManager;
    private ArrayList<MenuElement> hudElements;
    private ArrayList<MenuElement> ambientElements;
    private java.util.HashMap<Integer, Integer> remotePlayerHoverIndexes;
    private MenuElement playerIconElement;

    private int currentTurnPlayerId = 1;
    private int currentTurnNumber = 1;
    private String turnDisplayText = "";
    private float turnDisplayAlpha = 0.0f;
    private long turnDisplayStartTime = 0;
    private static final long TURN_DISPLAY_DURATION = 4000;
    private static final long TURN_DISPLAY_FADE_IN = 500;
    private static final long TURN_DISPLAY_FADE_OUT = 500;
    private boolean waitingForTurnToComplete = false;
    private long lastTimeDecrease = 0;
    private long lastAmbientUpdateMs = 0;
    private PopupWindow activePopup;
    private boolean gameEnded = false;
    private int totalInterestEarned = 0;
    private int totalBankWithdrawReturned = 0;
    private boolean isFishing = false;

    private java.util.ArrayList<Integer> askedQuestions = new java.util.ArrayList<>();
    private GameConfig.Question currentQuestion;

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
        networkManager = NetworkManager.getInstance();
        hudElements = new ArrayList<>();
        ambientElements = new ArrayList<>();
        remotePlayerHoverIndexes = new java.util.HashMap<>();
        loadGameHUD();
        loadAmbientObjects();

        if (isOnlineMode) {
            int localPlayerId = networkManager.getPlayerId();
            int connectedCount = network.NetworkManager.getInstance().getPlayers().size();

            double[][] positions = {
                    { 970.0, 425.0 },
                    { 1010.0, 425.0 },
                    { 970.0, 450.0 },
                    { 1010.0, 450.0 }
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
        MenuElement hudImg = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "hud" + File.separator + "hud.png", -48.0, 736.0,
                512.4, 325.7); // Y + 20
        hudElements.add(hudImg);

        playerIconElement = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "hud" + File.separator + "1.png", -80.0, 693.0,
                349.4, 349.4);
        hudElements.add(playerIconElement);

        Player localPlayer = getLocalPlayer();

        MenuElement text1 = new MenuElement("ทักษะ: " + (localPlayer != null ? localPlayer.getSkill() : 0), 230.0,
                853.0, 15); // Y + 20
        text1.setTextColor(new Color(30, 30, 40));
        hudElements.add(text1);

        MenuElement text2 = new MenuElement("สุขภาพ: " + (localPlayer != null ? localPlayer.getHealth() : 100), 230.0,
                907.0, 15); // Y + 20
        text2.setTextColor(new Color(30, 30, 40));
        hudElements.add(text2);

        MenuElement text3 = new MenuElement("ความรู้: " + (localPlayer != null ? localPlayer.getEducation() : 0), 230.0,
                961.0, 15); // Y + 20
        text3.setTextColor(new Color(30, 30, 40));
        hudElements.add(text3);

        MenuElement text4 = new MenuElement("เงิน: " + (localPlayer != null ? localPlayer.getMoney() : 500), 203.0,
                1020.0, 15); // Y + 20
        text4.setTextColor(new Color(30, 30, 40));
        hudElements.add(text4);

        MenuElement imgtimer = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "hud" + File.separator + "HUD info header.png",
                730.3, -9.0, 459.4, 62.6);
        hudElements.add(imgtimer);
        MenuElement texttimer = new MenuElement("P1 เหลือเวลา 10/24 ชม", 794.5, 31.0, 32);
        texttimer.setTextColor(new Color(30, 30, 40));
        hudElements.add(texttimer);
    }

    private void loadAmbientObjects() {
        try {

            MenuElement policeCar = new MenuElement(MenuElement.ElementType.IMAGE,
                    "assets" + java.io.File.separator + "ui" + java.io.File.separator + "Car-Police-Back.png",
                    1592.0, 620.0, 85.2, 75.2);
            double pStartX = 1592.0;
            double pStartY = 620.0;
            double pEndX = 1277.0;
            double pEndY = 468.0;
            double pSpeed = 90.0;
            policeCar.enablePatrol(pStartX, pStartY, pEndX, pEndY, pSpeed);
            ambientElements.add(policeCar);
        } catch (Exception ignored) {
        }
    }

    private Player getLocalPlayer() {
        if (players.isEmpty())
            return null;

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
        boolean canInteract = !isTurnOverlayActive();
        boolean anyHovering = false;
        String hoverName = null;
        int hoverIndex = -1;

        Player localPlayerForTurn = null;
        if (isOnlineMode) {
            int localPlayerIndex = networkManager.getPlayerId() - 1;
            if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
                localPlayerForTurn = players.get(localPlayerIndex);
            }
        } else if (!players.isEmpty()) {
            localPlayerForTurn = players.get(0);
        }

        long currentTime = System.currentTimeMillis();
        if (!gameEnded && isMyTurn() && localPlayerForTurn != null && localPlayerForTurn.hasTimeRemaining()) {
            if (lastTimeDecrease == 0
                    || (currentTime - lastTimeDecrease) >= GameConfig.TIME_AUTO_DECREASE_INTERVAL_MS) {
                double oldTime = localPlayerForTurn.getRemainingTime();
                double newTime = Math.max(0.0, oldTime - GameConfig.TIME_AUTO_DECREASE);
                localPlayerForTurn.setRemainingTime(newTime);
                lastTimeDecrease = currentTime;

                if (oldTime > 0.0 && newTime <= 0.0) {
                    closeActivePopup();
                    nextTurn();
                }
            }
        }

        if (isMyTurn() && canInteract) {
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
        }

        for (java.util.Map.Entry<Integer, Integer> entry : remotePlayerHoverIndexes.entrySet()) {
            int remotePlayerId = entry.getKey();
            int remoteHoverIndex = entry.getValue();

            if (remoteHoverIndex >= 0 && remoteHoverIndex < hoverObjects.size()) {
                GameObject hoverObj = hoverObjects.get(remoteHoverIndex);
                if (remotePlayerId == currentTurnPlayerId) {
                    hoverObj.render(g2d);
                }
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
                System.out.println("=== Turn completed (action finished, time remaining: "
                        + localPlayerForTurn.getRemainingTime() + " hours) ===");
            }
        }

        if (isOnlineMode && networkManager.isConnected()) {
            int localPlayerIndex = networkManager.getPlayerId() - 1;
            if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
                Player localPlayer = players.get(localPlayerIndex);
                long networkUpdateTime = System.currentTimeMillis();
                if (!networkManager.hasLastUpdate() || (networkUpdateTime - networkManager.getLastUpdateTime()) > 50) {
                    networkManager.sendPlayerMove(localPlayer);
                    networkManager.setLastUpdateTime(networkUpdateTime);
                }
            }
        }

        if (anyHovering && hoverIndex != currentHoverIndex) {
            SoundManager.getInstance().playSFX(GameConfig.HOVER_SOUND);
            currentHoverIndex = hoverIndex;
        } else if (!anyHovering) {
            currentHoverIndex = -1;
        }

        if (isOnlineMode && networkManager.isConnected() && isMyTurn() && canInteract) {
            int localPlayerId = networkManager.getPlayerId();
            if (anyHovering && hoverIndex != -1) {
                networkManager.sendPlayerHover(localPlayerId, hoverIndex);
            } else if (!anyHovering) {
                networkManager.sendPlayerHover(localPlayerId, -1);
            }
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

        if (playerIconElement != null) {
            String currentIconPath = playerIconElement.getImagePath();
            String newIconPath = "assets" + File.separator + "ui" + File.separator + "hud" + File.separator
                    + currentTurnPlayerId + ".png";
            if (!newIconPath.equals(currentIconPath)) {
                playerIconElement.setImagePath(newIconPath);
            }
        }

        for (int i = 0; i < hudElements.size(); i++) {
            MenuElement element = hudElements.get(i);

            if (i >= 2 && i <= 5 && element.getType() == MenuElement.ElementType.TEXT) {
                String newText = "";

                switch (i) {
                    case 2:
                        newText = "ทักษะ: " + (currentTurnPlayer != null ? currentTurnPlayer.getSkill() : "...");
                        break;
                    case 3:
                        newText = "สุขภาพ: " + (currentTurnPlayer != null ? currentTurnPlayer.getHealth() : "...");
                        break;
                    case 4:
                        newText = "ความรู้: " + (currentTurnPlayer != null ? currentTurnPlayer.getEducation() : "...");
                        break;
                    case 5:
                        newText = "เงิน: " + (currentTurnPlayer != null ? currentTurnPlayer.getMoney() : "...");
                        break;
                }

                element.setText(newText);
            } else if (i == 7 && element.getType() == MenuElement.ElementType.TEXT) {
                if (currentTurnPlayer != null) {
                    double remaining = currentTurnPlayer.getRemainingTime();
                    int remainingInt = (int) Math.ceil(remaining);
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

        long nowMs = System.currentTimeMillis();
        if (ambientElements != null && !ambientElements.isEmpty()) {
            if (lastAmbientUpdateMs == 0) {
                lastAmbientUpdateMs = nowMs;
            }
            long delta = Math.max(0, nowMs - lastAmbientUpdateMs);
            for (MenuElement el : ambientElements) {
                el.update(delta);
            }
            lastAmbientUpdateMs = nowMs;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - turnDisplayStartTime;
        long totalDuration = TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT;

        for (MenuElement el : ambientElements) {
            el.render(g2d);
        }

        if (elapsed > totalDuration) {
            turnDisplayAlpha = 0.0f;
            return;
        }

        if (elapsed < TURN_DISPLAY_FADE_IN) {
            turnDisplayAlpha = (float) elapsed / TURN_DISPLAY_FADE_IN;
        } else if (elapsed > TURN_DISPLAY_DURATION) {
            float fadeOutProgress = (float) (elapsed - TURN_DISPLAY_DURATION) / TURN_DISPLAY_FADE_OUT;
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

            Color textColor = new Color(255, 255, 255, (int) (255 * turnDisplayAlpha));
            g2d.setColor(textColor);

            int textX = centerX - textWidth / 2;
            int textY = centerY + textHeight / 4;

            g2d.drawString(turnDisplayText, textX, textY);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.setFont(originalFont);
        }
    }

    private boolean isTurnOverlayActive() {
        if (turnDisplayStartTime == 0 || turnDisplayText == null || turnDisplayText.isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        long elapsed = now - turnDisplayStartTime;
        long total = TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT;
        return elapsed <= total;
    }

    private void showGameStartDisplay() {
        turnDisplayText = "เริ่มต้นการใช้ชีวิต";
        turnDisplayStartTime = System.currentTimeMillis();
        turnDisplayAlpha = 0.0f;

        SoundManager.getInstance().playSFX(GameConfig.TURN_SOUND);

        javax.swing.Timer timer = new javax.swing.Timer((int) (TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT), _ -> {
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
        if (remainingWeeks < 0)
            remainingWeeks = 0;
        turnDisplayText = "ผ่านไปแล้ว " + (currentTurnNumber - 1) + " สัปดาห์เหลืออีก " + remainingWeeks + "/"
                + GameConfig.MAX_TURNS;
        turnDisplayStartTime = System.currentTimeMillis();
        turnDisplayAlpha = 0.0f;

        SoundManager.getInstance().playSFX(GameConfig.TURN_SOUND);

        javax.swing.Timer timer = new javax.swing.Timer((int) (TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT), _ -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                showTurnDisplay();
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showGameEndDisplayAndSummary() {
        closeActivePopup();
        if (gameEnded)
            return;
        withdrawAllDepositsAllPlayers();
        gameEnded = true;
        String endMsg = "การใช้ชีวิตจบลงแล้ว";
        if (totalBankWithdrawReturned > 0 || totalInterestEarned > 0) {
            endMsg += "คืนเงินฝาก $" + totalBankWithdrawReturned + " + ปันผล $" + totalInterestEarned;
        }
        turnDisplayText = endMsg;
        turnDisplayStartTime = System.currentTimeMillis();
        turnDisplayAlpha = 0.0f;
        try {
            SoundManager.getInstance().playSFX(GameConfig.LAST_TURN_SOUND);
        } catch (Exception ignored) {
        }

        if (isOnlineMode) {
            syncAllPlayersBeforeSummary();
        }

        javax.swing.Timer timer = new javax.swing.Timer((int) (TURN_DISPLAY_DURATION + TURN_DISPLAY_FADE_OUT), _ -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                GameSummaryWindow summary = new GameSummaryWindow(players);
                summary.setVisible(true);
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void syncAllPlayersBeforeSummary() {
        if (isOnlineMode && networkManager != null) {
            for (Player p : players) {
                if (!p.isRemotePlayer()) {
                    String syncMsg = "SYNC_PLAYER:" + p.getPlayerId() + ":" + p.getSkill() + ":" +
                            p.getEducation() + ":" + p.getHealth() + ":" + p.getMoney();
                    networkManager.sendMessage(syncMsg);
                }
            }
        }
    }

    public boolean isMyTurn() {
        if (isOnlineMode) {
            return networkManager.getPlayerId() == currentTurnPlayerId;
        }
        return currentTurnPlayerId == 1;
    }

    public void setRemotePlayerHover(int playerId, int hoverIndex) {
        if (hoverIndex >= -1) {
            if (hoverIndex == -1) {
                remotePlayerHoverIndexes.remove(playerId);
            } else {
                remotePlayerHoverIndexes.put(playerId, hoverIndex);
            }
        }
    }

    public void setTurn(int turnPlayerId, int turnNumber, String updateType) {
        closeActivePopup();

        currentHoverIndex = -1;
        remotePlayerHoverIndexes.clear();
        currentTurnPlayerId = turnPlayerId;
        currentTurnNumber = turnNumber;
        if (currentTurnNumber > GameConfig.MAX_TURNS || gameEnded) {
            showGameEndDisplayAndSummary();
            return;
        }

        for (Player player : players) {
            if (player.getPlayerId() == turnPlayerId) {
                double startTime = 24.0;

                player.setHealth(player.getHealth() - GameConfig.TURN_HEALTH_PENALTY);

                if (isOnlineMode && networkManager != null && player.getPlayerId() == networkManager.getPlayerId()) {
                    networkManager.sendPlayerStats(
                            player.getPlayerId(),
                            player.getSkill(),
                            player.getEducation(),
                            player.getHealth(),
                            player.getMoney(),
                            player.getBankDeposit());
                }

                if (player.getHealth() < GameConfig.LOW_HEALTH_THRESHOLD * 100) {
                    startTime = 24.0 - GameConfig.LOW_HEALTH_TIME_PENALTY;
                    turnDisplayText = "สุขภาพต่ำ! เวลาเริ่มต้น " + (int) startTime + " ชม.";
                    turnDisplayStartTime = System.currentTimeMillis();
                }

                player.setRemainingTime(startTime);

                updateHUDStats();
            }
        }

        if (turnNumber == 1 && turnPlayerId == 1 && turnDisplayStartTime == 0) {
            showGameStartDisplay();
        } else if ("WEEK".equals(updateType)) {
            if (currentTurnNumber >= GameConfig.MAX_TURNS) {
                showGameEndDisplayAndSummary();
                return;
            }
            showWeekDisplay();
        } else {
            showTurnDisplay();
        }
    }

    public void nextTurn() {
        warpCurrentTurnPlayerToDorm();
        if (isOnlineMode && networkManager.isConnected()) {
            networkManager.sendMessage("TURN_COMPLETE:" + currentTurnPlayerId + ":" + currentTurnNumber);
        } else {
            int connectedCount = 1;
            if (connectedCount == 0)
                return;

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
                    showGameEndDisplayAndSummary();
                    return;
                }

                if (wasLastPlayer && currentTurnNumber <= GameConfig.MAX_TURNS) {
                    applyWeeklyInterest();
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

    private void warpCurrentTurnPlayerToDorm() {
        Player p = null;
        if (isOnlineMode) {
            if (currentTurnPlayerId > 0 && currentTurnPlayerId <= players.size()) {
                p = players.get(currentTurnPlayerId - 1);
            }
        } else if (!players.isEmpty()) {
            p = players.get(0);
        }
        if (p == null)
            return;
        int dormIndex = -1;
        for (int i = 0; i < GameConfig.HOVER_OBJECTS.length; i++) {
            if ("หอพัก".equals(GameConfig.HOVER_OBJECTS[i].name)) {
                dormIndex = i;
                break;
            }
        }
        if (dormIndex >= 0) {
            GameConfig.HoverObject dorm = GameConfig.HOVER_OBJECTS[dormIndex];
            p.setDirection(dorm.direction);
            p.setX(dorm.playerX);
            p.setY(dorm.playerY);
            p.setMoving(false);
            if (isOnlineMode && networkManager.isConnected()) {
                networkManager.sendPlayerMove(p);
                networkManager.setLastUpdateTime(System.currentTimeMillis());
            }
        }
    }

    private void renderHoverUI(Graphics2D g2d, String name) {
        BufferedImage uiField = loadUI();
        if (uiField == null)
            return;

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
            String path = "assets" + java.io.File.separator + "ui" + java.io.File.separator
                    + "Input-Field-Small-White_0.png";
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
        if (isTurnOverlayActive()) {
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
                            System.out
                                    .println("=== Player " + localPlayer.getPlayerId() + " has no time remaining ===");
                            return;
                        }

                        if (localPlayer.isMoving() || localPlayer.isAnimating()) {
                            System.out.println("=== Player " + localPlayer.getPlayerId()
                                    + " is already moving, ignoring click ===");
                            return;
                        }

                        double currentX = localPlayer.getX();
                        double currentY = localPlayer.getY();
                        double distance = Math.sqrt((targetX - currentX) * (targetX - currentX)
                                + (targetY - currentY) * (targetY - currentY));

                        if (distance < 10.0) {
                            System.out.println("=== Player " + localPlayer.getPlayerId()
                                    + " clicked same location (distance: " + distance + ") ===");
                            game.ConfigPopup.PopupType popupType = game.ConfigPopup.resolveTypeByName(config.name);
                            if (popupType != null) {
                                closeActivePopup();
                                if (popupType == game.ConfigPopup.PopupType.GARDEN) {
                                    isFishing = false;
                                }
                                if (popupType == game.ConfigPopup.PopupType.UNIVERSITY && currentQuestion == null) {
                                    currentQuestion = getRandomQuestion();
                                }
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    game.PopupWindow.PopupWindowConfig cfg = game.ConfigPopup.createConfig(popupType);
                                    java.util.ArrayList<game.MenuElement> popupElements = game.ConfigPopup
                                            .createElements(popupType);

                                    game.PopupWindow.QuestionHandler qHandler = (popupType == game.ConfigPopup.PopupType.UNIVERSITY)
                                            ? answer -> {
                                                Player p = null;
                                                if (isOnlineMode) {
                                                    if (currentTurnPlayerId > 0
                                                            && currentTurnPlayerId <= players.size()) {
                                                        p = players.get(currentTurnPlayerId - 1);
                                                    }
                                                } else if (!players.isEmpty()) {
                                                    p = players.get(0);
                                                }
                                                if (p != null) {
                                                    handleStudyAnswer(answer, p);
                                                }
                                            }
                                            : null;

                                    activePopup = new game.PopupWindow(cfg, popupElements, productId -> {
                                        Player p = null;
                                        if (isOnlineMode) {
                                            if (currentTurnPlayerId > 0 && currentTurnPlayerId <= players.size()) {
                                                p = players.get(currentTurnPlayerId - 1);
                                            }
                                        } else if (!players.isEmpty()) {
                                            p = players.get(0);
                                        }
                                        if (p == null)
                                            return;
                                        if ("fries".equals(productId) || "burger".equals(productId)
                                                || "shake".equals(productId) || "bucket".equals(productId)) {
                                            int price = 0;
                                            int healthAdd = 0;
                                            if ("fries".equals(productId)) {
                                                price = 30;
                                                healthAdd = 40;
                                            } else if ("burger".equals(productId)) {
                                                price = 30;
                                                healthAdd = 50;
                                            } else if ("shake".equals(productId)) {
                                                price = 20;
                                                healthAdd = 30;
                                            } else if ("bucket".equals(productId)) {
                                                price = 20;
                                                healthAdd = 40;
                                            }
                                            if (price <= 0 || healthAdd == 0)
                                                return;
                                            if (p.getMoney() < price) {
                                                if (activePopup != null) {
                                                    String msg = "เงินไม่เพียงพอ ต้องการ $" + price + " (มี $"
                                                            + p.getMoney() + ")";
                                                    PopupWindow ap = activePopup;
                                                    SwingUtilities.invokeLater(() -> ap.showNotification(msg));
                                                }
                                                return;
                                            }
                                            p.setMoney(p.getMoney() - price);
                                            p.setHealth(p.getHealth() + healthAdd);
                                            if (activePopup != null) {
                                                int nowH = p.getHealth();
                                                String msg = "จ่าย $" + price + " ได้รับสุขภาพ +" + healthAdd
                                                        + " ตอนนี้มี " + nowH;
                                                PopupWindow ap = activePopup;
                                                SwingUtilities.invokeLater(() -> ap.showNotification(msg));
                                            }
                                            SoundManager.getInstance().playSFX(GameConfig.FOOD_EATEN_SOUND);
                                            if (isOnlineMode && networkManager != null) {
                                                networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(),
                                                        p.getEducation(),
                                                        p.getHealth(), p.getMoney(), p.getBankDeposit());
                                            }
                                            SwingUtilities.invokeLater(this::updateHUDStats);
                                        } else if ("fishing".equals(productId)) {
                                            handleFishing(p);
                                        } else if ("sleep".equals(productId)) {
                                            handleSleep(p);
                                        } else if ("gym_tophand".equals(productId)) {
                                            handleGym(p, "tophand");
                                        } else if ("gym_icon".equals(productId)) {
                                            handleGym(p, "icon");
                                        } else if ("deposit500".equals(productId) || "depositAll".equals(productId)
                                                || "withdraw500".equals(productId) || "withdrawAll".equals(productId)) {
                                            int amount = 0;
                                            if ("deposit500".equals(productId))
                                                amount = 100;
                                            if ("withdraw500".equals(productId))
                                                amount = 100;
                                            if ("depositAll".equals(productId))
                                                amount = p.getMoney();
                                            if ("withdrawAll".equals(productId))
                                                amount = p.getBankDeposit();
                                            if (productId.startsWith("deposit")) {
                                                if (amount <= 0 || p.getMoney() < amount) {
                                                    if (activePopup != null) {
                                                        String msg = "เงินไม่เพียงพอ ฝาก $" + amount + " (มี $"
                                                                + p.getMoney() + ")";
                                                        PopupWindow ap = activePopup;
                                                        SwingUtilities.invokeLater(() -> ap.showNotification(msg));
                                                    }
                                                    return;
                                                }
                                                p.setMoney(p.getMoney() - amount);
                                                p.increaseBankDeposit(amount);
                                                if (activePopup != null) {
                                                    String msg = "ฝากเงิน $" + amount + " สำเร็จ ฝากสะสม $"
                                                            + p.getBankDeposit();
                                                    PopupWindow ap = activePopup;
                                                    SwingUtilities.invokeLater(() -> ap.showNotification(msg));
                                                }
                                                if (isOnlineMode && networkManager != null) {
                                                    networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(),
                                                            p.getEducation(),
                                                            p.getHealth(), p.getMoney(), p.getBankDeposit());
                                                }
                                            } else if (productId.startsWith("withdraw")) {
                                                if (amount <= 0 || p.getBankDeposit() < amount) {
                                                    if (activePopup != null) {
                                                        String msg = "ยอดฝากไม่พอ ถอน $" + amount + " (ฝาก $"
                                                                + p.getBankDeposit() + ")";
                                                        PopupWindow ap = activePopup;
                                                        SwingUtilities.invokeLater(() -> ap.showNotification(msg));
                                                    }
                                                    return;
                                                }
                                                p.increaseBankDeposit(-amount);
                                                p.setMoney(p.getMoney() + amount);
                                                if (activePopup != null) {
                                                    String msg = "ถอนเงิน $" + amount + " สำเร็จ คงเหลือฝาก $"
                                                            + p.getBankDeposit();
                                                    PopupWindow ap = activePopup;
                                                    SwingUtilities.invokeLater(() -> ap.showNotification(msg));
                                                }
                                                if (isOnlineMode && networkManager != null) {
                                                    networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(),
                                                            p.getEducation(),
                                                            p.getHealth(), p.getMoney(), p.getBankDeposit());
                                                }
                                            }
                                            SwingUtilities.invokeLater(this::updateHUDStats);
                                        }
                                    }, qHandler);

                                    if (popupType == game.ConfigPopup.PopupType.UNIVERSITY && currentQuestion != null) {
                                        activePopup.setQuestion(currentQuestion.question);
                                    }

                                    activePopup.setVisible(true);
                                });
                            }
                            return;
                        }

                        localPlayer.setDirection(config.direction);
                        localPlayer.setDestination(targetX, targetY);

                        if (isOnlineMode && networkManager.isConnected()) {
                            networkManager.sendPlayerMove(localPlayer);
                            networkManager.setLastUpdateTime(System.currentTimeMillis());
                        }

                        waitingForTurnToComplete = true;

                        System.out
                                .println("=== Turn started: Player " + (isOnlineMode ? networkManager.getPlayerId() : 1)
                                        + " clicked, remaining time: " + localPlayer.getRemainingTime() + " hours ===");
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

    public void updateRemotePlayer(int playerId, double x, double y, String direction, boolean isMoving,
            double remainingTime) {
        updateRemotePlayer(playerId, x, y, direction, isMoving, remainingTime, x, y);
    }

    public void updateRemotePlayer(int playerId, double x, double y, String direction, boolean isMoving,
            double remainingTime, double destX, double destY) {
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
            boolean wasAnimating = remotePlayer.isAnimating();

            if (isMoving && !wasMoving && !wasAnimating) {
                remotePlayer.setDirection(direction);
                remotePlayer.setDestination(destX, destY, false);
            } else if (!isMoving && wasMoving) {
                if (remotePlayer.isAnimating()) {
                    remotePlayer.forceUpdateAnimation();
                } else {
                    remotePlayer.setX(x);
                    remotePlayer.setY(y);
                    remotePlayer.setMoving(false);
                }
            } else if (isMoving && wasMoving && remotePlayer.isAnimating()) {
                remotePlayer.setDirection(direction);
                remotePlayer.forceUpdateAnimation();
            } else if (isMoving && wasMoving && !remotePlayer.isAnimating()) {
                remotePlayer.setDirection(direction);
                remotePlayer.setDestination(destX, destY, false);
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

    public synchronized void updatePlayerStats(int playerId, int skill, int education, int health, int money) {
        int playerIndex = playerId - 1;
        if (playerIndex >= 0 && playerIndex < players.size()) {
            Player player = players.get(playerIndex);
            player.setSkill(skill);
            player.setEducation(education);
            player.setHealth(health);
            player.setMoney(money);
            System.out.println("=== GameScene: Updated stats for Player " + playerId + " - Skill:" + skill
                    + " Education:" + education + " Health:" + health + " Money:" + money + " ===");

            SwingUtilities.invokeLater(() -> {
                updateHUDStats();
            });
        }
    }

    private void updateHUDStats() {
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

            if (i >= 2 && i <= 5 && element.getType() == MenuElement.ElementType.TEXT) {
                String newText = "";

                switch (i) {
                    case 2:
                        newText = "ทักษะ: " + (currentTurnPlayer != null ? currentTurnPlayer.getSkill() : 0);
                        break;
                    case 3:
                        newText = "สุขภาพ: " + (currentTurnPlayer != null ? currentTurnPlayer.getHealth() : 100);
                        break;
                    case 4:
                        newText = "ความรู้: " + (currentTurnPlayer != null ? currentTurnPlayer.getEducation() : 0);
                        break;
                    case 5:
                        newText = "เงิน: " + (currentTurnPlayer != null ? currentTurnPlayer.getMoney() : 500);
                        break;
                }

                element.setText(newText);
            }
        }
    }

    public boolean hasActivePopup() {
        return activePopup != null && activePopup.isShowing();
    }

    private void closeActivePopup() {
        try {
            if (activePopup != null && activePopup.isShowing()) {
                if (activePopup instanceof PopupWindow) {
                    ((PopupWindow) activePopup).stopFishingAnimation();
                }
                activePopup.dispose();
            }
        } catch (Exception ignored) {
        } finally {
            activePopup = null;
            isFishing = false;
        }
    }

    private void applyWeeklyInterest() {
        for (Player p : players) {
            int deposit = p.getBankDeposit();
            if (deposit > 0) {
                int bonus = (int) Math.floor(deposit * 0.10);
                p.setMoney(p.getMoney() + bonus);
                totalInterestEarned += bonus;
            }
        }
    }

    private void withdrawAllDepositsAllPlayers() {
        int returned = 0;
        for (Player p : players) {
            int deposit = p.getBankDeposit();
            if (deposit > 0) {
                p.setMoney(p.getMoney() + deposit);
                p.setBankDeposit(0);
                returned += deposit;
            }
        }
        totalBankWithdrawReturned += returned;
    }

    private void handleFishing(Player p) {
        if (p == null || activePopup == null)
            return;

        if (isFishing) {
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> ap.showNotification("ลองอีกครั้ง..."));
            return;
        }

        double remainingTime = p.getRemainingTime();
        if (remainingTime < GameConfig.FISHING_TIME_COST) {
            String msg = "เวลาไม่พอ ต้องการ " + GameConfig.FISHING_TIME_COST + " ชั่วโมง (เหลือ "
                    + String.format("%.1f", remainingTime) + " ชั่วโมง)";
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> ap.showNotification(msg));
            return;
        }

        isFishing = true;
        p.setRemainingTime(remainingTime - GameConfig.FISHING_TIME_COST);
        SwingUtilities.invokeLater(this::updateHUDStats);

        PopupWindow ap = activePopup;
        SwingUtilities.invokeLater(() -> {
            ap.clearNotifications();
            ap.startFishingAnimation();
            ap.showNotification("กำลังตกปลา...");
        });

        final int[] hookSoundCount = { 0 };
        final int maxHookSounds = 3;
        javax.swing.Timer[] hookTimer = new javax.swing.Timer[1];

        javax.swing.Timer playHookSoundTimer = new javax.swing.Timer(600, _ -> {
            try {
                SoundManager.getInstance().playSFX(GameConfig.HOOK_SOUND);
            } catch (Exception ignored) {
            }

            hookSoundCount[0]++;
            if (hookSoundCount[0] >= maxHookSounds) {
                if (hookTimer[0] != null) {
                    hookTimer[0].stop();
                }

                GameConfig.Fish caughtFish = randomFish();
                if (caughtFish == null) {
                    isFishing = false;
                    SwingUtilities.invokeLater(() -> {
                        if (activePopup != null) {
                            activePopup.stopFishingAnimation();
                        }
                    });
                    return;
                }

                p.setHealth(p.getHealth() - caughtFish.healthBonus);
                p.setMoney(p.getMoney() + caughtFish.moneyBonus);

                int skillGain = GameConfig.FISHING_SKILL_MIN
                        + (int) (Math.random() * (GameConfig.FISHING_SKILL_MAX - GameConfig.FISHING_SKILL_MIN + 1));
                p.setSkill(p.getSkill() + skillGain);

                StringBuilder msgBuilder = new StringBuilder("ตกได้ " + caughtFish.name);
                if (caughtFish.moneyBonus > 0) {
                    msgBuilder.append(" ได้เงิน $").append(caughtFish.moneyBonus);
                }
                if (caughtFish.healthBonus > 0) {
                    msgBuilder.append(" สุขภาพ -").append(caughtFish.healthBonus);
                }
                msgBuilder.append(" ทักษะ +").append(skillGain);
                final String msg = msgBuilder.toString();
                final String fishImagePath = caughtFish.imagePath;

                if (isOnlineMode && networkManager != null) {
                    networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(), p.getEducation(),
                            p.getHealth(), p.getMoney(), p.getBankDeposit());
                }

                SwingUtilities.invokeLater(() -> {
                    if (activePopup != null) {
                        activePopup.stopFishingAnimation();
                        activePopup.clearNotifications();
                        activePopup.showNotification(msg, fishImagePath);
                    }
                    updateHUDStats();
                });

                javax.swing.Timer resetTimer = new javax.swing.Timer(3000, _ -> {
                    isFishing = false;
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        });
        playHookSoundTimer.setRepeats(true);
        playHookSoundTimer.start();
        hookTimer[0] = playHookSoundTimer;
    }

    private void handleSleep(Player p) {
        if (p == null || activePopup == null)
            return;

        double remainingTime = p.getRemainingTime();
        if (remainingTime < GameConfig.SLEEP_TIME_COST) {
            String msg = "เวลาไม่พอ ต้องการ " + GameConfig.SLEEP_TIME_COST + " ชั่วโมง (เหลือ "
                    + String.format("%.1f", remainingTime) + " ชั่วโมง)";
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> ap.showNotification(msg));
            return;
        }

        p.setRemainingTime(remainingTime - GameConfig.SLEEP_TIME_COST);
        p.setHealth(p.getHealth() + GameConfig.SLEEP_HEALTH_BONUS);

        if (isOnlineMode && networkManager != null) {
            networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(), p.getEducation(),
                    p.getHealth(), p.getMoney(), p.getBankDeposit());
        }

        String msg = "นอนพักผ่อน สุขภาพ +" + GameConfig.SLEEP_HEALTH_BONUS;
        PopupWindow ap = activePopup;
        SwingUtilities.invokeLater(() -> {
            ap.clearNotifications();
            ap.showNotification(msg);
            updateHUDStats();
        });

        try {
            SoundManager.getInstance().playSFX(GameConfig.ZZZ_SOUND);
        } catch (Exception ignored) {
        }
    }

    private void handleGym(Player p, String gymType) {
        if (p == null || activePopup == null)
            return;

        int healthBonus;
        double timeCost;
        String workoutName;

        if ("tophand".equals(gymType)) {
            healthBonus = GameConfig.GYM_HEALTH_BONUS_TOPHAND;
            timeCost = GameConfig.GYM_TIME_COST_TOPHAND;
            workoutName = "ออกกำลังกายเต็มที่";
        } else {
            healthBonus = GameConfig.GYM_HEALTH_BONUS_ICON;
            timeCost = GameConfig.GYM_TIME_COST_ICON;
            workoutName = "ออกกำลังกายเบาๆ";
        }

        double remainingTime = p.getRemainingTime();
        if (remainingTime < timeCost) {
            String msg = "เวลาไม่พอ ต้องการ " + (int) timeCost + " ชั่วโมง (เหลือ "
                    + String.format("%.1f", remainingTime) + " ชั่วโมง)";
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> ap.showNotification(msg));
            return;
        }

        p.setRemainingTime(remainingTime - timeCost);
        p.setHealth(p.getHealth() + healthBonus);

        int skillGain = GameConfig.GYM_SKILL_MIN
                + (int) (Math.random() * (GameConfig.GYM_SKILL_MAX - GameConfig.GYM_SKILL_MIN + 1));
        p.setSkill(p.getSkill() + skillGain);

        if (isOnlineMode && networkManager != null) {
            networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(), p.getEducation(),
                    p.getHealth(), p.getMoney(), p.getBankDeposit());
        }

        String msg = workoutName + " สุขภาพ +" + healthBonus + " ทักษะ +" + skillGain;
        PopupWindow ap = activePopup;
        SwingUtilities.invokeLater(() -> {
            ap.clearNotifications();
            ap.showNotification(msg);
            updateHUDStats();
        });

        try {
            SoundManager.getInstance().playSFX(GameConfig.FOOD_EATEN_SOUND);
        } catch (Exception ignored) {
        }
    }

    private GameConfig.Question getRandomQuestion() {
        if (GameConfig.QUESTIONS == null || GameConfig.QUESTIONS.length == 0)
            return null;

        if (askedQuestions.size() >= Math.min(10, GameConfig.QUESTIONS.length)) {
            askedQuestions.clear();
        }

        java.util.ArrayList<Integer> availableIndexes = new java.util.ArrayList<>();
        for (int i = 0; i < GameConfig.QUESTIONS.length; i++) {
            if (!askedQuestions.contains(i)) {
                availableIndexes.add(i);
            }
        }

        if (availableIndexes.isEmpty()) {
            askedQuestions.clear();
            for (int i = 0; i < GameConfig.QUESTIONS.length; i++) {
                availableIndexes.add(i);
            }
        }

        int randomIndex = availableIndexes.get((int) (Math.random() * availableIndexes.size()));
        askedQuestions.add(randomIndex);
        return GameConfig.QUESTIONS[randomIndex];
    }

    private void handleStudyAnswer(String answer, Player p) {
        if (answer == null || answer.trim().isEmpty()) {
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> ap.showNotification("กรุณาใส่คำตอบ"));
            return;
        }

        if (currentQuestion == null) {
            return;
        }

        String trimmedAnswer = answer.trim();
        String correctAnswer = currentQuestion.answer.trim();

        double remainingTime = p.getRemainingTime();
        if (remainingTime < GameConfig.STUDY_TIME_COST) {
            String msg = "เวลาไม่พอ ต้องการ " + GameConfig.STUDY_TIME_COST + " ชั่วโมง (เหลือ "
                    + String.format("%.1f", remainingTime) + " ชั่วโมง)";
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> ap.showNotification(msg));
            return;
        }

        p.setRemainingTime(remainingTime - GameConfig.STUDY_TIME_COST);

        p.setSkill(p.getSkill() + GameConfig.STUDY_SKILL_BONUS);

        if (trimmedAnswer.equalsIgnoreCase(correctAnswer)) {
            p.setEducation(p.getEducation() + GameConfig.STUDY_EDUCATION_BONUS);

            if (isOnlineMode && networkManager != null) {
                networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(), p.getEducation(),
                        p.getHealth(), p.getMoney(), p.getBankDeposit());
            }

            String msg = "ถูกต้อง! การศึกษา +" + GameConfig.STUDY_EDUCATION_BONUS + " ทักษะ +"
                    + GameConfig.STUDY_SKILL_BONUS + " (ใช้เวลา " + GameConfig.STUDY_TIME_COST + " ชม.)";
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> {
                ap.clearNotifications();
                ap.showNotification(msg);
                updateHUDStats();
            });
        } else {
            if (isOnlineMode && networkManager != null) {
                networkManager.sendPlayerStats(p.getPlayerId(), p.getSkill(), p.getEducation(),
                        p.getHealth(), p.getMoney(), p.getBankDeposit());
            }

            String msg = "ตอบผิด! ทักษะ +" + GameConfig.STUDY_SKILL_BONUS + " (ใช้เวลา " + GameConfig.STUDY_TIME_COST
                    + " ชม.)";
            PopupWindow ap = activePopup;
            SwingUtilities.invokeLater(() -> {
                ap.clearNotifications();
                ap.showNotification(msg);
                updateHUDStats();
            });
        }

        currentQuestion = getRandomQuestion();
        if (activePopup != null && currentQuestion != null) {
            activePopup.setQuestion(currentQuestion.question);
        }
    }

    private GameConfig.Fish randomFish() {
        if (GameConfig.FISHES == null || GameConfig.FISHES.length == 0)
            return null;

        double totalWeight = 0.0;
        for (GameConfig.Fish fish : GameConfig.FISHES) {
            totalWeight += fish.weight;
        }

        double random = Math.random() * totalWeight;
        double currentWeight = 0.0;

        for (GameConfig.Fish fish : GameConfig.FISHES) {
            currentWeight += fish.weight;
            if (random <= currentWeight) {
                return fish;
            }
        }

        return GameConfig.FISHES[GameConfig.FISHES.length - 1];
    }

}
