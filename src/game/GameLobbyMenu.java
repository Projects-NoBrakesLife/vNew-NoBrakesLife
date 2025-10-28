package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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

    private class MenuPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
        private BufferedImage backgroundImage;
        private Point mousePosition = new Point(0, 0);
        private ArrayList<MenuElement> uiElements;
        private boolean isMousePressed = false;
        private boolean isHoveringButton = false;
        private boolean wasHoveringButton = false;

        public MenuPanel() {
            uiElements = new ArrayList<>();
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            loadBackgroundImage();
            setCursor(CursorManager.getNormalCursor());
            addMouseListener(this);
            addMouseMotionListener(this);
            loadMenuElements(this);
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
            
            MenuElement img3 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\1.png", 420.0, 479.0, 245.9, 245.9);
            panel.addElement(img3);
            
            MenuElement img4 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\2.png", 693.0, 479.0, 245.8, 245.8);
            panel.addElement(img4);
            
            MenuElement img5 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\3.png", 971.0, 478.1, 247.6, 247.6);
            panel.addElement(img5);
            
            MenuElement img6 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\4.png", 1255.0, 479.3, 245.3, 245.3);
            panel.addElement(img6);
            
            MenuElement img7 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\button\\Button-Medium-Gray.png", 693.0, 784.7, 540.0, 195.0);
            panel.addElement(img7);
            
            MenuElement img8 = new MenuElement(MenuElement.ElementType.IMAGE, "assets\\ui\\joins\\QuestNotepad_Small.png", 529.1, 66.0, 861.8, 312.4);
            panel.addElement(img8);
            
            MenuElement text1 = new MenuElement("Player_1", 473.9, 530.0, 32);
            panel.addElement(text1);
            
            MenuElement text2 = new MenuElement("Player_2", 746.9, 530.0, 32);
            panel.addElement(text2);
            
            MenuElement text3 = new MenuElement("Player_3", 1025.8, 530.0, 32);
            panel.addElement(text3);
            
            MenuElement text4 = new MenuElement("Player_4", 1308.6, 530.0, 32);
            panel.addElement(text4);
            
            MenuElement text5 = new MenuElement("รอผู้เล่น 2/4", 828.0, 893.0, 50);
            panel.addElement(text5);

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
                GameWindow gameWindow = new GameWindow();
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
}

