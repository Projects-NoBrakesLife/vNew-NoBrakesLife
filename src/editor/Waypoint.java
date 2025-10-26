package editor;

import java.awt.Point;

public class Waypoint {
    private Point position;
    private int id;
    
    public Waypoint(double x, double y) {
        this.position = new Point((int)x, (int)y);
        this.id = (int)(Math.random() * 1000000);
    }
    
    public Waypoint(double x, double y, int id) {
        this.position = new Point((int)x, (int)y);
        this.id = id;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public double getX() {
        return position.getX();
    }
    
    public double getY() {
        return position.getY();
    }
    
    public int getId() {
        return id;
    }
    
    public void setPosition(double x, double y) {
        this.position.setLocation(x, y);
    }
    
    public boolean contains(int x, int y) {
        double distance = Math.sqrt(Math.pow(x - position.x, 2) + Math.pow(y - position.y, 2));
        return distance <= 10;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Waypoint waypoint = (Waypoint) obj;
        return Math.abs(this.position.x - waypoint.position.x) < 1.0 &&
               Math.abs(this.position.y - waypoint.position.y) < 1.0;
    }
    
    @Override
    public String toString() {
        return "Waypoint(" + position.x + ", " + position.y + ")";
    }
}

