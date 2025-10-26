package game;

import java.awt.Font;
import java.io.File;

public class FontManager {
    private static Font thaiFont;
    
    public static Font getThaiFont(int style, int size) {
        if (thaiFont == null) {
            try {
                String fontPath = "assets" + File.separator + "font" + File.separator + "NotoSerifThai-Regular.ttf";
                File fontFile = new File(fontPath);
                if (!fontFile.exists()) {
                    fontFile = new File(System.getProperty("user.dir") + File.separator + fontPath);
                }
                if (fontFile.exists()) {
                    thaiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                } else {
                    thaiFont = new Font("Arial", Font.PLAIN, size);
                }
            } catch (Exception ex) {
                thaiFont = new Font("Arial", Font.PLAIN, size);
            }
        }
        
        return thaiFont.deriveFont(style, size);
    }
    
    public static Font getThaiFont(int size) {
        return getThaiFont(Font.PLAIN, size);
    }
}
