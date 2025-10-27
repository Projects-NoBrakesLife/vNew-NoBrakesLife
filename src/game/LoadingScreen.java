package game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class LoadingScreen extends JFrame {
    private static final String BACKGROUND = "assets/background/Card Positive Image BG.png";
    
    private LoadingPanel loadingPanel;
    
    public LoadingScreen() {
        initializeWindow();
        createLoadingPanel();
        pack();
        centerWindow();
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
        private long frameDelay = 150;
        private boolean animationComplete;
        
        public LoadingPanel() {
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            loadBackgroundImage();
            loadAnimationFrames();
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
            javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFrameTime >= frameDelay && !animationComplete) {
                    if (animationFrames.size() > 0) {
                        currentFrame++;
                        lastFrameTime = currentTime;
                        
                        if (currentFrame >= animationFrames.size()) {
                            animationComplete = true;
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
                dispose();
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
            });
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
            
            if (animationFrames != null && animationFrames.size() > 0 && currentFrame < animationFrames.size()) {
                BufferedImage currentFrameImg = animationFrames.get(currentFrame);
                if (currentFrameImg != null) {
                    int frameWidth = currentFrameImg.getWidth();
                    int frameHeight = currentFrameImg.getHeight();
                    
                    int frameX = (getWidth() - frameWidth) / 2;
                    int frameY = (getHeight() - frameHeight) / 2;
                    
                    g2d.drawImage(currentFrameImg, frameX, frameY, null);
                }
            }
        }
    }
}

