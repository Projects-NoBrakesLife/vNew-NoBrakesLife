package game;

import java.awt.*;
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
        try {
            String hoverPath = System.getProperty("user.dir") + File.separator + "assets/ui/button/Button-Big-Pink.png";
            File file = new File(hoverPath);
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
        if (type == ElementType.IMAGE && image != null) {
            BufferedImage imgToDraw = hovered && hoverImage != null ? hoverImage : image;
            g2d.drawImage(imgToDraw, (int)x, (int)y, (int)width, (int)height, null);
        } else if (type == ElementType.TEXT) {
            g2d.setColor(textColor);
            g2d.setFont(textFont);
            g2d.drawString(text, (int)x, (int)y);
        } else if (type == ElementType.BUTTON) {
            BufferedImage imgToDraw = hovered && hoverImage != null ? hoverImage : image;
            if (imgToDraw != null) {
                g2d.drawImage(imgToDraw, (int)x, (int)y, (int)width, (int)height, null);
            }
            if (text != null && !text.isEmpty()) {
                g2d.setColor(textColor);
                g2d.setFont(textFont);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (int)(x + (width - fm.stringWidth(text)) / 2);
                int textY = (int)(y + (height + fm.getAscent()) / 2);
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

