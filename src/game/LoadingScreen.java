package game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class LoadingScreen extends JFrame {
    private static final String BACKGROUND = "assets/background/bg.png";
    
    private LoadingPanel loadingPanel;
    private MainMenu preloadedMenu;
    
    public LoadingScreen() {
        initializeWindow();
        createLoadingPanel();
        preloadMainMenu();
        pack();
        centerWindow();
        setCursor(CursorManager.getNormalCursor());
    }
    
    private void preloadMainMenu() {
        new Thread(() -> {
            try {
                preloadedMenu = new MainMenu();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void initializeWindow() {
        setTitle("No Brakes Life - Loading");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
    }
    
    private void createLoadingPanel() {
        loadingPanel = new LoadingPanel();
        add(loadingPanel);
    }
    
    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }
    
    private class LoadingPanel extends JPanel {
        private BufferedImage backgroundImage;
        private ArrayList<BufferedImage> animationFrames;
        private int currentFrame;
        private long lastFrameTime;
        private long frameDelay = 50;
        private boolean showAnimation;
        private long animationStartTime;
        private long delayAfterAnimation = GameConfig.LOADING_SCREEN_DELAY_MS;
        private boolean inWaitingState;
        private boolean menuOpened = false;
        private long animationCompleteTime;
        
        public LoadingPanel() {
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            loadBackgroundImage();
            loadAnimationFrames();
            animationStartTime = System.currentTimeMillis() + 500;
            startAnimationTimer();
        }
        
        private void loadBackgroundImage() {
            try {
                String fullPath = System.getProperty("user.dir") + File.separator + BACKGROUND;
                File imageFile = new File(fullPath);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                }
            } catch (Exception ex) {
            }
        }
        
        private void loadAnimationFrames() {
            animationFrames = new ArrayList<>();
            String basePath = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "ui" + File.separator + "loading";
            
            for (int i = 1; i <= 13; i++) {
                String frameFile = String.format("%02d.png", i);
                String fullPath = basePath + File.separator + frameFile;
                
                try {
                    File file = new File(fullPath);
                    if (file.exists()) {
                        BufferedImage image = ImageIO.read(file);
                        if (image != null) {
                            animationFrames.add(image);
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        
        private void startAnimationTimer() {
            javax.swing.Timer timer = new javax.swing.Timer(30, e -> {
                long currentTime = System.currentTimeMillis();
                
                if (!showAnimation && currentTime >= animationStartTime) {
                    showAnimation = true;
                    inWaitingState = false;
                }
                
                if (showAnimation && !inWaitingState) {
                    if (currentTime - lastFrameTime >= frameDelay) {
                        if (animationFrames.size() > 0) {
                            currentFrame = (currentFrame + 1) % animationFrames.size();
                            lastFrameTime = currentTime;
                        }
                    }
                    
                    if (currentTime - animationStartTime >= delayAfterAnimation && !menuOpened) {
                        inWaitingState = true;
                        animationCompleteTime = currentTime;
                    }
                }
                
                if (inWaitingState && !menuOpened) {
                    if (LoadingScreen.this.preloadedMenu != null) {
                        long elapsed = currentTime - animationCompleteTime;
                        if (elapsed > 100) {
                            menuOpened = true;
                            closeAndOpenMenu();
                        }
                    } else {
                        long elapsed = currentTime - animationCompleteTime;
                        if (elapsed > 2000) {
                            menuOpened = true;
                            closeAndOpenMenu();
                        }
                    }
                }
                
                repaint();
            });
            timer.start();
        }
        
        private void closeAndOpenMenu() {
            SwingUtilities.invokeLater(() -> {
                MainMenu menuToShow = LoadingScreen.this.preloadedMenu;
                
                if (menuToShow == null) {
                    menuToShow = new MainMenu();
                }
                
                menuToShow.setVisible(true);
                LoadingScreen.this.setVisible(false);
                LoadingScreen.this.dispose();
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            if (!showAnimation) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                return;
            }
            
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
            
            if (animationFrames != null && animationFrames.size() > 0 && currentFrame >= 0 && currentFrame < animationFrames.size()) {
                BufferedImage currentFrameImg = animationFrames.get(currentFrame);
                if (currentFrameImg != null) {
                    int frameWidth = currentFrameImg.getWidth();
                    int frameHeight = currentFrameImg.getHeight();
                    
                    double scaleFactor = 0.3;
                    int scaledFrameWidth = (int) (frameWidth * scaleFactor);
                    int scaledFrameHeight = (int) (frameHeight * scaleFactor);
                    
                    int frameX = (getWidth() - scaledFrameWidth) / 2;
                    int frameY = (getHeight() - scaledFrameHeight) / 2;
                    
                    g2d.drawImage(currentFrameImg, frameX, frameY, scaledFrameWidth, scaledFrameHeight, null);
                }
            }
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(FontManager.getThaiFont(32));
            String loadingText = "รอแป๊บกำลังโหลดนะน้องนะ...";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(loadingText);
            int textX = getWidth() - textWidth - 60;
            int textY = getHeight() - 30;
            g2d.drawString(loadingText, textX, textY);
            g2d.setColor(new Color(255, 255, 255, 255));
            g2d.setFont(FontManager.getThaiFont(26));
            String versionText = "v " + GitVersion.getVersion();
            g2d.drawString(versionText, 20, getHeight() - 20);
        }
    }
}

