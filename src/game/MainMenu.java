package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MainMenu extends JFrame {
    private static final String MENU_BACKGROUND = "assets" + java.io.File.separator + "background" + java.io.File.separator + "Score-Screen-Background.png";
    
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
                        element.setHovered(isHovering);
                        if (isHovering) {
                            isHoveringButton = true;
                        }
                    }
                    element.render(g2d);
                }
            }
            
            if (isHoveringButton && !wasHoveringButton) {
                playHoverSound();
            }
            wasHoveringButton = isHoveringButton;
            
            updateCursor();
        }
        
        private void updateCursor() {
            if (isMousePressed && isHoveringButton) {
                setCursor(CursorManager.getPressCursor());
            } else  {
                setCursor(CursorManager.getNormalCursor());
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
        public static void loadMenuElements(MenuPanel panel) {
            MenuElement buttonPlay = new MenuElement(
                MenuElement.ElementType.IMAGE, 
                "assets/ui/button/Button-Big-Blue.png", 
                561.0, 267.0, 798.0, 196.0,
                "start_game",
                () -> panel.startGame()
            );
            panel.addElement(buttonPlay);
            
            MenuElement text = new MenuElement("เล่นเกม", 790.5, 395.0, 100);
            panel.addElement(text);
            
            MenuElement button2 = new MenuElement(
                MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Big-Gray.png", 
                561.0, 785.0, 798.0, 196.0,
                "button_3",
                () -> panel.handleButton("button_3")
            );
            panel.addElement(button2);
            
            MenuElement button1 = new MenuElement(
                MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Big-Gray.png", 
                561.0, 518.0, 798.0, 196.0,
                "button_2",
                () -> panel.handleButton("button_2")
            );
            panel.addElement(button1);
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
        public void mouseEntered(MouseEvent e) {}
        
        @Override
        public void mouseExited(MouseEvent e) {}
        
        @Override
        public void mouseDragged(MouseEvent e) {}
        
        public void handleButton(String buttonId) {
            System.out.println("Button clicked: " + buttonId);
            
            switch(buttonId) {
                case "button_2":
                    System.out.println("Button 2 pressed!");
                    break;
                case "button_3":
                    System.out.println("Button 3 pressed!");
                    break;
                default:
                    break;
            }
        }
        
        private void playHoverSound() {
            playSound("assets" + File.separator + "sfx" + File.separator + "Button Select.wav");
        }
        
        private void playClickSound() {
            playSound("assets" + File.separator + "sfx" + File.separator + "Button Click 1.wav");
        }
        
        private void playSound(String soundPath) {
            try {
                File soundFile = new File(soundPath);
                if (!soundFile.exists()) {
                    soundFile = new File(System.getProperty("user.dir") + File.separator + soundPath);
                }
                if (soundFile.exists()) {
                    javax.sound.sampled.AudioInputStream audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
                    javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                }
            } catch (Exception ex) {
            }
        }
        
        public void startGame() {
            SwingUtilities.getWindowAncestor(this).dispose();
            SwingUtilities.invokeLater(() -> {
                GameWindow gameWindow = new GameWindow();
                gameWindow.setVisible(true);
            });
        }
    }
}

