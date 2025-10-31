package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class PopupWindow extends JDialog {
    private PopupWindowConfig config;
    private ArrayList<MenuElement> elements;
    private BufferedImage backgroundImage;
    public static interface PurchaseHandler { void onPurchase(String productId); }
    public static interface QuestionHandler { void onAnswerSubmit(String answer); }
    private PurchaseHandler purchaseHandler;
    private QuestionHandler questionHandler;
    private java.util.ArrayDeque<NotificationData> notifyQueue = new java.util.ArrayDeque<>();
    private NotificationData currentNotify;
    private long notifyStartMs = 0L;
    private final int notifyDurationMs = 2000;
    private JTextField answerField;
    private MenuElement questionTextElement;
    
    private boolean isFishingAnimation = false;
    private String currentFishingFishImage = null;
    private final long fishingImageChangeInterval = 200L;
    private javax.swing.Timer fishingAnimationTimer;
    
    private static class NotificationData {
        String message;
        String imagePath;
        NotificationData(String msg, String img) {
            message = msg;
            imagePath = img;
        }
    }

    public PopupWindow(JFrame parent, PopupWindowConfig config, ArrayList<MenuElement> elements) {
        super(parent, true); 
        this.config = config;
        this.elements = new ArrayList<>(elements);
        
        initializeWindow();
        loadBackgroundImage();
        createContentPanel();
        pack();
        centerWindow();
    }

    public PopupWindow(PopupWindowConfig config, ArrayList<MenuElement> elements) {
        super((Frame) null, true);
        this.config = config;
        this.elements = new ArrayList<>(elements);
        this.purchaseHandler = null;
        
        initializeWindow();
        loadBackgroundImage();
        createContentPanel();
        pack();
        centerWindow();
    }

    public PopupWindow(PopupWindowConfig config, ArrayList<MenuElement> elements, PurchaseHandler handler) {
        this(config, elements, handler, null);
    }
    
    public PopupWindow(PopupWindowConfig config, ArrayList<MenuElement> elements, PurchaseHandler handler, QuestionHandler qHandler) {
        super((Frame) null, true);
        this.config = config;
        this.elements = new ArrayList<>(elements);
        this.purchaseHandler = handler;
        this.questionHandler = qHandler;
        initializeWindow();
        loadBackgroundImage();
        createContentPanel();
        pack();
        centerWindow();
    }

    public void showNotification(String msg) {
        showNotification(msg, null);
    }
    
    public void showNotification(String msg, String imagePath) {
        if (msg == null || msg.trim().isEmpty()) return;
        notifyQueue.offer(new NotificationData(msg, imagePath));
        if (currentNotify == null) {
            currentNotify = notifyQueue.poll();
            notifyStartMs = System.currentTimeMillis();
        }
        if (getContentPane() != null) {
            getContentPane().repaint();
        }
    }

    public void clearNotifications() {
        currentNotify = null;
        notifyQueue.clear();
        if (getContentPane() != null) {
            getContentPane().repaint();
        }
    }
    
    public void setQuestion(String questionText) {
        if (questionTextElement != null) {
            questionTextElement.setText(questionText);
        }
        if (answerField != null) {
            answerField.setText("");
        }
        if (getContentPane() != null) {
            getContentPane().repaint();
        }
    }
    
    public void startFishingAnimation() {
        isFishingAnimation = true;
        if (GameConfig.FISHES != null && GameConfig.FISHES.length > 0) {
            int randomIndex = (int)(Math.random() * GameConfig.FISHES.length);
            currentFishingFishImage = GameConfig.FISHES[randomIndex].imagePath;
        }
        
        if (fishingAnimationTimer != null) {
            fishingAnimationTimer.stop();
        }
        
        fishingAnimationTimer = new javax.swing.Timer((int)fishingImageChangeInterval, _ -> {
            if (isFishingAnimation && GameConfig.FISHES != null && GameConfig.FISHES.length > 0) {
                int randomIndex = (int)(Math.random() * GameConfig.FISHES.length);
                currentFishingFishImage = GameConfig.FISHES[randomIndex].imagePath;
                if (getContentPane() != null) {
                    getContentPane().repaint();
                }
            }
        });
        fishingAnimationTimer.start();
        
        if (getContentPane() != null) {
            getContentPane().repaint();
        }
    }
    
    public void stopFishingAnimation() {
        isFishingAnimation = false;
        currentFishingFishImage = null;
        if (fishingAnimationTimer != null) {
            fishingAnimationTimer.stop();
            fishingAnimationTimer = null;
        }
        if (getContentPane() != null) {
            getContentPane().repaint();
        }
    }

    private void initializeWindow() {
        setTitle("Popup Window");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        setSize(config.width, config.height);
        try { SoundManager.getInstance().playSFX(GameConfig.LOCATION_OPEN_SOUND); } catch (Exception ignored) {}
        
     
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopFishingAnimation();
                try { SoundManager.getInstance().playSFX(GameConfig.LOCATION_CLOSE_SOUND); } catch (Exception ignored) {}
                dispose();
            }
        });
    }

    private void loadBackgroundImage() {
        if (config.useBackgroundImage && config.backgroundImagePath != null) {
            try {
                String fullPath = System.getProperty("user.dir") + File.separator + config.backgroundImagePath;
                File imageFile = new File(fullPath);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                }
            } catch (Exception ex) {
                System.err.println("Error loading background image: " + ex.getMessage());
            }
        }
    }

    private void createContentPanel() {
        ContentPanel panel = new ContentPanel();
        setContentPane(panel);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private class ContentPanel extends JPanel {
        private int lastMouseX = -1;
        private int lastMouseY = -1;
        private BufferedImage tooltipBg;
        public ContentPanel() {
            setPreferredSize(new Dimension(config.width, config.height));
            setLayout(null);
            
            for (MenuElement el : elements) {
                if (el.getText() != null && (el.getText().equals(".......") || el.getText().equals("   "))) {
                    answerField = new JTextField("");
                    answerField.setBounds((int)el.getX() - 10, (int)el.getY() - 10, 250, 35);
                    answerField.setFont(FontManager.getThaiFont(Font.PLAIN, 24));
                    answerField.setOpaque(false);
                    answerField.setBorder(null);
                    answerField.setForeground(new Color(0, 0, 0));
                    
                    answerField.addFocusListener(new java.awt.event.FocusAdapter() {
                        @Override
                        public void focusGained(java.awt.event.FocusEvent e) {
                            // Clear on focus
                        }
                        
                        @Override
                        public void focusLost(java.awt.event.FocusEvent e) {
                            // Keep text
                        }
                    });
                    
                    answerField.addActionListener(_ -> {
                        try { SoundManager.getInstance().playSFX(GameConfig.STAMP_SOUND); } catch (Exception ignored) {}
                        if (questionHandler != null) {
                            String text = answerField.getText();
                            if (!text.trim().isEmpty()) {
                                questionHandler.onAnswerSubmit(text);
                            }
                        }
                    });
                    
                    add(answerField);
                    
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        answerField.requestFocusInWindow();
                    });
                } else if (el.getText() != null && el.getText().equals("คำถาม")) {
                    questionTextElement = el;
                }
            }

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });

            addMouseMotionListener(new java.awt.event.MouseMotionListener() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    handleHover(e.getX(), e.getY());
                }

                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    handleHover(e.getX(), e.getY());
                }
            });

            
            for (MenuElement el : elements) {
                String path = el.getImagePath();
                if (path != null) {
                    String normalizedPath = path.replace('\\', '/');
                    if (normalizedPath.endsWith("assets/ui/popup/TEMP_Exit_button.png")) {
                        el.setHoverImage("assets" + java.io.File.separator + "ui" + java.io.File.separator + "popup" + java.io.File.separator + "TEMP_exit_button_hover.png");
                        el.setButtonId("popup_exit");
                    } else if (normalizedPath.endsWith("assets/ui/popup/Furniture_Fridge.png") || 
                               normalizedPath.endsWith("assets/ui/popup/Furniture_KnivesWall.png")) {
                       
                        el.setUseScaleEffect(true);
                    }
                }
            }

         
            try {
                String tipPath = "assets" + File.separator + "ui" + File.separator + "Input-Field-Small-White_0.png";
                String full = System.getProperty("user.dir") + File.separator + tipPath;
                File f = new File(full);
                if (f.exists()) {
                    tooltipBg = ImageIO.read(f);
                }
            } catch (Exception ignored) {}
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (config.useBackgroundImage && backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, config.width, config.height, null);
            } else {
                g2d.setColor(config.backgroundColor);
                g2d.fillRect(0, 0, config.width, config.height);
            }

            for (MenuElement element : elements) {
                element.render(g2d);
            }

 
            if (lastMouseX >= 0 && lastMouseY >= 0) {
                MenuElement hoveredWithTooltip = null;
                for (int i = elements.size() - 1; i >= 0; i--) {
                    MenuElement el = elements.get(i);
                    if (el.isHovered() && el.getTooltip() != null && !el.getTooltip().isEmpty()) {
                        hoveredWithTooltip = el;
                        break;
                    }
                }
                if (hoveredWithTooltip != null) {
                    String tip = hoveredWithTooltip.getTooltip();
                    Font original = g2d.getFont();
                    g2d.setFont(FontManager.getThaiFont(Font.PLAIN, 18));
                    FontMetrics fm = g2d.getFontMetrics();
                    int paddingX = 18;
                    int paddingY = 10;
                    int tw = fm.stringWidth(tip) + paddingX * 2;
                    int th = fm.getHeight() + paddingY * 2;
                    int tx = lastMouseX + 18;
                    int ty = lastMouseY + 18;
                    if (tx + tw > config.width) tx = config.width - tw - 10;
                    if (ty + th > config.height) ty = config.height - th - 10;
                    if (tooltipBg != null) {
                        g2d.drawImage(tooltipBg, tx, ty, tw, th, null);
                    } else {
                        g2d.setColor(new Color(255, 255, 255, 230));
                        g2d.fillRoundRect(tx, ty, tw, th, 20, 20);
                        g2d.setColor(new Color(60, 60, 80, 180));
                        g2d.drawRoundRect(tx, ty, tw, th, 20, 20);
                    }
                    g2d.setColor(new Color(20, 20, 30));
                    g2d.drawString(tip, tx + paddingX, ty + paddingY + fm.getAscent());
                    g2d.setFont(original);
                }
            }

            long now = System.currentTimeMillis();
            
            if (currentNotify != null) {
                if (now - notifyStartMs >= notifyDurationMs) {
                    currentNotify = notifyQueue.poll();
                    notifyStartMs = System.currentTimeMillis();
                }
            }
            if (currentNotify != null && currentNotify.message != null) {
                Font old = g2d.getFont();
                g2d.setFont(FontManager.getThaiFont(Font.BOLD, 32));
                
                int imageSize = 64;
                int x = (int)91.0;
                int y = (int)836.0;
                int textX = x;
                
                if (currentNotify.imagePath != null) {
                    try {
                        String imgPath = currentNotify.imagePath;
                        File imgFile = new File(imgPath);
                        if (!imgFile.exists()) {
                            imgFile = new File(System.getProperty("user.dir") + File.separator + imgPath);
                        }
                        if (imgFile.exists()) {
                            BufferedImage fishImg = ImageIO.read(imgFile);
                            if (fishImg != null) {
                                g2d.drawImage(fishImg, x, y - imageSize - 5, imageSize, imageSize, null);
                                textX = x + imageSize + 10;
                            }
                        }
                    } catch (Exception ignored) {}
                } else if (isFishingAnimation && currentFishingFishImage != null && currentNotify.message.contains("กำลังตกปลา")) {
                    try {
                        String imgPath = currentFishingFishImage;
                        File imgFile = new File(imgPath);
                        if (!imgFile.exists()) {
                            imgFile = new File(System.getProperty("user.dir") + File.separator + imgPath);
                        }
                        if (imgFile.exists()) {
                            BufferedImage fishImg = ImageIO.read(imgFile);
                            if (fishImg != null) {
                                g2d.drawImage(fishImg, x, y - imageSize - 5, imageSize, imageSize, null);
                                textX = x + imageSize + 10;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                
                String text = currentNotify.message;
                g2d.setColor(new Color(0, 0, 0, 160));
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        g2d.drawString(text, textX + dx, y + dy);
                    }
                }
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawString(text, textX, y);
                g2d.setFont(old);
            }
        }

        private void handleHover(int mx, int my) {
            boolean anyHover = false;
            lastMouseX = mx;
            lastMouseY = my;
            for (int i = elements.size() - 1; i >= 0; i--) {
                MenuElement el = elements.get(i);
                boolean h = el.contains(mx, my);
                boolean was = el.isHovered();
                el.setHovered(h);
                if (h && !was) {
                    try { SoundManager.getInstance().playSFX(GameConfig.HOVER_SOUND); } catch (Exception ignored) {}
                }
                if (h) {
                    anyHover = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    break;
                }
            }
            if (!anyHover) {
                setCursor(Cursor.getDefaultCursor());
            }
            repaint();
        }

        private void handleClick(int mx, int my) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                MenuElement el = elements.get(i);
                if (el.contains(mx, my)) {
                    String path = el.getImagePath();
                    boolean isExit = (el.getButtonId() != null && el.getButtonId().equals("popup_exit"))
                            || (path != null && path.replace('\\','/').endsWith("assets/ui/popup/TEMP_Exit_button.png"));
                    if (isExit) {
                        stopFishingAnimation();
                        try { SoundManager.getInstance().playSFX(GameConfig.LOCATION_CLOSE_SOUND); } catch (Exception ignored) {}
                        SwingUtilities.getWindowAncestor(this).dispose();
                        return;
                    }
                    if (purchaseHandler != null && path != null) {
                        String p = path.replace('\\','/');
                        if (p.endsWith("assets/ui/popup/Icon-Cluckers-Bucket #2969.png")) {
                            purchaseHandler.onPurchase("bucket");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Cluckers-Fries #2994.png")) {
                            purchaseHandler.onPurchase("fries");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Cluckers-Burger #2907.png")) {
                            purchaseHandler.onPurchase("burger");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Cluckers-Shake.png")) {
                            purchaseHandler.onPurchase("shake");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Deposit-500.png")) {
                            purchaseHandler.onPurchase("deposit500");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Deposit-All.png")) {
                            purchaseHandler.onPurchase("depositAll");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Withdraw-500 #42864.png")) {
                            purchaseHandler.onPurchase("withdraw500");
                            return;
                        } else if (p.endsWith("assets/ui/popup/Icon-Withdraw-All #45109.png")) {
                            purchaseHandler.onPurchase("withdrawAll");
                            return;
                        } else if (p.contains("Icon-Fishing-Pole")) {
                            try { SoundManager.getInstance().playSFX(GameConfig.BUTTON_CLICK_2_SOUND); } catch (Exception ignored) {}
                            purchaseHandler.onPurchase("fishing");
                            return;
                        } else if (p.contains("Icon-Furniture-Bed_Mattress")) {
                            try { SoundManager.getInstance().playSFX(GameConfig.BUTTON_CLICK_2_SOUND); } catch (Exception ignored) {}
                            purchaseHandler.onPurchase("sleep");
                            return;
                        } else if (p.contains("Icon-Chamber-Study-Old")) {
                            try { SoundManager.getInstance().playSFX(GameConfig.STAMP_SOUND); } catch (Exception ignored) {}
                            if (questionHandler != null && answerField != null) {
                                String text = answerField.getText();
                                if (!text.trim().isEmpty()) {
                                    questionHandler.onAnswerSubmit(text);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    public static class PopupWindowConfig {
        public int width = 800;
        public int height = 600;
        public Color backgroundColor = new Color(240, 240, 240);
        public String backgroundImagePath = null;
        public boolean useBackgroundImage = false;
    }
}

