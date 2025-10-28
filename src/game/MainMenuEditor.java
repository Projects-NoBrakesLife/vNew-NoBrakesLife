package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MainMenuEditor extends JFrame {
    private MenuEditorPanel editorPanel;
    private ArrayList<MenuElement> elements;
    private MenuElement selectedElement;
    private MenuElement draggedElement;
    private Point dragOffset;
    
    public MainMenuEditor() {
        initializeWindow();
        createEditorPanel();
        pack();
        centerWindow();
    }
    
    private void initializeWindow() {
        setTitle("Main Menu Editor - No Brakes Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());
    }
    
    private void createEditorPanel() {
        editorPanel = new MenuEditorPanel();
        add(editorPanel, BorderLayout.CENTER);
    }
    
    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }
    
    private class MenuEditorPanel extends JPanel implements MouseListener, MouseMotionListener {
        private BufferedImage backgroundImage;
        private JSlider scaleSlider;
        private JLabel scaleLabel;
        private JSlider fontSlider;
        private JLabel fontLabel;
        private JButton colorBtn;
        private Color currentTextColor = Color.WHITE;
        private int originalFontSize;
        private double originalWidth;
        private double originalHeight;
        private ArrayList<Integer> guideLines;
        
        public MenuEditorPanel() {
            setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
            setLayout(null);
            setFocusable(true);
            elements = new ArrayList<>();
            guideLines = new ArrayList<>();
            loadBackgroundImage();
            setupToolbar();
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        
        private void loadBackgroundImage() {
            try {
                String fullPath = System.getProperty("user.dir") + File.separator + 
                    "assets" + File.separator + "background" + File.separator + "Score-Screen-Background.png";
                File imageFile = new File(fullPath);
                if (imageFile.exists()) {
                    backgroundImage = ImageIO.read(imageFile);
                }
            } catch (Exception ex) {
            }
        }
        
        private void setupToolbar() {
            JToolBar toolBar = new JToolBar();
            Font buttonFont = FontManager.getThaiFont(Font.BOLD, 14);
            Font labelFont = FontManager.getThaiFont(12);
            
            JButton addImageBtn = new JButton("เพิ่มรูป");
            addImageBtn.setFont(buttonFont);
            addImageBtn.addActionListener(e -> { handleAddImage(); });
            
            JButton addTextBtn = new JButton("เพิ่มข้อความ");
            addTextBtn.setFont(buttonFont);
            addTextBtn.addActionListener(e -> { handleAddText(); });
            
            JButton deleteBtn = new JButton("ลบ");
            deleteBtn.setFont(buttonFont);
            deleteBtn.addActionListener(e -> { handleDelete(); });
            
            JButton layerUpBtn = new JButton("เลเยอร์ ↑");
            layerUpBtn.setFont(buttonFont);
            layerUpBtn.addActionListener(e -> { handleLayerUp(); });
            
            JButton layerDownBtn = new JButton("เลเยอร์ ↓");
            layerDownBtn.setFont(buttonFont);
            layerDownBtn.addActionListener(e -> { handleLayerDown(); });
            
            JButton exportBtn = new JButton("Export โค้ด");
            exportBtn.setFont(buttonFont);
            exportBtn.addActionListener(e -> { handleExportCode(); });
            
            JButton clearBtn = new JButton("ล้างทั้งหมด");
            clearBtn.setFont(buttonFont);
            clearBtn.addActionListener(e -> { handleClearAll(); });
            
            JButton loadFromGameBtn = new JButton("โหลดจากเกมจริง");
            loadFromGameBtn.setFont(buttonFont);
            loadFromGameBtn.addActionListener(e -> { loadFromGame(); });
            
            toolBar.add(loadFromGameBtn);
            toolBar.addSeparator();
            toolBar.add(addImageBtn);
            toolBar.add(addTextBtn);
            toolBar.add(deleteBtn);
            toolBar.addSeparator();
            
            JLabel sizeLabel = new JLabel("ขนาด:");
            sizeLabel.setFont(labelFont);
            toolBar.add(sizeLabel);
            
            scaleSlider = new JSlider(10, 500, 100);
            scaleSlider.setMajorTickSpacing(100);
            scaleSlider.setMinorTickSpacing(25);
            scaleSlider.setPaintTicks(false);
            scaleSlider.setPaintLabels(false);
            scaleSlider.addChangeListener(e -> { handleScaleChange(); });
            toolBar.add(scaleSlider);
            
            scaleLabel = new JLabel("100%");
            scaleLabel.setFont(labelFont);
            toolBar.add(scaleLabel);
            
            toolBar.addSeparator();
            
            JLabel fontLabelTitle = new JLabel("ฟอนต์:");
            fontLabelTitle.setFont(labelFont);
            toolBar.add(fontLabelTitle);
            
            fontSlider = new JSlider(8, 200, 32);
            fontSlider.setMajorTickSpacing(50);
            fontSlider.setMinorTickSpacing(10);
            fontSlider.setPaintTicks(false);
            fontSlider.setPaintLabels(false);
            fontSlider.addChangeListener(e -> { handleFontChange(); });
            toolBar.add(fontSlider);
            
            fontLabel = new JLabel("32");
            fontLabel.setFont(labelFont);
            toolBar.add(fontLabel);
            
            toolBar.addSeparator();
            
            JLabel colorLabel = new JLabel("สี:");
            colorLabel.setFont(labelFont);
            toolBar.add(colorLabel);
            
            colorBtn = new JButton("⚫");
            colorBtn.setFont(labelFont);
            colorBtn.setBackground(currentTextColor);
            colorBtn.setOpaque(true);
            colorBtn.addActionListener(e -> { handleColorChange(); });
            toolBar.add(colorBtn);
            
            toolBar.addSeparator();
            toolBar.add(layerUpBtn);
            toolBar.add(layerDownBtn);
            
            toolBar.addSeparator();
            toolBar.add(exportBtn);
            toolBar.addSeparator();
            toolBar.add(clearBtn);
            
            SwingUtilities.invokeLater(() -> {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    frame.setJMenuBar(new JMenuBar());
                    frame.getJMenuBar().add(toolBar);
                }
            });
        }
        
        private void handleAddImage() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("เลือกไฟล์รูปภาพ");
            String projectRoot = System.getProperty("user.dir");
            fileChooser.setCurrentDirectory(new File(projectRoot));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
                }
                
                @Override
                public String getDescription() {
                    return "ไฟล์รูปภาพ (*.png, *.jpg, *.jpeg, *.gif)";
                }
            });
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String relativePath = getRelativePath(selectedFile);
                MenuElement element = new MenuElement(MenuElement.ElementType.IMAGE, relativePath, 400, 300, 0, 0);
                elements.add(element);
                repaint();
            }
        }
        
        private void handleAddText() {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "เพิ่มข้อความ", true);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel label = new JLabel("ป้อนข้อความ:");
            label.setFont(FontManager.getThaiFont(14));
            
            JTextField textField = new JTextField(20);
            textField.setFont(FontManager.getThaiFont(14));
            
            JLabel sizeLabel = new JLabel("ขนาดตัวอักษร:");
            sizeLabel.setFont(FontManager.getThaiFont(14));
            
            JTextField sizeField = new JTextField("32", 10);
            sizeField.setFont(FontManager.getThaiFont(14));
            
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            inputPanel.add(label);
            inputPanel.add(textField);
            inputPanel.add(sizeLabel);
            inputPanel.add(sizeField);
            
            JButton okBtn = new JButton("ตกลง");
            okBtn.setFont(FontManager.getThaiFont(Font.BOLD, 14));
            
            JButton cancelBtn = new JButton("ยกเลิก");
            cancelBtn.setFont(FontManager.getThaiFont(14));
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okBtn);
            buttonPanel.add(cancelBtn);
            
            panel.add(inputPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            
            okBtn.addActionListener(e -> {
                String text = textField.getText();
                if (text != null && !text.trim().isEmpty()) {
                    int fontSize = 32;
                    try {
                        String sizeStr = sizeField.getText();
                        if (sizeStr != null && !sizeStr.trim().isEmpty()) {
                            fontSize = Integer.parseInt(sizeStr);
                        }
                    } catch (Exception ex) {
                    }
                    MenuElement element = new MenuElement(text, 400, 300, fontSize);
                    elements.add(element);
                    repaint();
                }
                dialog.dispose();
            });
            
            cancelBtn.addActionListener(e -> dialog.dispose());
            
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
        
        private void handleDelete() {
            if (selectedElement != null) {
                elements.remove(selectedElement);
                selectedElement = null;
                updateScaleSlider();
                repaint();
            }
        }
        
        private void handleLayerUp() {
            if (selectedElement != null) {
                int index = elements.indexOf(selectedElement);
                if (index < elements.size() - 1) {
                    elements.remove(index);
                    elements.add(index + 1, selectedElement);
                    repaint();
                }
            }
        }
        
        private void handleLayerDown() {
            if (selectedElement != null) {
                int index = elements.indexOf(selectedElement);
                if (index > 0) {
                    elements.remove(index);
                    elements.add(index - 1, selectedElement);
                    repaint();
                }
            }
        }
        
        private void handleScaleChange() {
            if (selectedElement != null && selectedElement.getType() == MenuElement.ElementType.IMAGE) {
                int sliderValue = scaleSlider.getValue();
                double scale = sliderValue / 100.0;
                
                scaleLabel.setText(sliderValue + "%");
                
                selectedElement.setWidth(originalWidth * scale);
                selectedElement.setHeight(originalHeight * scale);
                
                repaint();
            }
        }
        
        private void handleFontChange() {
            if (selectedElement != null && selectedElement.getType() == MenuElement.ElementType.TEXT) {
                int fontSize = fontSlider.getValue();
                
                fontLabel.setText(String.valueOf(fontSize));
                
                Font oldFont = selectedElement.getTextFont();
                Font newFont = FontManager.getThaiFont(oldFont.getStyle(), fontSize);
                selectedElement.setTextFont(newFont);
                
                repaint();
            }
        }
        
        private void handleColorChange() {
            if (selectedElement != null && selectedElement.getType() == MenuElement.ElementType.TEXT) {
                Color newColor = JColorChooser.showDialog(this, "เลือกสีข้อความ", currentTextColor);
                if (newColor != null) {
                    currentTextColor = newColor;
                    selectedElement.setTextColor(newColor);
                    colorBtn.setBackground(newColor);
                    repaint();
                }
            } else {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกข้อความก่อน", "ข้อความแจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            }
        }
        
        private void updateScaleSlider() {
            if (selectedElement != null) {
                if (selectedElement.getType() == MenuElement.ElementType.IMAGE) {
                    scaleSlider.setEnabled(true);
                    originalWidth = selectedElement.getWidth();
                    originalHeight = selectedElement.getHeight();
                    scaleSlider.setValue(100);
                    scaleLabel.setText("100%");
                } else {
                    scaleSlider.setEnabled(false);
                    scaleLabel.setText("-");
                }
                
                if (selectedElement.getType() == MenuElement.ElementType.TEXT) {
                    fontSlider.setEnabled(true);
                    int currentSize = selectedElement.getTextFont().getSize();
                    originalFontSize = currentSize;
                    fontSlider.setValue(currentSize);
                    fontLabel.setText(String.valueOf(currentSize));
                    
                    currentTextColor = selectedElement.getTextColor();
                    colorBtn.setBackground(currentTextColor);
                } else {
                    fontSlider.setEnabled(false);
                    fontLabel.setText("-");
                }
            } else {
                scaleSlider.setEnabled(false);
                scaleLabel.setText("-");
                fontSlider.setEnabled(false);
                fontLabel.setText("-");
            }
        }
        
        private void handleExportCode() {
            StringBuilder code = new StringBuilder();
            code.append("public static void loadMenuElements(MenuPanel panel) {\n");
            
            for (MenuElement element : elements) {
                if (element.getType() == MenuElement.ElementType.IMAGE) {
                    code.append(String.format("    MenuElement img = new MenuElement(MenuElement.ElementType.IMAGE, \"%s\", %.1f, %.1f, %.1f, %.1f);\n",
                        element.getImagePath(), element.getX(), element.getY(), element.getWidth(), element.getHeight()));
                    code.append("    panel.addElement(img);\n");
                } else if (element.getType() == MenuElement.ElementType.TEXT) {
                    code.append(String.format("    MenuElement text = new MenuElement(\"%s\", %.1f, %.1f, %d);\n",
                        element.getText(), element.getX(), element.getY(), element.getTextFont().getSize()));
                    code.append("    panel.addElement(text);\n");
                }
            }
            
            code.append("}\n");
            
            JTextArea textArea = new JTextArea(code.toString());
            textArea.setFont(FontManager.getThaiFont(12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(800, 500));
            
            int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Exported Menu Code",
                JOptionPane.OK_CANCEL_OPTION
            );
            
            if (result == JOptionPane.OK_OPTION) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new java.awt.datatransfer.StringSelection(code.toString()),
                    null
                );
                JOptionPane.showMessageDialog(this, "คัดลอกโค้ดลงคลิปบอร์ดแล้ว!");
            }
        }
        
        private void handleClearAll() {
            int result = JOptionPane.showConfirmDialog(
                this,
                "ต้องการล้างทุกอย่างหรือไม่?",
                "ล้างทั้งหมด",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                elements.clear();
                selectedElement = null;
                draggedElement = null;
                repaint();
            }
        }
        
        private void loadFromGame() {
            elements.clear();
            
            MenuElement buttonPlay = new MenuElement(
                MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Big-Blue.png", 
                561.0, 267.0, 798.0, 196.0,
                "start_game",
                null
            );
            elements.add(buttonPlay);
            
            MenuElement text1 = new MenuElement("เล่นเกม", 790.5, 395.0, 100);
            elements.add(text1);
            
            MenuElement button2 = new MenuElement(
                MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Big-Gray.png", 
                561.0, 518.0, 798.0, 196.0,
                "button_2",
                null
            );
            elements.add(button2);
            
            MenuElement text2 = new MenuElement("ตั้งค่า", 840.5, 661.0, 100);
            elements.add(text2);
            
            MenuElement button3 = new MenuElement(
                MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Big-Gray.png", 
                561.0, 785.0, 798.0, 196.0,
                "button_3",
                null
            );
            elements.add(button3);
            
            MenuElement text3 = new MenuElement("ออก", 840.5, 912.0, 100);
            elements.add(text3);
            
            MenuElement logo = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "logo" + File.separator + "logo.png",
                718.1, 23.0, 483.7, 285.2
            );
            elements.add(logo);
            
            MenuElement card = new MenuElement(
                MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "Card-Beg-For-Wishlist.png",
                1460.0, 282.0, 409.8, 581.2
            );
            elements.add(card);
            
            MenuElement cardText = new MenuElement("เด็ก CS กำลังขอเกรด A", 1487.9, 383.0, 35);
            elements.add(cardText);
            
            selectedElement = null;
            draggedElement = null;
            updateScaleSlider();
            repaint();
        }
        
        private String getRelativePath(File file) {
            String projectRoot = System.getProperty("user.dir");
            String filePath = file.getAbsolutePath();
            if (filePath.startsWith(projectRoot)) {
                return filePath.substring(projectRoot.length() + 1);
            }
            return filePath;
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
            
            for (MenuElement element : elements) {
                element.render(g2d);
            }
            
            drawGuideLines(g2d);
        }
        
        private void drawGuideLines(Graphics2D g2d) {
            g2d.setColor(new Color(255, 0, 0, 150));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
            
            for (Integer linePos : guideLines) {
                if (linePos >= 0) {
                    g2d.drawLine(linePos, 0, linePos, getHeight());
                    g2d.drawLine(0, linePos, getWidth(), linePos);
                }
            }
        }
        
        private void updateGuideLines() {
            guideLines.clear();
            
            if (draggedElement == null) return;
            
            int snapThreshold = 10;
            double elemX = draggedElement.getX();
            double elemY = draggedElement.getY();
            double elemW = draggedElement.getWidth();
            double elemH = draggedElement.getHeight();
            double elemCX = elemX + elemW / 2;
            double elemCY = elemY + elemH / 2;
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            if (Math.abs(elemCX - centerX) < snapThreshold) {
                guideLines.add(centerX);
            }
            if (Math.abs(elemCY - centerY) < snapThreshold) {
                guideLines.add(centerY);
            }
            
            for (MenuElement other : elements) {
                if (other == draggedElement) continue;
                
                double otherX = other.getX();
                double otherY = other.getY();
                double otherW = other.getWidth();
                double otherH = other.getHeight();
                double otherCX = otherX + otherW / 2;
                double otherCY = otherY + otherH / 2;
                
                if (Math.abs(elemX - otherX) < snapThreshold || 
                    Math.abs(elemX - (otherX + otherW)) < snapThreshold) {
                    guideLines.add((int)otherX);
                    guideLines.add((int)(otherX + otherW));
                }
                
                if (Math.abs(elemCX - otherCX) < snapThreshold) {
                    guideLines.add((int)otherCX);
                }
                
                if (Math.abs(elemY - otherY) < snapThreshold || 
                    Math.abs(elemY - (otherY + otherH)) < snapThreshold) {
                    guideLines.add((int)otherY);
                    guideLines.add((int)(otherY + otherH));
                }
                
                if (Math.abs(elemCY - otherCY) < snapThreshold) {
                    guideLines.add((int)otherCY);
                }
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            
            boolean found = false;
            for (int i = elements.size() - 1; i >= 0; i--) {
                MenuElement element = elements.get(i);
                if (element.contains(p.x, p.y)) {
                    selectedElement = element;
                    element.setSelected(true);
                    draggedElement = element;
                    
                    double elementX = element.getX();
                    double elementY = element.getY();
                    dragOffset = new Point((int)(p.x - elementX), (int)(p.y - elementY));
                    
                    elements.remove(i);
                    elements.add(element);
                    found = true;
                    updateScaleSlider();
                    repaint();
                    break;
                }
            }
            
            if (!found) {
                if (selectedElement != null) {
                    selectedElement.setSelected(false);
                }
                selectedElement = null;
                updateScaleSlider();
                repaint();
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggedElement != null) {
                double newX = e.getX() - dragOffset.x;
                double newY = e.getY() - dragOffset.y;
                
                double elemW = draggedElement.getWidth();
                double elemH = draggedElement.getHeight();
                double elemCX = newX + elemW / 2;
                double elemCY = newY + elemH / 2;
                
                int snapThreshold = 10;
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                if (Math.abs(elemCX - centerX) < snapThreshold) {
                    newX = centerX - elemW / 2;
                }
                if (Math.abs(elemCY - centerY) < snapThreshold) {
                    newY = centerY - elemH / 2;
                }
                
                for (MenuElement other : elements) {
                    if (other == draggedElement) continue;
                    
                    double otherX = other.getX();
                    double otherY = other.getY();
                    double otherW = other.getWidth();
                    double otherH = other.getHeight();
                    double otherCX = otherX + otherW / 2;
                    double otherCY = otherY + otherH / 2;
                    
                    if (Math.abs(newX - otherX) < snapThreshold) {
                        newX = otherX;
                    }
                    if (Math.abs(newX - (otherX + otherW)) < snapThreshold) {
                        newX = otherX + otherW;
                    }
                    if (Math.abs(elemCX - otherCX) < snapThreshold) {
                        newX = otherCX - elemW / 2;
                    }
                    
                    if (Math.abs(newY - otherY) < snapThreshold) {
                        newY = otherY;
                    }
                    if (Math.abs(newY - (otherY + otherH)) < snapThreshold) {
                        newY = otherY + otherH;
                    }
                    if (Math.abs(elemCY - otherCY) < snapThreshold) {
                        newY = otherCY - elemH / 2;
                    }
                }
                
                draggedElement.setX(newX);
                draggedElement.setY(newY);
                
                updateGuideLines();
                repaint();
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            draggedElement = null;
            guideLines.clear();
            repaint();
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                Point p = e.getPoint();
                for (int i = elements.size() - 1; i >= 0; i--) {
                    MenuElement element = elements.get(i);
                    if (element.contains(p.x, p.y)) {
                        elements.remove(i);
                        if (selectedElement == element) {
                            selectedElement = null;
                            updateScaleSlider();
                        }
                        repaint();
                        return;
                    }
                }
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {}
        
        @Override
        public void mouseExited(MouseEvent e) {}
        
        @Override
        public void mouseMoved(MouseEvent e) {}
    }
}

