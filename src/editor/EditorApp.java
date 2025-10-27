package editor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import game.GameWindow;
import game.GameObject;
import game.FontManager;
import javax.swing.plaf.FontUIResource;

public class EditorApp {
    private GameWindow window;
    private GameWindow.GamePanel gamePanel;
    private boolean positionMode;
    
    public EditorApp() {
        window = new GameWindow();
        gamePanel = window.getGamePanel();
        this.positionMode = false;
    }
    
    public void start() {
        SwingUtilities.invokeLater(() -> {
            setUIFont();
            createMenuBar();
            window.setVisible(true);
            gamePanel.setEditorApp(this);
        });
    }
    
    private void setUIFont() {
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
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        Font buttonFont = FontManager.getThaiFont(12);
        Font menuFont = FontManager.getThaiFont(12);
        
        JButton rotLeftBtn = new JButton("↺ Rotate Left");
        rotLeftBtn.setFont(buttonFont);
        rotLeftBtn.addActionListener(_ -> {
            GameObject obj = gamePanel.getEditorManager().getSelectedObject();
            if (obj != null) {
                obj.setRotation(obj.getRotation() - 15);
                gamePanel.repaint();
            }
        });
        
        JButton rotRightBtn = new JButton("Rotate Right ↻");
        rotRightBtn.setFont(buttonFont);
        rotRightBtn.addActionListener(_ -> {
            GameObject obj = gamePanel.getEditorManager().getSelectedObject();
            if (obj != null) {
                obj.setRotation(obj.getRotation() + 15);
                gamePanel.repaint();
            }
        });
        
        JButton resetRotBtn = new JButton("Reset Rotation");
        resetRotBtn.setFont(buttonFont);
        resetRotBtn.addActionListener(_ -> {
            GameObject obj = gamePanel.getEditorManager().getSelectedObject();
            if (obj != null) {
                obj.setRotation(0);
                gamePanel.repaint();
            }
        });
        
        JButton addWaypointBtn = new JButton("Toggle Waypoint Mode");
        addWaypointBtn.setFont(buttonFont);
        addWaypointBtn.addActionListener(_ -> handleToggleWaypointMode());
        
        JButton exportWaypointBtn = new JButton("Export Waypoints");
        exportWaypointBtn.setFont(buttonFont);
        exportWaypointBtn.addActionListener(_ -> handleExportWaypoints());
        
        JButton clearWaypointsBtn = new JButton("Clear Waypoints");
        clearWaypointsBtn.setFont(buttonFont);
        clearWaypointsBtn.addActionListener(_ -> handleClearWaypoints());
        
        JButton newPathBtn = new JButton("New Path");
        newPathBtn.setFont(buttonFont);
        newPathBtn.addActionListener(_ -> handleNewPath());
        
        JButton positionModeBtn = new JButton("Position Mode");
        positionModeBtn.setFont(buttonFont);
        positionModeBtn.addActionListener(_ -> handlePositionMode());
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(menuFont);
        JMenuItem addItem = new JMenuItem("Add Object");
        addItem.setFont(menuFont);
        addItem.addActionListener(_ -> handleAddObject());
        JMenuItem exportItem = new JMenuItem("Export Scene");
        exportItem.setFont(menuFont);
        exportItem.addActionListener(_ -> handleExportScene());
        JMenuItem loadItem = new JMenuItem("Load Scene");
        loadItem.setFont(menuFont);
        loadItem.addActionListener(_ -> handleLoadScene());
        JMenuItem clearItem = new JMenuItem("Clear All");
        clearItem.setFont(menuFont);
        clearItem.addActionListener(_ -> handleClearAll());
        
        fileMenu.add(addItem);
        fileMenu.addSeparator();
        fileMenu.add(loadItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(clearItem);
        
        menuBar.add(rotLeftBtn);
        menuBar.add(rotRightBtn);
        menuBar.add(resetRotBtn);
        menuBar.add(Box.createHorizontalStrut(20));
        menuBar.add(addWaypointBtn);
        menuBar.add(newPathBtn);
        menuBar.add(exportWaypointBtn);
        menuBar.add(clearWaypointsBtn);
        menuBar.add(Box.createHorizontalStrut(20));
        menuBar.add(positionModeBtn);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(fileMenu);
        window.setJMenuBar(menuBar);
    }
    
    private void handleAddObject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image File");
        
        String projectRoot = System.getProperty("user.dir");
        fileChooser.setCurrentDirectory(new java.io.File(projectRoot));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
            }
            
