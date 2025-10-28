package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class AboutMenu extends JFrame {
    private static final String MENU_BACKGROUND = "assets" + File.separator + "background" + File.separator
            + "Score-Screen-Background.png";

    private AboutPanel aboutPanel;

    public AboutMenu() {
        initializeWindow();
        createAboutPanel();
        pack();
        centerWindow();
    }

    private void initializeWindow() {
        setTitle("No Brakes Life - About");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        addDragSupport();
        addKeyListener();
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

    private void addKeyListener() {
        final AboutMenu self = this;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                self.backToMainMenuStatic();
                return true;
            }
            return false;
        });
    }

    private void createAboutPanel() {
        aboutPanel = new AboutPanel();
        add(aboutPanel);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private class AboutPanel extends JPanel implements MouseListener, MouseMotionListener {
        private BufferedImage backgroundImage;
        private Point mousePosition = new Point(0, 0);
        private ArrayList<MenuElement> uiElements;
        private boolean isMousePressed = false;
        private boolean isHoveringButton = false;
        private boolean wasHoveringButton = false;

        public AboutPanel() {
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
                String fullPath = System.getProperty("user.dir") + File.separator + MENU_BACKGROUND;
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
                for (MenuElement element : uiElements) {
                    if (element.getType() == MenuElement.ElementType.IMAGE) {
                        boolean isHovering = element.contains(mousePosition.x, mousePosition.y);

                        if (element.getImagePath() != null && element.getImagePath().contains("1.png")) {
                            element.setHovered(isHovering);
                            if (isHovering) {
                                isHoveringButton = true;
                            }
                        }

                        if (element.getButtonId() != null) {
                            element.setHovered(isHovering);
                            if (isHovering) {
                                isHoveringButton = true;
                            }
                        }
                    }
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

        public static void loadMenuElements(AboutPanel panel) {
            MenuElement img = new MenuElement(MenuElement.ElementType.IMAGE, "assets" + File.separator + "ui"
                    + File.separator + "teams" + File.separator + "Panel-Background-Small.png", 453.0, 190.0, 1055.5,
                    744.2);
            panel.addElement(img);
            MenuElement img2 = new MenuElement(MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "teams" + File.separator + "1.png", 524.0,
                    271.0, 176.2, 263.3);
            img2.setUseScaleEffect(true);
            panel.addElement(img2);
            MenuElement img3 = new MenuElement(MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "teams" + File.separator + "1.png", 766.0,
                    271.2, 175.9, 262.9);
            img3.setUseScaleEffect(true);
            panel.addElement(img3);
            MenuElement img4 = new MenuElement(MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "teams" + File.separator + "1.png", 999.0,
                    272.2, 174.6, 260.9);
            img4.setUseScaleEffect(true);
            panel.addElement(img4);
            MenuElement img5 = new MenuElement(MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "teams" + File.separator + "1.png", 1250.0,
                    272.4, 174.3, 260.6);
            img5.setUseScaleEffect(true);
            panel.addElement(img5);
            MenuElement text = new MenuElement("By Team C++", 524.0, 600.0, 38);
            text.setTextColor(new Color(30, 30, 40));
            panel.addElement(text);

            MenuElement backButton = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "button" + File.separator
                            + "Button-Small-Blue.png",
                    104.0, 49.0, 367.0, 196.0,
                    "back_button",
                    () -> panel.backToMainMenu());
            backButton.setHoverImage("assets" + File.separator + "ui" + File.separator + "button" + File.separator
                    + "Button-Small-Pink.png");
            panel.addElement(backButton);

            MenuElement backIcon = new MenuElement(
                    MenuElement.ElementType.IMAGE,
                    "assets" + File.separator + "ui" + File.separator + "icon" + File.separator + "Back-Arrow.png",
                    204.0, 85.5, 167.0, 123.0);
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

        private void playHoverSound() {
            SoundManager.getInstance()
                    .playSFX("assets" + File.separator + "sfx" + File.separator + "Button Select.wav");
        }

        private void playClickSound() {
            SoundManager.getInstance()
                    .playSFX("assets" + File.separator + "sfx" + File.separator + "Button Click 1.wav");
        }

        public void backToMainMenu() {
            SwingUtilities.invokeLater(() -> {
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);

                javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
                    AboutMenu.this.setVisible(false);
                });
                timer.setRepeats(false);
                timer.start();
            });
        }
    }

    public void backToMainMenuStatic() {
        SwingUtilities.invokeLater(() -> {
            MainMenu mainMenu = new MainMenu();
            mainMenu.setVisible(true);

            javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
                this.setVisible(false);
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}
