package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class SettingsMenu extends JFrame {
    private static final String MENU_BACKGROUND = "assets" + File.separator + "background" + File.separator + "Score-Screen-Background.png";
    
    private SettingsPanel settingsPanel;
    
    public SettingsMenu() {
        initializeWindow();
        createSettingsPanel();
        pack();
        centerWindow();
    }
    
    private void initializeWindow() {
        setTitle("No Brakes Life - Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        addDragSupport();
        addKeyListener();
    }
    
    private void addDragSupport() {
        final int[] point = new int[2];
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                point[0] = e.getXOnScreen();
                point[1] = e.getYOnScreen();
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int newX = getX() + e.getXOnScreen() - point[0];
                int newY = getY() + e.getYOnScreen() - point[1];
                point[0] = e.getXOnScreen();
                point[1] = e.getYOnScreen();
                setLocation(newX, newY);
            }
        });
    }
    
    private void addKeyListener() {
        final SettingsMenu self = this;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                self.backToMainMenuStatic();
                return true;
            }
            return false;
        });
    }
    
    private void createSettingsPanel() {
        settingsPanel = new SettingsPanel();
        add(settingsPanel);
    }
    
    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }
    
    private class SettingsPanel extends JPanel implements MouseListener, MouseMotionListener {
        private BufferedImage backgroundImage;
        private Point mousePosition = new Point(0, 0);
        private ArrayList<MenuElement> uiElements;
        private ArrayList<SliderBar> sliderBars;
        private SliderBar draggedSlider;
        private SliderBar masterSlider;
        private SliderBar musicSlider;
        private SliderBar sfxSlider;
        private boolean isMousePressed = false;
        private boolean isHoveringButton = false;
        private boolean wasHoveringButton = false;
        
        public SettingsPanel() {
            uiElements = new ArrayList<>();
            sliderBars = new ArrayList<>();
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            loadBackgroundImage();
            setCursor(CursorManager.getNormalCursor());
            addMouseListener(this);
            addMouseMotionListener(this);
            loadSettingsElements(this);
        }
        
        public void addElement(MenuElement element) {
            uiElements.add(element);
        }
        
        public void addSliderBar(SliderBar slider) {
            sliderBars.add(slider);
        }
        
        public void setMasterSlider(SliderBar slider) {
            this.masterSlider = slider;
        }
        
        public void setMusicSlider(SliderBar slider) {
            this.musicSlider = slider;
        }
        
        public void setSFXSlider(SliderBar slider) {
            this.sfxSlider = slider;
        }
        
        private void loadBackgroundImage() {
            try {
                String fullPath = System.getProperty("user.dir") + File.separator + MENU_BACKGROUND;
                File imageFile = new File(fullPath);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                }
            } catch (Exception ex) {
                System.err.println("ไม่สามารถโหลดภาพพื้นหลังได้: " + ex.getMessage());
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            if (backgroundImage != null) {
                int imageWidth = backgroundImage.getWidth();
                int imageHeight = backgroundImage.getHeight();
                
                double scaleX = (double) getWidth() / imageWidth;
                double scaleY = (double) getHeight() / imageHeight;
                double scale = Math.max(scaleX, scaleY);
                
                int scaledWidth = (int) (imageWidth * scale);
                int scaledHeight = (int) (imageHeight * scale);
                
                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;
                
                g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);
            } else {
                g2d.setColor(new Color(20, 20, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
            if (uiElements != null) {
                isHoveringButton = false;
                for (MenuElement element : uiElements) {
                    if (element.getType() == MenuElement.ElementType.IMAGE && element.getButtonId() != null) {
                        boolean isHovering = element.contains(mousePosition.x, mousePosition.y);
                        element.setHovered(isHovering);
                        if (isHovering) {
                            isHoveringButton = true;
                        }
                    }
                    element.render(g2d);
                }
            }
            
            if (isHoveringButton && !wasHoveringButton) {
                playHoverSound();
            }
            wasHoveringButton = isHoveringButton;
            
        
            if (sliderBars != null) {
                for (SliderBar slider : sliderBars) {
                    slider.render(g2d);
                }
            }

            g2d.setColor(new Color(255, 255, 255, 255));
            g2d.setFont(FontManager.getThaiFont(26));
            String versionText = "v " + GitVersion.getVersion();
            g2d.drawString(versionText, 20, getHeight() - 20);
            
            updateCursor();
        }
        
        private void updateCursor() {
            if (isMousePressed) {
                setCursor(CursorManager.getPressCursor());
            } else {
                setCursor(CursorManager.getNormalCursor());
            }
        }
        
        public static void loadSettingsElements(SettingsPanel panel) {
            MenuElement backButton = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Small-Blue.png",
                104.0, 49.0, 367.0, 196.0,
                "back_button",
                () -> panel.backToMainMenu()
            );
            backButton.setHoverImage("assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Small-Pink.png");
            panel.addElement(backButton);
            
            MenuElement backIcon = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "icon" + File.separator + "Back-Arrow.png",
                204.0, 85.5, 167.0, 123.0
            );
            panel.addElement(backIcon);
            
            MenuElement paperStack = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "background" + File.separator + "Paper-Stack.png",
                506.0, 48.0, 1521.0, 2048.0
            );
            panel.addElement(paperStack);
            
     /*        MenuElement handHeld = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "Hand-Held.png",
                -169.0, 2.0, 730.1, 412.4
            );
            panel.addElement(handHeld); */
            
            MenuElement robotHead = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "Robot-Head-01.png",
                1785.0, 367.0, 936.4, 669.8
            );
            panel.addElement(robotHead);
            
            MenuElement titleText = new MenuElement("การตั้งค่า", 613.0, 192.0, 91);
            titleText.setTextColor(new Color(30, 30, 40));
            panel.addElement(titleText);
            
            MenuElement cassette = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "Cassette-02.png",
                2003.0, 2.0, 651.2, 414.6
            );
            panel.addElement(cassette);
            
            MenuElement text1 = new MenuElement("เสียงโดยรวม", 669.0, 365.8, 50);
            text1.setTextColor(new Color(30, 30, 40));
            panel.addElement(text1);
            
            MenuElement text2 = new MenuElement("เสียงเพลง", 722.0, 527.0, 50);
            text2.setTextColor(new Color(30, 30, 40));
            panel.addElement(text2);
            
            MenuElement text3 = new MenuElement("เสียงเอฟเฟค", 668.0, 702.0, 50);
            text3.setTextColor(new Color(30, 30, 40));
            panel.addElement(text3);
            
            float master = SoundManager.getInstance().getMasterVolume();
            float music = SoundManager.getInstance().getMusicVolume();
            float sfx = SoundManager.getInstance().getSFXVolume();
            System.out.println("Settings panel load: master=" + master + ", music=" + music + ", sfx=" + sfx);
            
            SliderBar slider1 = new SliderBar(984.9, 338.0, 654.6, 19.1);
            slider1.setValue(master);
            panel.addSliderBar(slider1);
            panel.setMasterSlider(slider1);
            
            SliderBar slider2 = new SliderBar(984.9, 498.4, 656.7, 19.2);
            slider2.setValue(music);
            panel.addSliderBar(slider2);
            panel.setMusicSlider(slider2);
            
            SliderBar slider3 = new SliderBar(983.3, 679.0, 659.9, 19.3);
            slider3.setValue(sfx);
            panel.addSliderBar(slider3);
            panel.setSFXSlider(slider3);
            
            MenuElement icon1 = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "setting" + File.separator + "Icon-Settings-Audio #3925.png",
                1655.0, 311.5, 92.4, 72.1
            );
            panel.addElement(icon1);
            
            MenuElement icon2 = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "setting" + File.separator + "Icon-Settings-Audio #3925.png",
                1655.0, 472.0, 92.4, 72.1
            );
            panel.addElement(icon2);
            
            MenuElement icon3 = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "setting" + File.separator + "Icon-Settings-Audio #3925.png",
                1655.0, 652.6, 92.4, 72.1
            );
            panel.addElement(icon3);
            
            MenuElement settingsIcon = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "setting" + File.separator + "Icon-Settings-Game.png",
                1005.0, 102.0, 120.0, 120.0
            );
            panel.addElement(settingsIcon);
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            mousePosition = e.getPoint();
            repaint();
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();
                for (MenuElement element : uiElements) {
                    if (element.getType() == MenuElement.ElementType.IMAGE && element.contains(p.x, p.y)) {
                        playClickSound();
                        element.executeAction();
                        return;
                    }
                }
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                isMousePressed = true;
                Point p = e.getPoint();
                
    
                for (SliderBar slider : sliderBars) {
                    if (slider.contains(p.x, p.y)) {
                        slider.startDrag(p.x, p.y);
                        draggedSlider = slider;
                        repaint();
                        return;
                    }
                }
                
                repaint();
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                isMousePressed = false;
                if (draggedSlider != null) {
                    draggedSlider.endDrag();
                    draggedSlider = null;
                }
                repaint();
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggedSlider != null) {
                Point p = e.getPoint();
                draggedSlider.drag(p.x, p.y);
           
                SoundManager soundManager = SoundManager.getInstance();
                if (draggedSlider == masterSlider) {
                    soundManager.setMasterVolume(draggedSlider.getValueFloat());
                } else if (draggedSlider == musicSlider) {
                    soundManager.setMusicVolume(draggedSlider.getValueFloat());
                } else if (draggedSlider == sfxSlider) {
                    soundManager.setSFXVolume(draggedSlider.getValueFloat());
                }
                
                repaint();
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {}
        
        @Override
        public void mouseExited(MouseEvent e) {}
        
        private void playHoverSound() {
            SoundManager.getInstance().playSFX("assets" + File.separator + "sfx" + File.separator + "Button Select.wav");
        }
        
        private void playClickSound() {
            SoundManager.getInstance().playSFX("assets" + File.separator + "sfx" + File.separator + "Button Click 1.wav");
        }
        
        public void backToMainMenu() {
            SwingUtilities.invokeLater(() -> {
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
                
             
                javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
                    SettingsMenu.this.setVisible(false);
                });
                timer.setRepeats(false);
                timer.start();
            });
        }
    }
    
    public void backToMainMenuStatic() {
        SwingUtilities.invokeLater(() -> {
            MainMenu mainMenu = new MainMenu();
            mainMenu.setVisible(true);
            

            javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
                this.setVisible(false);
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}