            @Override
            public String getDescription() {
                return "Image Files (*.png, *.jpg, *.jpeg, *.gif)";
            }
        });
        
        int result = fileChooser.showOpenDialog(window);
        if (result == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            GameObject obj = new GameObject(imagePath, 400, 300, 0, 0);
            gamePanel.getEditorManager().addObject(obj);
            gamePanel.repaint();
        }
    }
    
    private void handleExportScene() {
        StringBuilder code = new StringBuilder();
        code.append("ArrayList<GameObjectData> sceneData = new ArrayList<>();\n");
        
        for (GameObject obj : gamePanel.getEditorManager().getObjects()) {
            code.append(String.format(
                "sceneData.add(new GameObjectData(\"%s\", %.1f, %.1f, %.1f, %.1f, %.1f));\n",
                obj.getImage() != null ? "image.png" : "null",
                obj.getX(),
                obj.getY(),
                obj.getWidth(),
                obj.getHeight(),
                obj.getRotation()
            ));
        }
        
        code.append("\nfor (GameObjectData data : sceneData) {\n");
        code.append("    GameObject obj = new GameObject(data.getImagePath(), data.getX(), data.getY(), data.getWidth(), data.getHeight());\n");
        code.append("    obj.setRotation(data.getRotation());\n");
        code.append("    objects.add(obj);\n");
        code.append("}\n");
        
        JTextArea textArea = new JTextArea(code.toString());
        textArea.setFont(FontManager.getThaiFont(12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        int result = JOptionPane.showConfirmDialog(
            window, 
            scrollPane, 
            "Exported Scene Code", 
            JOptionPane.OK_CANCEL_OPTION
        );
        
        if (result == JOptionPane.OK_OPTION) {
            new SceneExporter().exportToClipboard(code.toString());
            JOptionPane.showMessageDialog(window, "Code copied to clipboard!");
        }
    }
    
    private void handleLoadScene() {
        String code = JOptionPane.showInputDialog(
            window,
            "Paste exported scene code:",
            "Load Scene",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (code != null && !code.isEmpty()) {
            new SceneLoader().loadFromCode(code, gamePanel.getEditorManager());
            gamePanel.repaint();
        }
    }
    
    private void handleClearAll() {
        int result = JOptionPane.showConfirmDialog(
            window,
            "Are you sure you want to clear all objects?",
            "Clear All",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            gamePanel.getEditorManager().getObjects().clear();
            gamePanel.repaint();
        }
    }
    
    private boolean waypointModeEnabled = false;
    
    private void handleToggleWaypointMode() {
        waypointModeEnabled = !waypointModeEnabled;
        gamePanel.getEditorManager().setWaypointMode(waypointModeEnabled);
        if (waypointModeEnabled) {
            JOptionPane.showMessageDialog(window, "Waypoint Mode: ON\nClick anywhere to add waypoints");
        } else {
            JOptionPane.showMessageDialog(window, "Waypoint Mode: OFF");
        }
        gamePanel.repaint();
    }
    
    private void handleExportWaypoints() {
        StringBuilder code = new StringBuilder();
        code.append("ArrayList<ArrayList<Waypoint>> allPaths = new ArrayList<>();\n\n");
        
        int pathIndex = 0;
        for (ArrayList<Waypoint> path : gamePanel.getEditorManager().getWaypointPaths()) {
            code.append(String.format("ArrayList<Waypoint> path%d = new ArrayList<>();\n", pathIndex));
            for (int i = 0; i < path.size(); i++) {
                Waypoint wp = path.get(i);
                code.append(String.format("path%d.add(new Waypoint(%.1f, %.1f)); // %d\n", 
                    pathIndex, wp.getX(), wp.getY(), i));
            }
            code.append(String.format("allPaths.add(path%d);\n\n", pathIndex));
            pathIndex++;
        }
        
        code.append("return allPaths;\n");
        
        JTextArea textArea = new JTextArea(code.toString());
        textArea.setFont(FontManager.getThaiFont(12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        int result = JOptionPane.showConfirmDialog(
            window,
            scrollPane,
            "Exported Waypoints",
            JOptionPane.OK_CANCEL_OPTION
        );
        
        if (result == JOptionPane.OK_OPTION) {
            new SceneExporter().exportToClipboard(code.toString());
            JOptionPane.showMessageDialog(window, "Code copied to clipboard!");
        }
    }
    
    private void handleClearWaypoints() {
        int result = JOptionPane.showConfirmDialog(
            window,
            "Are you sure you want to clear all waypoints?",
            "Clear Waypoints",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            gamePanel.getEditorManager().clearWaypoints();
            gamePanel.repaint();
        }
    }
    
    private void handleNewPath() {
        gamePanel.getEditorManager().startNewPath();
        JOptionPane.showMessageDialog(window, "New path started. Current path: " + gamePanel.getEditorManager().getCurrentPathIndex());
        gamePanel.repaint();
    }
    
    private void handlePositionMode() {
        positionMode = !positionMode;
        gamePanel.getEditorManager().setPositionMode(positionMode);
        if (positionMode) {
            JOptionPane.showMessageDialog(window, "Position Mode: ON\nClick on screen to copy X, Y coordinates");
        } else {
            JOptionPane.showMessageDialog(window, "Position Mode: OFF");
        }
        gamePanel.repaint();
    }
    
    public void handlePositionClick(int x, int y) {
        if (positionMode) {
            String coords = String.format("%.1f, %.1f", (double)x, (double)y);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(coords), null);
            JOptionPane.showMessageDialog(window, "Coordinates copied!\n" + coords);
            positionMode = false;
            gamePanel.getEditorManager().setPositionMode(false);
        }
    }
}
