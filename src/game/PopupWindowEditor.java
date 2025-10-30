package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class PopupWindowEditor extends JFrame {
    private PopupEditorPanel editorPanel;
    private ArrayList<MenuElement> elements;
    private MenuElement selectedElement;
    private MenuElement draggedElement;
    private Point dragOffset;
    private PopupWindowConfig config;

    public PopupWindowEditor() {
        config = new PopupWindowConfig();
        initializeWindow();
        createEditorPanel();
        pack();
        centerWindow();
    }

    private void initializeWindow() {
        setTitle("Popup Window Editor - No Brakes Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());
    }

    private void createEditorPanel() {
        editorPanel = new PopupEditorPanel();
        add(editorPanel, BorderLayout.CENTER);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    public static class PopupWindowConfig {
        public int width = 1600;
        public int height = 900;
        public Color backgroundColor = new Color(240, 240, 240);
        public String backgroundImagePath = null;
        public boolean useBackgroundImage = false;
    }

    private class PopupEditorPanel extends JPanel implements MouseListener, MouseMotionListener {
        private BufferedImage backgroundImage;
        private JSlider scaleSlider;
        private JLabel scaleLabel;
        private JSlider fontSlider;
        private JLabel fontLabel;
        private JButton colorBtn;
        private Color currentTextColor = Color.BLACK;
        private double originalWidth;
        private double originalHeight;

        public PopupEditorPanel() {
            // ใช้ขนาดใกล้เคียงกับเกมจริง แต่ไม่เต็มจอ
            int editorWidth = (int)(GameConfig.WINDOW_WIDTH * 0.85);
            int editorHeight = (int)(GameConfig.WINDOW_HEIGHT * 0.85);
            if (config.width < editorWidth) config.width = editorWidth;
            if (config.height < editorHeight) config.height = editorHeight;
            setPreferredSize(new Dimension(config.width, config.height));
            setLayout(null);
            setFocusable(true);
            elements = new ArrayList<>();
            loadBackground();
            setupToolbar();
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        private void loadBackground() {
            if (config.useBackgroundImage && config.backgroundImagePath != null) {
                try {
                    String fullPath = System.getProperty("user.dir") + File.separator + config.backgroundImagePath;
                    File imageFile = new File(fullPath);
                    if (imageFile.exists()) {
                        backgroundImage = ImageIO.read(imageFile);
                    }
                } catch (Exception ex) {
                }
            }
        }

        private void setupToolbar() {
            JToolBar toolBar = new JToolBar();
            Font buttonFont = FontManager.getThaiFont(Font.BOLD, 14);
            Font labelFont = FontManager.getThaiFont(12);

            JButton bgColorBtn = new JButton("พื้นหลัง - สี");
            bgColorBtn.setFont(buttonFont);
            bgColorBtn.addActionListener(_ -> handleBackgroundColor());

            JButton bgImageBtn = new JButton("พื้นหลัง - ภาพ");
            bgImageBtn.setFont(buttonFont);
            bgImageBtn.addActionListener(_ -> handleBackgroundImage());

            JButton bgRemoveBtn = new JButton("ลบพื้นหลัง");
            bgRemoveBtn.setFont(buttonFont);
            bgRemoveBtn.addActionListener(_ -> handleRemoveBackground());

            JButton sizeBtn = new JButton("ขนาดหน้าต่าง");
            sizeBtn.setFont(buttonFont);
            sizeBtn.addActionListener(_ -> handleWindowSize());

            toolBar.add(bgColorBtn);
            toolBar.add(bgImageBtn);
            toolBar.add(bgRemoveBtn);
            toolBar.addSeparator();
            toolBar.add(sizeBtn);
            toolBar.addSeparator();

            JButton addImageBtn = new JButton("เพิ่มรูป");
            addImageBtn.setFont(buttonFont);
            addImageBtn.addActionListener(_ -> handleAddImage());

            JButton addTextBtn = new JButton("เพิ่มข้อความ");
            addTextBtn.setFont(buttonFont);
            addTextBtn.addActionListener(_ -> handleAddText());

            JButton deleteBtn = new JButton("ลบ");
            deleteBtn.setFont(buttonFont);
            deleteBtn.addActionListener(_ -> handleDelete());

            JButton layerUpBtn = new JButton("เลเยอร์ ↑");
            layerUpBtn.setFont(buttonFont);
            layerUpBtn.addActionListener(_ -> handleLayerUp());

            JButton layerDownBtn = new JButton("เลเยอร์ ↓");
            layerDownBtn.setFont(buttonFont);
            layerDownBtn.addActionListener(_ -> handleLayerDown());

            JButton exportBtn = new JButton("Export โค้ด");
            exportBtn.setFont(buttonFont);
            exportBtn.addActionListener(_ -> handleExportCode());

            JButton importBtn = new JButton("Import โค้ด");
            importBtn.setFont(buttonFont);
            importBtn.addActionListener(_ -> handleImportCode());

            JButton clearBtn = new JButton("ล้างทั้งหมด");
            clearBtn.setFont(buttonFont);
            clearBtn.addActionListener(_ -> handleClearAll());

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
            scaleSlider.addChangeListener(_ -> handleScaleChange());
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
            fontSlider.addChangeListener(_ -> handleFontChange());
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
            colorBtn.addActionListener(_ -> handleColorChange());
            toolBar.add(colorBtn);

            toolBar.addSeparator();
            toolBar.add(layerUpBtn);
            toolBar.add(layerDownBtn);

            toolBar.addSeparator();
            toolBar.add(exportBtn);
            toolBar.add(importBtn);
            toolBar.addSeparator();
            toolBar.add(clearBtn);

            SwingUtilities.invokeLater(() -> {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    JDialog toolDialog = new JDialog(frame, "Dev Tools", false);
                    toolDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    JPanel content = new JPanel(new BorderLayout());
                    content.add(toolBar, BorderLayout.CENTER);
                    toolDialog.setContentPane(content);
                    toolDialog.pack();
                    Point frameLoc = frame.getLocationOnScreen();
                    toolDialog.setLocation(frameLoc.x + frame.getWidth() - toolDialog.getWidth() - 20, frameLoc.y + 40);
                    toolDialog.setAlwaysOnTop(true);
                    toolDialog.setVisible(true);
                }
            });
        }

        private void handleBackgroundColor() {
            Color newColor = JColorChooser.showDialog(this, "เลือกสีพื้นหลัง", config.backgroundColor);
            if (newColor != null) {
                config.backgroundColor = newColor;
                config.useBackgroundImage = false;
                config.backgroundImagePath = null;
                backgroundImage = null;
                repaint();
            }
        }

        private void handleBackgroundImage() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("เลือกรูปภาพพื้นหลัง");
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
                config.backgroundImagePath = relativePath;
                config.useBackgroundImage = true;
                loadBackground();
                repaint();
            }
        }

        private void handleRemoveBackground() {
            config.useBackgroundImage = false;
            config.backgroundImagePath = null;
            backgroundImage = null;
            config.backgroundColor = new Color(240, 240, 240);
            repaint();
        }

        private void handleWindowSize() {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "กำหนดขนาดหน้าต่าง", true);
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

            panel.add(new JLabel("ความกว้าง:"));
            JTextField widthField = new JTextField(String.valueOf(config.width), 10);
            panel.add(widthField);

            panel.add(new JLabel("ความสูง:"));
            JTextField heightField = new JTextField(String.valueOf(config.height), 10);
            panel.add(heightField);

            JButton okBtn = new JButton("ตกลง");
            okBtn.addActionListener(_ -> {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    if (width > 0 && height > 0) {
                        config.width = width;
                        config.height = height;
                        setPreferredSize(new Dimension(width, height));
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                        if (frame != null) {
                            frame.pack();
                        }
                        repaint();
                        dialog.dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "กรุณาป้อนตัวเลขที่ถูกต้อง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton cancelBtn = new JButton("ยกเลิก");
            cancelBtn.addActionListener(_ -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okBtn);
            buttonPanel.add(cancelBtn);

            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        private String getRelativePath(File file) {
            String projectRoot = System.getProperty("user.dir");
            String filePath = file.getAbsolutePath();
            if (filePath.startsWith(projectRoot)) {
                return filePath.substring(projectRoot.length() + 1);
            }
            return filePath;
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
                MenuElement element = new MenuElement(MenuElement.ElementType.IMAGE, relativePath, config.width / 2, config.height / 2, 0, 0);
                elements.add(element);
                repaint();
            }
        }

        private void handleAddText() {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "เพิ่มข้อความ", true);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

            okBtn.addActionListener(_ -> {
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
                    MenuElement element = new MenuElement(text, config.width / 2, config.height / 2, fontSize);
                    element.setTextColor(currentTextColor);
                    elements.add(element);
                    repaint();
                }
                dialog.dispose();
            });

            cancelBtn.addActionListener(_ -> dialog.dispose());

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
            code.append("PopupWindowConfig config = new PopupWindowConfig();\n");
            code.append(String.format("config.width = %d;\n", config.width));
            code.append(String.format("config.height = %d;\n", config.height));
            code.append(String.format("config.backgroundColor = new Color(%d, %d, %d);\n",
                    config.backgroundColor.getRed(), config.backgroundColor.getGreen(), config.backgroundColor.getBlue()));
            code.append(String.format("config.useBackgroundImage = %s;\n", config.useBackgroundImage));
            if (config.useBackgroundImage && config.backgroundImagePath != null) {
                code.append(String.format("config.backgroundImagePath = \"%s\";\n", config.backgroundImagePath));
            }
            code.append("\n");
            code.append("ArrayList<MenuElement> elements = new ArrayList<>();\n");

            for (MenuElement element : elements) {
                if (element.getType() == MenuElement.ElementType.IMAGE) {
                    code.append(String.format(
                            "MenuElement img = new MenuElement(MenuElement.ElementType.IMAGE, \"%s\", %.1f, %.1f, %.1f, %.1f);\n",
                            element.getImagePath(), element.getX(), element.getY(), element.getWidth(), element.getHeight()));
                    code.append("elements.add(img);\n");
                } else if (element.getType() == MenuElement.ElementType.TEXT) {
                    code.append(String.format("MenuElement text = new MenuElement(\"%s\", %.1f, %.1f, %d);\n",
                            element.getText().replace("\"", "\\\""), element.getX(), element.getY(), element.getTextFont().getSize()));
                    code.append(String.format("text.setTextColor(new Color(%d, %d, %d));\n",
                            element.getTextColor().getRed(), element.getTextColor().getGreen(), element.getTextColor().getBlue()));
                    code.append("elements.add(text);\n");
                }
            }

            code.append("\n");
            code.append("PopupWindow window = new PopupWindow(config, elements);\n");
            code.append("window.setVisible(true);\n");

            JTextArea textArea = new JTextArea(code.toString());
            textArea.setFont(FontManager.getThaiFont(12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(800, 500));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    scrollPane,
                    "Exported Popup Window Code",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new java.awt.datatransfer.StringSelection(code.toString()),
                        null);
                JOptionPane.showMessageDialog(this, "คัดลอกโค้ดลงคลิปบอร์ดแล้ว!");
            }
        }

        private void handleClearAll() {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "ต้องการล้างทุกอย่างหรือไม่?",
                    "ล้างทั้งหมด",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                elements.clear();
                selectedElement = null;
                draggedElement = null;
                repaint();
            }
        }

        private void handleImportCode() {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Import โค้ด", true);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel label = new JLabel("วางโค้ดที่นี่:");
            label.setFont(FontManager.getThaiFont(14));

            JTextArea textArea = new JTextArea(20, 60);
            textArea.setFont(FontManager.getThaiFont(Font.PLAIN, 11));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(700, 400));

            JButton okBtn = new JButton("ตกลง");
            okBtn.setFont(FontManager.getThaiFont(Font.BOLD, 14));

            JButton cancelBtn = new JButton("ยกเลิก");
            cancelBtn.setFont(FontManager.getThaiFont(14));

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okBtn);
            buttonPanel.add(cancelBtn);

            panel.add(label, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(panel);

            okBtn.addActionListener(_ -> {
                String code = textArea.getText();
                parseAndLoadCode(code);
                dialog.dispose();
            });

            cancelBtn.addActionListener(_ -> dialog.dispose());

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        private void parseAndLoadCode(String code) {
            elements.clear();

            String[] lines = code.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.contains("config.width =")) {
                    try {
                        config.width = Integer.parseInt(line.substring(line.indexOf("=") + 1).replace(";", "").trim());
                    } catch (Exception e) {}
                } else if (line.contains("config.height =")) {
                    try {
                        config.height = Integer.parseInt(line.substring(line.indexOf("=") + 1).replace(";", "").trim());
                    } catch (Exception e) {}
                } else if (line.contains("config.backgroundColor =")) {
                    try {
                        String colorStr = line.substring(line.indexOf("Color(") + 6, line.indexOf(")"));
                        String[] parts = colorStr.split(",");
                        int r = Integer.parseInt(parts[0].trim());
                        int g = Integer.parseInt(parts[1].trim());
                        int b = Integer.parseInt(parts[2].trim());
                        config.backgroundColor = new Color(r, g, b);
                        config.useBackgroundImage = false;
                    } catch (Exception e) {}
                } else if (line.contains("config.backgroundImagePath =")) {
                    try {
                        String path = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                        config.backgroundImagePath = path;
                        config.useBackgroundImage = true;
                        loadBackground();
                    } catch (Exception e) {}
                } else if (line.contains("new MenuElement")) {
                    if (line.contains("MenuElement.ElementType.IMAGE")) {
                        MenuElement img = parseImageElement(line);
                        if (img != null) {
                            elements.add(img);
                        }
                    } else if (line.contains("new MenuElement(\"")) {
                        MenuElement text = parseTextElement(line);
                        if (text != null) {
                            elements.add(text);
                        }
                    }
                } else if (line.contains("text.setTextColor")) {
                    if (!elements.isEmpty() && elements.get(elements.size() - 1).getType() == MenuElement.ElementType.TEXT) {
                        try {
                            String colorStr = line.substring(line.indexOf("Color(") + 6, line.indexOf(")"));
                            String[] parts = colorStr.split(",");
                            int r = Integer.parseInt(parts[0].trim());
                            int g = Integer.parseInt(parts[1].trim());
                            int b = Integer.parseInt(parts[2].trim());
                            elements.get(elements.size() - 1).setTextColor(new Color(r, g, b));
                        } catch (Exception e) {}
                    }
                }
            }

            setPreferredSize(new Dimension(config.width, config.height));
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.pack();
            }

            selectedElement = null;
            draggedElement = null;
            updateScaleSlider();
            repaint();
        }

        private MenuElement parseImageElement(String line) {
            try {
                String path = line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
                String[] parts = line.split(",");
                double x = Double.parseDouble(parts[1].trim());
                double y = Double.parseDouble(parts[2].trim());
                double w = Double.parseDouble(parts[3].trim());
                double h = Double.parseDouble(parts[4].trim());
                return new MenuElement(MenuElement.ElementType.IMAGE, path, x, y, w, h);
            } catch (Exception e) {
                return null;
            }
        }

        private MenuElement parseTextElement(String line) {
            try {
                String text = line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
                String[] parts = line.split(",");
                double x = Double.parseDouble(parts[1].trim());
                double y = Double.parseDouble(parts[2].trim());
                int size = Integer.parseInt(parts[3].trim());
                return new MenuElement(text, x, y, size);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (config.useBackgroundImage && backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, config.width, config.height, null);
            } else {
                g2d.setColor(config.backgroundColor);
                g2d.fillRect(0, 0, config.width, config.height);
            }

            for (MenuElement element : elements) {
                element.render(g2d);
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
                    dragOffset = new Point((int) (p.x - elementX), (int) (p.y - elementY));

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
                draggedElement.setX(newX);
                draggedElement.setY(newY);
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            draggedElement = null;
        }

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseMoved(MouseEvent e) {}
    }
}

