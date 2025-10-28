package game;

import network.NetworkManager;
import network.PlayerInfo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GameLobbyMenu extends JFrame {
    private static final String MENU_BACKGROUND = "assets" + java.io.File.separator + "background"
            + java.io.File.separator + "Score-Screen-Background.png";

    private MenuPanel menuPanel;

    public GameLobbyMenu() {
        initializeWindow();
        createMenuPanel();
        pack();
        centerWindow();
    }

    private void initializeWindow() {
        setTitle("No Brakes Life - Game Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        addDragSupport();
    }

    private void addDragSupport() {
        final int[] point = new int[2];

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                point[0] = e.getXOnScreen();
                point[1] = e.getYOnScreen();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int newX = getX() + e.getXOnScreen() - point[0];
                int newY = getY() + e.getYOnScreen() - point[1];
                point[0] = e.getXOnScreen();
                point[1] = e.getYOnScreen();
                setLocation(newX, newY);
            }
        });
    }

    private void createMenuPanel() {
        menuPanel = new MenuPanel();
        add(menuPanel);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    public class MenuPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
        private BufferedImage backgroundImage;
        private Point mousePosition = new Point(0, 0);
        private ArrayList<MenuElement> uiElements;
        private boolean isMousePressed = false;
        private boolean isHoveringButton = false;
        private boolean wasHoveringButton = false;
        
        private MenuElement player1Text, player2Text, player3Text, player4Text, playerCountText;
        private MenuElement player1Img, player2Img, player3Img, player4Img;
        private MenuElement player1Icon, player2Icon, player3Icon, player4Icon;

        public MenuPanel() {
            uiElements = new ArrayList<>();
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            loadBackgroundImage();
            setCursor(CursorManager.getNormalCursor());
            addMouseListener(this);
            addMouseMotionListener(this);
            loadMenuElements(this);
            
            NetworkManager.getInstance().setLobbyMenu(GameLobbyMenu.this);
            if (!NetworkManager.getInstance().isConnected()) {
                boolean connected = NetworkManager.getInstance().connect(GameConfig.SERVER_HOST, GameConfig.SERVER_PORT);
                if (!connected) {
                    JOptionPane.showMessageDialog(this, "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้", "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void addElement(MenuElement element) {
            uiElements.add(element);
        }

        private void loadBackgroundImage() {
            try {
                String fullPath = System.getProperty("user.dir") + java.io.File.separator + MENU_BACKGROUND;
                File imageFile = new File(fullPath);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                }
            } catch (Exception ex) {
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (backgroundImage != null) {
                int imageWidth = backgroundImage.getWidth();
                int imageHeight = backgroundImage.getHeight();

                double scaleX = (double) getWidth() / imageWidth;
                double scaleY = (double) getHeight() / imageHeight;
                double scale = Math.max(scaleX, scaleY);

                int scaledWidth = (int) (imageWidth * scale);
                int scaledHeight = (int) (imageHeight * scale);

                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;

                g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);
            } else {
                g2d.setColor(new Color(20, 20, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            if (uiElements != null) {
                isHoveringButton = false;

                for (MenuElement element : uiElements) {
                    if (element.getType() == MenuElement.ElementType.IMAGE && element.getButtonId() != null) {
                        String buttonId = element.getButtonId();
                        if (!"back_icon".equals(buttonId)) {
                            boolean isHovering = element.contains(mousePosition.x, mousePosition.y);
                            element.setHovered(isHovering);
                            
                            if (isHovering) {
                                isHoveringButton = true;
                            }
                        } else {
                            element.setHovered(false);
                        }
                    }
                }

                for (MenuElement element : uiElements) {
                    element.render(g2d);
                }
            }

            if (isHoveringButton && !wasHoveringButton) {
                playHoverSound();
            }
            wasHoveringButton = isHoveringButton;

            g2d.setColor(new Color(255, 255, 255, 255));
            g2d.setFont(FontManager.getThaiFont(26));
            String versionText = "v " + GitVersion.getVersion();
            g2d.drawString(versionText, 20, getHeight() - 20);

            updateCursor();
        }

        private void updateCursor() {
            if (isMousePressed) {
                setCursor(CursorManager.getPressCursor());
            } else {
                setCursor(CursorManager.getNormalCursor());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        public static void loadMenuElements(MenuPanel panel) {
            MenuElement img1 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\Computer-Crappy.png", 26.0, 293.0, 524.5, 481.4);
            panel.addElement(img1);
            
            MenuElement img2 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\CD.png", 1496.0, 10.0, 774.7, 774.7);
            panel.addElement(img2);
            
            panel.player1Img = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\1.png", 420.0, 479.0, 245.9, 245.9);
            panel.player1Img.setVisibility(false);
            panel.addElement(panel.player1Img);
            
            panel.player2Img = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\2.png", 693.0, 479.0, 245.8, 245.8);
            panel.player2Img.setVisibility(false);
            panel.addElement(panel.player2Img);
            
            panel.player3Img = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\3.png", 971.0, 478.1, 247.6, 247.6);
            panel.player3Img.setVisibility(false);
            panel.addElement(panel.player3Img);
            
            panel.player4Img = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\4.png", 1255.0, 479.3, 245.3, 245.3);
            panel.player4Img.setVisibility(false);
            panel.addElement(panel.player4Img);
            
            panel.player1Icon = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\Icon-Attribute-Score.png", 550.5, 366.0, 193.2, 193.2);
            panel.player1Icon.setVisibility(false);
            panel.addElement(panel.player1Icon);
            
            panel.player2Icon = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\Icon-Attribute-Score.png", 828.0, 364.3, 196.5, 196.5);
            panel.player2Icon.setVisibility(false);
            panel.addElement(panel.player2Icon);
            
            panel.player3Icon = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\Icon-Attribute-Score.png", 1112.0, 378.4, 195.0, 195.0);
            panel.player3Icon.setVisibility(false);
            panel.addElement(panel.player3Icon);
            
            panel.player4Icon = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\Icon-Attribute-Score.png", 1402.0, 379.0, 193.8, 193.8);
            panel.player4Icon.setVisibility(false);
            panel.addElement(panel.player4Icon);
            
            MenuElement img7 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\button\\Button-Medium-Gray.png", 693.0, 784.7, 540.0, 195.0);
            panel.addElement(img7);
            
            MenuElement img8 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\QuestNotepad_Small.png", 529.1, 66.0, 861.8, 312.4);
            panel.addElement(img8);
            
            panel.player1Text = new MenuElement("", 473.9, 530.0, 32);
            panel.player1Text.setVisibility(false);
            panel.player1Text.setTextColor(new Color(30, 30, 40));
            panel.addElement(panel.player1Text);
            
            panel.player2Text = new MenuElement("", 746.9, 530.0, 32);
            panel.player2Text.setVisibility(false);
            panel.player2Text.setTextColor(new Color(30, 30, 40));
            panel.addElement(panel.player2Text);
            
            panel.player3Text = new MenuElement("", 1025.8, 530.0, 32);
            panel.player3Text.setVisibility(false);
            panel.player3Text.setTextColor(new Color(30, 30, 40));
            panel.addElement(panel.player3Text);
            
            panel.player4Text = new MenuElement("", 1308.6, 530.0, 32);
            panel.player4Text.setVisibility(false);
            panel.player4Text.setTextColor(new Color(30, 30, 40));
            panel.addElement(panel.player4Text);
            
            panel.playerCountText = new MenuElement("กำลังโหลด...", 828.0, 893.0, 50);
            panel.addElement(panel.playerCountText);

            MenuElement backButton = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "button" + File.separator
                            + "Button-Small-Blue.png",
                    104.0, 49.0, 367.0, 196.0,
                    "back_button",
                    () -> panel.backToGameMode());
            backButton.setHoverImage("assets" + File.separator + "ui" + File.separator + "button" + File.separator
                    + "Button-Small-Pink.png");
            panel.addElement(backButton);

            MenuElement backIcon = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "icon" + File.separator + "Back-Arrow.png",
                    204.0, 85.5, 167.0, 123.0,
                    "back_icon",
                    () -> panel.backToGameMode());
            panel.addElement(backIcon);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mousePosition = e.getPoint();
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();
                for (MenuElement element : uiElements) {
                    if (element.getType() == MenuElement.ElementType.IMAGE && element.contains(p.x, p.y) && element.getButtonId() != null) {
                        playClickSound();
                        element.executeAction();
                        return;
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                isMousePressed = true;
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                isMousePressed = false;
                repaint();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        private void playHoverSound() {
            SoundManager.getInstance()
                    .playSFX("assets" + File.separator + "sfx" + File.separator + "Button Select.wav");
        }

        private void playClickSound() {
            SoundManager.getInstance()
                    .playSFX("assets" + File.separator + "sfx" + File.separator + "Button Click 1.wav");
        }

        public void startGame() {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            SoundManager.getInstance().stopBackgroundMusic();

            SwingUtilities.invokeLater(() -> {
            
                boolean isOnlineMode = NetworkManager.getInstance().isConnected();
                System.out.println("=== GameLobbyMenu: Starting game in online mode: " + isOnlineMode + " ===");
                
                GameWindow gameWindow = new GameWindow(isOnlineMode);
                gameWindow.setVisible(true);

                javax.swing.Timer timer = new javax.swing.Timer(100, _ -> {
                    parentFrame.dispose();
                });
                timer.setRepeats(false);
                timer.start();
            });
        }

        public void backToGameMode() {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            
            NetworkManager.getInstance().disconnect();

            SwingUtilities.invokeLater(() -> {
                GameModeMenu gameModeMenu = new GameModeMenu();
                gameModeMenu.setVisible(true);

                javax.swing.Timer timer = new javax.swing.Timer(100, _ -> {
                    parentFrame.dispose();
                });
                timer.setRepeats(false);
                timer.start();
            });
        }
        
    }
    
    public void showGameStartedMessage() {
        if (menuPanel != null && menuPanel.playerCountText != null) {
            menuPanel.playerCountText.setText("เกมเริ่มไปแล้ว...");
            menuPanel.repaint();
        }
    }
    
    public void updateLobbyInfo(List<PlayerInfo> players) {
        System.out.println("=== GameLobbyMenu: updateLobbyInfo called with " + players.size() + " players ===");
        for (PlayerInfo p : players) {
            System.out.println("  Player " + p.playerId + ": " + p.playerName + " - " + (p.isConnected ? "CONNECTED" : "DISCONNECTED"));
        }
        SwingUtilities.invokeLater(() -> {
            if (menuPanel != null) {
                int connectedCount = 0;
                int localPlayerId = NetworkManager.getInstance().getPlayerId();
                System.out.println("=== GameLobbyMenu: menuPanel is not null, updating UI, localPlayerId=" + localPlayerId + " ===");
                
                for (int i = 0; i < 4; i++) {
                    MenuElement imgElement = null;
                    MenuElement textElement = null;
                    MenuElement iconElement = null;
                    
                    switch (i) {
                        case 0: 
                            imgElement = menuPanel.player1Img;
                            textElement = menuPanel.player1Text; 
                            iconElement = menuPanel.player1Icon;
                            break;
                        case 1: 
                            imgElement = menuPanel.player2Img;
                            textElement = menuPanel.player2Text; 
                            iconElement = menuPanel.player2Icon;
                            break;
                        case 2: 
                            imgElement = menuPanel.player3Img;
                            textElement = menuPanel.player3Text; 
                            iconElement = menuPanel.player3Icon;
                            break;
                        case 3: 
                            imgElement = menuPanel.player4Img;
                            textElement = menuPanel.player4Text; 
                            iconElement = menuPanel.player4Icon;
                            break;
                    }
                    
                    boolean isConnected = (i < players.size() && players.get(i).isConnected);
                    boolean isLocalPlayer = (i + 1 == localPlayerId);
                    
                    if (imgElement != null) {
                        imgElement.setVisibility(isConnected);
                    }
                    
                    if (iconElement != null) {
                        iconElement.setVisibility(isLocalPlayer && isConnected);
                    }
                    
                    if (textElement != null) {
                        if (isConnected) {
                            textElement.setText("Player_" + (i + 1));
                            textElement.setTextColor(new Color(30, 30, 40));
                            textElement.setVisibility(true);
                            connectedCount++;
                            System.out.println("Slot " + (i+1) + ": Set text to Player_" + (i + 1));
                        } else {
                            textElement.setText("");
                            textElement.setVisibility(false);
                        }
                    }
                }
                
                if (menuPanel.playerCountText != null) {
                    menuPanel.playerCountText.setText("รอผู้เล่น " + connectedCount + "/" + GameConfig.MAX_PLAYERS);
                }
                
                menuPanel.repaint();
                System.out.println("=== GameLobbyMenu: UI update complete, called repaint() ===");
            } else {
                System.out.println("=== GameLobbyMenu: ERROR - menuPanel is null! ===");
            }
        });
    }
    
    public void startGame() {
        menuPanel.startGame();
    }
}

