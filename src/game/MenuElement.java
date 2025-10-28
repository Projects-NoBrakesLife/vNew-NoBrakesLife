package game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MenuElement {
    public enum ElementType {
        IMAGE,
        TEXT,
        BUTTON
    }
    
    public interface ButtonAction {
        void execute();
    }
    
    private ElementType type;
    private String imagePath;
    private BufferedImage image;
    private String text;
    private String buttonId;
    private ButtonAction buttonAction;
    private double x;
    private double y;
    private double width;
    private double height;
    private Color textColor;
    private Font textFont;
    private boolean selected;
    private boolean hovered;
    private BufferedImage hoverImage;
    private boolean useScaleEffect = false;
    private boolean trackMouse = false;
    private Point mousePosition = new Point(0, 0);
    private double currentAngle = 0;
    private double forcedAngle = Double.NaN;
    
    public MenuElement(ElementType type, String imagePath, double x, double y, double width, double height) {
        this.type = type;
        this.imagePath = imagePath;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.selected = false;
        this.hovered = false;
        this.textColor = Color.WHITE;
        this.text = "";
        this.buttonId = null;
        this.buttonAction = null;
        loadImage();
        loadHoverImage();
    }
    
    public MenuElement(ElementType type, String imagePath, double x, double y, double width, double height, String buttonId, ButtonAction action) {
        this.type = type;
        this.imagePath = imagePath;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.selected = false;
        this.hovered = false;
        this.textColor = Color.WHITE;
        this.text = "";
        this.buttonId = buttonId;
        this.buttonAction = action;
        loadImage();
        loadHoverImage();
    }
    
    public MenuElement(String text, double x, double y, int fontSize) {
        this.type = ElementType.TEXT;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = 0;
        this.height = 0;
        this.selected = false;
        this.textColor = Color.WHITE;
        this.textFont = FontManager.getThaiFont(Font.BOLD, fontSize);
        calculateDimensions();
    }
    
    private void loadImage() {
        if (imagePath == null || imagePath.isEmpty()) return;
        try {
            String fullPath = System.getProperty("user.dir") + File.separator + imagePath;
            File file = new File(fullPath);
            if (file.exists()) {
                image = ImageIO.read(file);
                if (width == 0 || height == 0) {
                    if (image != null) {
                        this.width = image.getWidth();
                        this.height = image.getHeight();
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
    
    private void loadHoverImage() {
        if (buttonId != null) {
            try {
                String hoverPath = System.getProperty("user.dir") + File.separator + "assets/ui/button/Button-Big-Pink.png";
                File file = new File(hoverPath);
                if (file.exists()) {
                    hoverImage = ImageIO.read(file);
                }
            } catch (Exception ex) {
            }
        }
    }
    
    public void setHoverImage(String hoverPath) {
        if (hoverPath == null) {
            hoverImage = null;
            return;
        }
        try {
            String fullPath = System.getProperty("user.dir") + File.separator + hoverPath;
            File file = new File(fullPath);
            if (file.exists()) {
                hoverImage = ImageIO.read(file);
            }
        } catch (Exception ex) {
        }
    }
    
    private void calculateDimensions() {
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tempImg.createGraphics();
        FontMetrics fm = g.getFontMetrics(textFont);
        this.width = fm.stringWidth(text);
        this.height = fm.getHeight();
        g.dispose();
    }
    
    public void render(Graphics2D g2d) {
        double scaleFactor = (hovered && useScaleEffect) ? 1.1 : 1.0;
        double scaledWidth = width * scaleFactor;
        double scaledHeight = height * scaleFactor;
        double offsetX = (width - scaledWidth) / 2;
        double offsetY = (height - scaledHeight) / 2;
        
        if (type == ElementType.IMAGE && image != null) {
            BufferedImage imgToDraw = useScaleEffect ? image : (hovered && hoverImage != null ? hoverImage : image);
            
            if (trackMouse && imgToDraw != null) {
                double centerX = x + width / 2;
                double centerY = y + height / 2;
                
                if (!Double.isNaN(forcedAngle)) {
                    double angle = forcedAngle;
                    currentAngle = Math.toDegrees(angle);
                    
                    AffineTransform oldTransform = g2d.getTransform();
                    g2d.translate(centerX, centerY);
                    g2d.rotate(angle);
                    g2d.drawImage(imgToDraw, (int)(-scaledWidth/2), (int)(-scaledHeight/2), (int)scaledWidth, (int)scaledHeight, null);
                    g2d.setTransform(oldTransform);
                } else {
                    g2d.drawImage(imgToDraw, (int)(x + offsetX), (int)(y + offsetY), (int)scaledWidth, (int)scaledHeight, null);
                }
            } else {
                g2d.drawImage(imgToDraw, (int)(x + offsetX), (int)(y + offsetY), (int)scaledWidth, (int)scaledHeight, null);
            }
        } else if (type == ElementType.TEXT) {
            g2d.setColor(textColor);
            Font scaledFont = textFont.deriveFont((float)(textFont.getSize() * scaleFactor));
            g2d.setFont(scaledFont);
            g2d.drawString(text, (int)(x - offsetX), (int)(y - offsetY));
        } else if (type == ElementType.BUTTON) {
            BufferedImage imgToDraw = useScaleEffect ? image : (hovered && hoverImage != null ? hoverImage : image);
            if (imgToDraw != null) {
                g2d.drawImage(imgToDraw, (int)(x + offsetX), (int)(y + offsetY), (int)scaledWidth, (int)scaledHeight, null);
            }
            if (text != null && !text.isEmpty()) {
                g2d.setColor(textColor);
                Font scaledFont = textFont.deriveFont((float)(textFont.getSize() * scaleFactor));
                g2d.setFont(scaledFont);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (int)(x + offsetX + (scaledWidth - fm.stringWidth(text)) / 2);
                int textY = (int)(y + offsetY + (scaledHeight + fm.getAscent()) / 2);
                g2d.drawString(text, textX, textY);
            }
        }
        
        if (selected) {
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.BLUE);
            g2d.drawRect((int)x - 1, (int)y - 1, (int)width + 2, (int)height + 2);
        }
    }
    
    public boolean contains(double mx, double my) {
       
        if (type == ElementType.TEXT) {
            return mx >= x && mx <= x + width && my >= y - height && my <= y;
        }
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
    
    public ElementType getType() {
        return type;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
        if (type == ElementType.TEXT) {
            calculateDimensions();
        }
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public Color getTextColor() {
        return textColor;
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }
    
    public Font getTextFont() {
        return textFont;
    }
    
    public void setTextFont(Font font) {
        this.textFont = font;
        if (type == ElementType.TEXT) {
            calculateDimensions();
        }
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public boolean isHovered() {
        return hovered;
    }
    
    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }
    
    public void setUseScaleEffect(boolean use) {
        this.useScaleEffect = use;
    }
    
    public void setTrackMouse(boolean track) {
        this.trackMouse = track;
    }
    
    public void setMousePosition(Point mousePos) {
        this.mousePosition = mousePos;
    }
    
    public double getCurrentAngle() {
        return currentAngle;
    }
    
    public void setForcedAngle(double angleDegrees) {
        this.forcedAngle = Math.toRadians(angleDegrees);
    }
    
    public void clearForcedAngle() {
        this.forcedAngle = Double.NaN;
    }
    
    public String getButtonId() {
        return buttonId;
    }
    
    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }
    
    public ButtonAction getButtonAction() {
        return buttonAction;
    }
    
    public void setButtonAction(ButtonAction buttonAction) {
        this.buttonAction = buttonAction;
    }
    
    public void executeAction() {
        if (buttonAction != null) {
            buttonAction.execute();
        }
    }
}

