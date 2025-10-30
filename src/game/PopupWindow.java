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
    private PurchaseHandler purchaseHandler;
    private java.util.ArrayDeque<String> notifyQueue = new java.util.ArrayDeque<>();
    private String currentNotify;
    private long notifyStartMs = 0L;
    private final int notifyDurationMs = 2500;

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
        super((Frame) null, true);
        this.config = config;
        this.elements = new ArrayList<>(elements);
        this.purchaseHandler = handler;
        initializeWindow();
        loadBackgroundImage();
        createContentPanel();
        pack();
        centerWindow();
    }

    public void showNotification(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        notifyQueue.offer(msg);
        if (currentNotify == null) {
            currentNotify = notifyQueue.poll();
            notifyStartMs = System.currentTimeMillis();
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
            if (currentNotify != null) {
                Font old = g2d.getFont();
                g2d.setFont(FontManager.getThaiFont(Font.BOLD, 32));
                int x = (int)91.0;
                int y = (int)836.0;
                String text = currentNotify;
                g2d.setColor(new Color(0, 0, 0, 160));
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        g2d.drawString(text, x + dx, y + dy);
                    }
                }
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawString(text, x, y);
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
                el.setHovered(h);
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

