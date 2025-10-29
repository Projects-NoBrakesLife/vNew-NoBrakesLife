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

    public PopupWindow(JFrame parent, PopupWindowConfig config, ArrayList<MenuElement> elements) {
        super(parent, true); // modal dialog
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
        
        initializeWindow();
        loadBackgroundImage();
        createContentPanel();
        pack();
        centerWindow();
    }

    private void initializeWindow() {
        setTitle("Popup Window");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        setSize(config.width, config.height);
        
        // ปิดเมื่อกด ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        }

        private void handleHover(int mx, int my) {
            boolean anyHover = false;
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
                        SwingUtilities.getWindowAncestor(this).dispose();
                        return;
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

