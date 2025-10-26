
package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import editor.EditorManager;

public class GameWindow extends JFrame {
    private static final String TITLE = "No Brakes Life";
    
    private GamePanel gamePanel;
    
    public GameWindow() {
        initializeWindow();
        createGamePanel();
        pack();
        centerWindow();
    }
    
    private void initializeWindow() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
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
            
            startAnimationLoop();
        }
        
        private void startAnimationLoop() {
            javax.swing.Timer timer = new javax.swing.Timer(16, _ -> {
                if (scene.getPlayer().isMoving()) {
                    repaint();
                }
            });
            timer.start();
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
    }
}

class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private EditorManager editorManager;
    private GameWindow.GamePanel panel;
    private boolean isDragging;
    private boolean isPressed;
    
    public MouseHandler(EditorManager editorManager, GameWindow.GamePanel panel) {
        this.editorManager = editorManager;
        this.panel = panel;
        this.isDragging = false;
        this.isPressed = false;
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        isPressed = true;
        panel.setCursor(CursorManager.getPressCursor());
        editorManager.handleMousePressed(e);
        if (editorManager.isWaypointMode()) {
            panel.repaint();
        } else {
            panel.repaint();
        }
    }
    
    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        isDragging = true;
        panel.setCursor(CursorManager.getPressCursor());
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
        
        if (isPressed || isDragging) {
            panel.setCursor(CursorManager.getPressCursor());
        } else {
            panel.setCursor(CursorManager.getNormalCursor());
        }
        
        panel.repaint();
    }
    
    @Override
    public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
        editorManager.handleMouseWheel(e);
        panel.repaint();
    }
}

