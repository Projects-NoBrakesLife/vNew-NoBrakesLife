package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MainMenu extends JFrame {
    private static final String MENU_BACKGROUND = "assets" + java.io.File.separator + "background"
            + java.io.File.separator + "Score-Screen-Background.png";

    private MenuPanel menuPanel;

    public MainMenu() {
        initializeWindow();
        createMenuPanel();
        pack();
        centerWindow();
    }

    private void initializeWindow() {
        setTitle("No Brakes Life - Main Menu");
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
        private ArrayList<AnimatedMenuElement> animatedElements;
        private boolean isMousePressed = false;
        private boolean isHoveringButton = false;
        private boolean wasHoveringButton = false;

        public MenuPanel() {
            uiElements = new ArrayList<>();
            animatedElements = new ArrayList<>();
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            loadBackgroundImage();
            setCursor(CursorManager.getNormalCursor());
            addMouseListener(this);
            addMouseMotionListener(this);
            loadMenuElements(this);
            startAnimationTimer();

            SoundManager.getInstance().playBackgroundMusic(
                    "assets" + File.separator + "sfx" + File.separator + "Walk_Of_Life_TRACK2.wav");
        }

        private void startAnimationTimer() {
            javax.swing.Timer timer = new javax.swing.Timer(15, _ -> {
                for (AnimatedMenuElement anim : animatedElements) {
                    anim.update();
                }
                repaint();
            });
            timer.start();
        }

        public void addElement(MenuElement element) {
            uiElements.add(element);
        }

        public void addAnimatedElement(AnimatedMenuElement element) {
            animatedElements.add(element);
        }

        private void loadBackgroundImage() {
            try {
                String fullPath = System.getProperty("user.dir") + java.io.File.separator + MENU_BACKGROUND;
                File imageFile = new File(fullPath);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                }
            } catch (Exception ex) {
                System.err.println("ไม่สามารถโหลดภาพพื้นหลังได้: " + ex.getMessage());
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
                MenuElement hoveredCard = null;
                MenuElement hoveredCardText = null;
                MenuElement iconElement = null;
                MenuElement smallButtonElement = null;

                for (MenuElement element : uiElements) {
                    element.setMousePosition(mousePosition);

                    String imagePath = element.getImagePath();
                    if (imagePath != null && imagePath.contains("WOLTempIcon_Head")) {
                        iconElement = element;
                    } else if (imagePath != null && imagePath.contains("Button-Small-Blue")) {
                        smallButtonElement = element;
                    } else if (imagePath != null && imagePath.contains("Card-Beg-For-Wishlist")) {
                        hoveredCard = element;
                        boolean isHovering = element.contains(mousePosition.x, mousePosition.y);
                        element.setHovered(isHovering);
                    } else if (element.getText() != null && element.getText().equals("เด็ก CS กำลังขอเกรด A")) {
                        hoveredCardText = element;
                        boolean isHovering = element.contains(mousePosition.x, mousePosition.y);
                        element.setHovered(isHovering);

                        if (isHovering && hoveredCard != null) {
                            hoveredCard.setHovered(true);
                        }
                    }
                }

                if (iconElement != null && smallButtonElement != null) {
                    boolean iconHovering = iconElement.contains(mousePosition.x, mousePosition.y);
                    smallButtonElement.setHovered(iconHovering);
                }

                if (hoveredCard != null && hoveredCardText != null && hoveredCard.isHovered()) {
                    hoveredCardText.setHovered(true);
                }

                boolean hasButtonHovered = false;
                for (MenuElement element : uiElements) {
                    if (element.getType() == MenuElement.ElementType.IMAGE) {
                        boolean isHovering = element.contains(mousePosition.x, mousePosition.y);
                        if (element.getButtonId() != null) {
                            element.setHovered(isHovering);
                            if (isHovering) {
                                isHoveringButton = true;
                                hasButtonHovered = true;

                                if (iconElement != null) {
                                    String buttonId = element.getButtonId();
                                    if ("start_game".equals(buttonId)) {
                                        iconElement.setForcedAngle(-20);
                                    } else if ("button_2".equals(buttonId)) {
                                        iconElement.setForcedAngle(0.4);
                                    } else if ("button_3".equals(buttonId)) {
                                        iconElement.setForcedAngle(14);
                                    }
                                }
                            }
                        }
                    }
                }

                if (iconElement != null && !hasButtonHovered) {
                    iconElement.clearForcedAngle();
                }

                for (MenuElement element : uiElements) {
                    element.render(g2d);
                }
            }

            if (isHoveringButton && !wasHoveringButton) {
                playHoverSound();
            }
            wasHoveringButton = isHoveringButton;

            if (animatedElements != null) {
                for (AnimatedMenuElement anim : animatedElements) {
                    anim.render(g2d);
                }
            }

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
            MenuElement smallButton = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "button" + File.separator
                            + "Button-Small-Blue.png",
                    81.0, 33.0, 385.4, 205.8,
                    "about_button",
                    () -> panel.openAbout());
            smallButton.setHoverImage("assets" + File.separator + "ui" + File.separator + "button" + File.separator
                    + "Button-Small-Pink.png");
            panel.addElement(smallButton);

            MenuElement icon = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "icon" + File.separator
                            + "WOLTempIcon_Head.png",
                    184.3, 57.4, 178.8, 157.1);
            icon.setTrackMouse(true);
            panel.addElement(icon);

            MenuElement buttonPlay = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets/ui/button/Button-Big-Blue.png",
                    561.0, 267.0, 798.0, 196.0,
                    "start_game",
                    () -> panel.startGame());
            panel.addElement(buttonPlay);

            MenuElement text1 = new MenuElement("เล่นเกม", 790.5, 395.0, 100);
            panel.addElement(text1);

            MenuElement button1 = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "button" + File.separator
                            + "Button-Big-Blue.png",
                    561.0, 518.0, 798.0, 196.0,
                    "button_2",
                    () -> panel.handleButton("button_2"));
            panel.addElement(button1);

            MenuElement text2 = new MenuElement("ตั้งค่า", 840.5, 661.0, 100);
            panel.addElement(text2);

            MenuElement button2 = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "button" + File.separator
                            + "Button-Big-Blue.png",
                    561.0, 785.0, 798.0, 196.0,
                    "button_3",
                    () -> panel.handleButton("button_3"));
            panel.addElement(button2);

            MenuElement text3 = new MenuElement("ออก", 840.5, 912.0, 100);
            panel.addElement(text3);

            MenuElement logo = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets/ui/logo/logo.png",
                    718.1, 23.0, 483.7, 285.2);
            panel.addElement(logo);

            AnimatedMenuElement settings = new AnimatedMenuElement(
                    "assets/ui/logo",
                    1127.0, 117.0, 121.7, 121.7,
                    200);
            panel.addAnimatedElement(settings);

            MenuElement card = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "Card-Beg-For-Wishlist.png",
                    1460.0, 282.0, 409.8, 581.2);
            card.setUseScaleEffect(true);
            panel.addElement(card);

            MenuElement cardText = new MenuElement("เด็ก CS กำลังขอเกรด A", 1487.9, 383.0, 35);
            cardText.setTextColor(new Color(30, 30, 40));

            panel.addElement(cardText);
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
                    if (element.getType() == MenuElement.ElementType.IMAGE && element.contains(p.x, p.y)) {
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

        public void handleButton(String buttonId) {
            System.out.println("Button clicked: " + buttonId);

            switch (buttonId) {
                case "button_2":
                    openSettings();
                    break;
                case "button_3":
                    System.out.println("ออกจากเกม");
                    System.exit(0);
                    break;
                default:
                    break;
            }
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

        public void openSettings() {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            SwingUtilities.invokeLater(() -> {
                SettingsMenu settingsMenu = new SettingsMenu();
                settingsMenu.setVisible(true);

                javax.swing.Timer timer = new javax.swing.Timer(50, _ -> {
                    parentFrame.setVisible(false);
                });
                timer.setRepeats(false);
                timer.start();
            });
        }

        public void openAbout() {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            SwingUtilities.invokeLater(() -> {
                AboutMenu aboutMenu = new AboutMenu();
                aboutMenu.setVisible(true);

                javax.swing.Timer timer = new javax.swing.Timer(50, _ -> {
                    parentFrame.setVisible(false);
                });
                timer.setRepeats(false);
                timer.start();
            });
        }
    }
}
