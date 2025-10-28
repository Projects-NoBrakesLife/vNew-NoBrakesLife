
package game;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import editor.EditorManager;
import editor.EditorApp;

public class GameWindow extends JFrame {
    private static final String TITLE = "No Brakes Life";
    
    private GamePanel gamePanel;
    private GameWindow window;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameWindow().setVisible(true);
         
        });
    }
    
    public GameWindow() {
        initializeWindow();
        createGamePanel();
        setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setMinimumSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setMaximumSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        pack();

        centerWindow();

    }
    
    private void initializeWindow() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);


        addDragSupport();
        JMenuBar menuBar = new JMenuBar();
        Font thaiFont = FontManager.getThaiFont(12);
        FontUIResource fontResource = new FontUIResource(thaiFont);
        UIManager.put("OptionPane.font", fontResource);
        UIManager.put("FileChooser.font", fontResource);
        UIManager.put("Label.font", fontResource);
        UIManager.put("Button.font", fontResource);
        UIManager.put("Menu.font", fontResource);
        UIManager.put("MenuItem.font", fontResource);
        UIManager.put("TextField.font", fontResource);
        UIManager.put("TextArea.font", fontResource);
        setJMenuBar(menuBar);
        setupToolbar();
    }

    private void setupToolbar() {
        JToolBar toolBar = new JToolBar();
 
        toolBar.addSeparator();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.setJMenuBar(new JMenuBar());
                frame.getJMenuBar().add(toolBar);
            }
        });
    }
    
    private void addDragSupport() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dispose();
                System.exit(0);
                return true;
            }
            return false;
        });
    }
    
    private void createGamePanel() {
        gamePanel = new GamePanel();
        add(gamePanel);
    }
    
    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }
    
    public GamePanel getGamePanel() {
        return gamePanel;
    }
    
    public static class GamePanel extends JPanel {
        private BackgroundManager backgroundManager;
        private EditorManager editorManager;
        private GameScene scene;
        private MouseHandler handler;
        
        public GamePanel() {
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            setDoubleBuffered(true);
            
            backgroundManager = new BackgroundManager();
            editorManager = new EditorManager();
            scene = new GameScene();
            
            loadGameScene();
            
            setCursor(CursorManager.getNormalCursor());
            
            MouseHandler handler = new MouseHandler(editorManager, this);
            addMouseListener(handler);
            addMouseMotionListener(handler);
            addMouseWheelListener(handler);
            
            this.handler = handler;
            
            addDragSupport();
            
            startAnimationLoop();
        }
        
        private void addDragSupport() {
            final int[] point = new int[2];
            final boolean[] isDraggingWindow = {false};
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    isDraggingWindow[0] = true;
                    point[0] = e.getXOnScreen();
                    point[1] = e.getYOnScreen();
                }
                
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    isDraggingWindow[0] = false;
                }
            });
            
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    if (isDraggingWindow[0]) {
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(GamePanel.this);
                        if (frame != null) {
                            int newX = frame.getX() + e.getXOnScreen() - point[0];
                            int newY = frame.getY() + e.getYOnScreen() - point[1];
                            point[0] = e.getXOnScreen();
                            point[1] = e.getYOnScreen();
                            frame.setLocation(newX, newY);
                        }
                    }
                }
            });
        }
        
        private void startAnimationLoop() {
            javax.swing.Timer timer = new javax.swing.Timer(16, _ -> {
                if (scene.getPlayer().isMoving()) {
                    repaint();
                }
            });
            timer.start();
            
            javax.swing.Timer renderTimer = new javax.swing.Timer(50, _ -> {
                repaint();
            });
            renderTimer.start();
        }
        
        private void loadGameScene() {
            for (GameConfig.HoverObject config : GameConfig.HOVER_OBJECTS) {
                GameObject obj = new GameObject(config.imagePath, config.x, config.y, config.width, config.height);
                obj.setRotation(config.rotation);
                scene.addHoverObject(obj, config.name);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            backgroundManager.render(g2d, getWidth(), getHeight());
            editorManager.render(g2d);
            scene.render(g2d);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(FontManager.getThaiFont(Font.BOLD, 16));
            Point mouse = scene.getMousePosition();
            String debugText = "X: " + mouse.x + " Y: " + mouse.y;
            g2d.drawString(debugText, 10, 30);
            
            g2d.setColor(new Color(255, 255, 255, 255));
            g2d.setFont(FontManager.getThaiFont(26));
            String versionText = "v " + GitVersion.getVersion();
            g2d.drawString(versionText, 20, getHeight() - 20);
            
            updateCursor();
        }
        
        private void updateCursor() {
            if (handler != null && handler.isPressed()) {
                setCursor(CursorManager.getPressCursor());
            } else {
                setCursor(CursorManager.getNormalCursor());
            }
        }
        
        public BackgroundManager getBackgroundManager() {
            return backgroundManager;
        }
        
        public EditorManager getEditorManager() {
            return editorManager;
        }
        
        public GameScene getScene() {
            return scene;
        }
        
        public void setEditorApp(EditorApp editorApp) {
            if (handler != null) {
                handler.setEditorApp(editorApp);
            }
        }
    }
}

class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private EditorManager editorManager;
    private GameWindow.GamePanel panel;
    private boolean isDragging;
    private boolean isPressed;
    private EditorApp editorApp;
    
    public MouseHandler(EditorManager editorManager, GameWindow.GamePanel panel) {
        this.editorManager = editorManager;
        this.panel = panel;
        this.isDragging = false;
        this.isPressed = false;
    }
    
    public void setEditorApp(EditorApp editorApp) {
        this.editorApp = editorApp;
    }
    
    public boolean isPressed() {
        return isPressed;
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        isPressed = true;
        editorManager.handleMousePressed(e);
        
        if (editorManager.isPositionMode() && editorApp != null) {
            editorApp.handlePositionClick(e.getX(), e.getY());
        }
        
        panel.repaint();
    }
    
    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        isDragging = true;
        editorManager.handleMouseDragged(e);
        panel.repaint();
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        isPressed = false;
        isDragging = false;
        editorManager.handleMouseReleased(e);
        panel.repaint();
    }
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        panel.getScene().handleClick(e.getX(), e.getY());
        panel.repaint();
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {}
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        panel.setCursor(CursorManager.getNormalCursor());
    }
    
    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        panel.getScene().updateMousePosition(e.getX(), e.getY());
        panel.repaint();
    }
    
    @Override
    public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
        editorManager.handleMouseWheel(e);
        panel.repaint();
    }
}

