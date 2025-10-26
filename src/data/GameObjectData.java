package data;

public class GameObjectData {
    private String imagePath;
    private double x;
    private double y;
    private double width;
    private double height;
    private double rotation;
    
    public GameObjectData(String imagePath, double x, double y, double width, double height, double rotation) {
        this.imagePath = imagePath;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public double getRotation() {
        return rotation;
    }
}

