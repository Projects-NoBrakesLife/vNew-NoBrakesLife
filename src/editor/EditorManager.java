package editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import game.GameObject;

import java.util.List;

public class EditorManager {
    private ArrayList<GameObject> objects;
    private ArrayList<ArrayList<Waypoint>> waypointPaths;
    private int currentPathIndex;
    private GameObject selectedObject;
    private GameObject draggedObject;
    private Point dragOffset;
    private boolean waypointMode;
    
    public EditorManager() {
        objects = new ArrayList<>();
        waypointPaths = new ArrayList<>();
        currentPathIndex = 0;
        waypointMode = false;
    }
    
    public void addObject(GameObject obj) {
        objects.add(obj);
    }
    
    public void removeObject(GameObject obj) {
        objects.remove(obj);
        if (selectedObject == obj) {
            selectedObject = null;
        }
        if (draggedObject == obj) {
            draggedObject = null;
        }
    }
    
    public void handleMousePressed(MouseEvent e) {
        Point p = e.getPoint();
        
        if (waypointMode && e.getButton() == MouseEvent.BUTTON1) {
            addWaypoint(p.x, p.y);
        } else {
            GameObject clicked = findObjectAt(p.x, p.y);
            
            if (clicked != null) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    selectedObject = clicked;
                    draggedObject = clicked;
                    
                    double objX = clicked.getX();
                    double objY = clicked.getY();
                    dragOffset = new Point((int) (p.x - objX), (int) (p.y - objY));
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    removeObject(clicked);
                }
            } else {
                selectedObject = null;
            }
        }
    }
    
    public void handleMouseDragged(MouseEvent e) {
        if (draggedObject == null) {
            return;
        }
        
        int mx = e.getX();
        int my = e.getY();
        
        draggedObject.setX(mx - dragOffset.x);
        draggedObject.setY(my - dragOffset.y);
    }
    
    public void handleMouseWheel(MouseWheelEvent e) {
        if (selectedObject != null) {
            int rotation = e.getWheelRotation();
            double currentSize = selectedObject.getWidth();
            double newSize = Math.max(20, currentSize - rotation * 10);
            selectedObject.setWidth(newSize);
            selectedObject.setHeight(newSize);
        }
    }
    
    public void handleMouseReleased(MouseEvent e) {
        draggedObject = null;
    }
    
    public void render(Graphics2D g2d) {
        for (GameObject obj : objects) {
            obj.render(g2d);
        }
        
        for (int pathIndex = 0; pathIndex < waypointPaths.size(); pathIndex++) {
            ArrayList<Waypoint> path = waypointPaths.get(pathIndex);
            Color pathColor = pathIndex == currentPathIndex ? Color.RED : Color.ORANGE;
            
            for (int i = 0; i < path.size(); i++) {
                Waypoint wp = path.get(i);
                renderWaypoint(g2d, wp, i, pathColor);
            }
            
            if (path.size() > 1) {
                renderPaths(g2d, path);
            }
        }
        
        if (selectedObject != null) {
            renderSelectionBox(g2d, selectedObject);
        }
    }
    
    private void renderWaypoint(Graphics2D g2d, Waypoint wp, int index, Color pathColor) {
        int x = (int)wp.getX();
        int y = (int)wp.getY();
        
        g2d.setColor(pathColor);
        g2d.fillOval(x - 5, y - 5, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - 5, y - 5, 10, 10);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String label = String.valueOf(index);
        int labelWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, x - labelWidth / 2, y - 8);
    }
    
    private void renderPaths(Graphics2D g2d, ArrayList<Waypoint> path) {
        g2d.setColor(new Color(255, 200, 0, 150));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (int i = 0; i < path.size() - 1; i++) {
            Waypoint wp1 = path.get(i);
            Waypoint wp2 = path.get(i + 1);
            g2d.drawLine((int)wp1.getX(), (int)wp1.getY(), (int)wp2.getX(), (int)wp2.getY());
        }
    }
    
    private void renderSelectionBox(Graphics2D g2d, GameObject obj) {
        double x = obj.getX();
        double y = obj.getY();
        double w = obj.getWidth();
        double h = obj.getHeight();
        
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLUE);
        g2d.drawRect((int) x, (int) y, (int) w, (int) h);
    }
    
    
    private GameObject findObjectAt(int x, int y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (objects.get(i).contains(x, y)) {
                return objects.get(i);
            }
        }
        return null;
    }
    
    public GameObject getSelectedObject() {
        return selectedObject;
    }
    
    public ArrayList<GameObject> getObjects() {
        return objects;
    }
    
    public void addWaypoint(double x, double y) {
        if (waypointPaths.isEmpty()) {
            waypointPaths.add(new ArrayList<>());
            currentPathIndex = 0;
        }
        waypointPaths.get(currentPathIndex).add(new Waypoint(x, y));
        System.out.println("Waypoint added at: " + x + ", " + y + " in path " + currentPathIndex);
    }
    
    public void removeWaypointAt(int x, int y) {
        for (ArrayList<Waypoint> path : waypointPaths) {
            path.removeIf(wp -> wp.contains(x, y));
        }
    }
    
    public ArrayList<ArrayList<Waypoint>> getWaypointPaths() {
        return waypointPaths;
    }
    
    public void clearWaypoints() {
        waypointPaths.clear();
        currentPathIndex = 0;
    }
    
    public void startNewPath() {
        waypointPaths.add(new ArrayList<>());
        currentPathIndex = waypointPaths.size() - 1;
        System.out.println("Started new path: " + currentPathIndex);
    }
    
    public int getCurrentPathIndex() {
        return currentPathIndex;
    }
    
    public void setWaypointMode(boolean enabled) {
        this.waypointMode = enabled;
    }
    
    public boolean isWaypointMode() {
        return waypointMode;
    }
}
