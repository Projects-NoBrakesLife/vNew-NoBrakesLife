package network;

import game.FontManager;
import game.GameConfig;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.io.File;

public class ServerLogWindow extends JFrame {
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    private SimpleDateFormat dateFormat;
    private JPanel playerListPanel;
    private GameServer server;
    private java.util.Timer updateTimer;
    private JLabel serverInfoLabel;
    private java.util.concurrent.ConcurrentLinkedQueue<String> logBuffer = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private javax.swing.Timer flushTimer;
    
    private final NetworkLogger.LogListener loggerListener = message -> logBuffer.offer(message);

    public ServerLogWindow() {
        initializeWindow();
        createComponents();
        centerWindow();
        
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        startPlayerListUpdate();
        NetworkLogger.getInstance().addListener(loggerListener);
        startFlushTimer();
    }
    
    public void setServer(GameServer server) {
        this.server = server;
    }
    
    private void initializeWindow() {
        setTitle("Game Server - Log");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));
        setSize(900, 600);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = JOptionPane.showConfirmDialog(
                    ServerLogWindow.this,
                    "Are you sure you want to stop the server?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void createComponents() {
        setLayout(new BorderLayout());
   
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        headerPanel.setBackground(new Color(245, 245, 245));
        serverInfoLabel = new JLabel("กำลังตรวจสอบ IP/Port...");
        serverInfoLabel.setFont(FontManager.getThaiFont(Font.BOLD, 13));
        serverInfoLabel.setForeground(new Color(60, 60, 60));
        headerPanel.add(serverInfoLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
   
        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        Border titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            "ผู้เล่นออนไลน์",
            0, 0,
            FontManager.getThaiFont(Font.BOLD, 14)
        );
        playerListPanel.setBorder(titledBorder);
        playerListPanel.setPreferredSize(new Dimension(220, 0));
        playerListPanel.setBackground(new Color(250, 250, 250));
        
        JScrollPane playerListScroll = new JScrollPane(playerListPanel);
        playerListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        playerListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
 
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        scrollPane = new JScrollPane(logArea);
        
   
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, playerListScroll);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setBackground(new Color(240, 240, 240));
        clearButton = new JButton("ล้าง Log");
        clearButton.setFont(FontManager.getThaiFont(Font.BOLD, 12));
        clearButton.setPreferredSize(new Dimension(100, 30));
        clearButton.setBackground(new Color(158, 158, 158));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);
        clearButton.addActionListener(_ -> {
            logArea.setText("");
            addLog("Log cleared");
        });
        buttonPanel.add(clearButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        updatePlayerList();
        updateServerInfo();
    }

    @Override
    public void dispose() {
        try {
            NetworkLogger.getInstance().removeListener(loggerListener);
        } catch (Exception ignored) {}
        if (flushTimer != null) flushTimer.stop();
        super.dispose();
    }

    private void updateServerInfo() {
        try {
            String ip = detectLocalIPv4();
            int port = GameConfig.SERVER_PORT;
            serverInfoLabel.setText("IP: " + ip + "    Port: " + port);
        } catch (Exception e) {
            serverInfoLabel.setText("IP/Port: ไม่สามารถตรวจสอบได้");
        }
    }

    private String detectLocalIPv4() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> ifaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                java.net.NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                java.util.Enumeration<java.net.InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    java.net.InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private void startFlushTimer() {
        flushTimer = new javax.swing.Timer(5000, _ -> flushBufferedLogs());
        flushTimer.start();
    }

    private void flushBufferedLogs() {
        if (logBuffer.isEmpty()) return;
        java.util.List<String> drained = new java.util.ArrayList<>();
        String msg;
        while ((msg = logBuffer.poll()) != null) {
            drained.add(msg);
        }
        if (drained.isEmpty()) return;
        SwingUtilities.invokeLater(() -> {
            for (String line : drained) {
                addLog(line);
            }
        });
    }
    
    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }
    
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = dateFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void startPlayerListUpdate() {
        updateTimer = new java.util.Timer(true);
        updateTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updatePlayerList());
            }
        }, 0, 500);
    }
    
    private void updatePlayerList() {
        if (server == null) {
            playerListPanel.removeAll();
            playerListPanel.revalidate();
            playerListPanel.repaint();
            return;
        }
        
        List<PlayerInfo> players = server.getPlayers();
        playerListPanel.removeAll();
        
        for (PlayerInfo player : players) {
            if (player.isConnected) {
                JPanel playerCard = createPlayerCard(player);
                playerListPanel.add(playerCard);
                playerListPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        playerListPanel.revalidate();
        playerListPanel.repaint();
    }
    
    private JPanel createPlayerCard(PlayerInfo player) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(255, 255, 255));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(240, 248, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(255, 255, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }
        });

        try {
            String iconPath = "assets" + File.separator + "ui" + File.separator + "hud" + File.separator + player.playerId + ".png";
            File iconFile = new File(iconPath);
            if (!iconFile.exists()) {
                iconFile = new File(System.getProperty("user.dir") + File.separator + iconPath);
            }
            
            if (iconFile.exists()) {
                ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImg);
                
                JLabel iconLabel = new JLabel(icon);
                iconLabel.setHorizontalAlignment(JLabel.CENTER);
                card.add(iconLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel iconLabel = new JLabel("P" + player.playerId);
            iconLabel.setHorizontalAlignment(JLabel.CENTER);
            card.add(iconLabel, BorderLayout.CENTER);
        }
        
        JLabel nameLabel = new JLabel(player.playerName, JLabel.CENTER);
        nameLabel.setFont(FontManager.getThaiFont(Font.BOLD, 13));
        nameLabel.setForeground(new Color(50, 50, 50));
        card.add(nameLabel, BorderLayout.SOUTH);
        
        JLabel hintLabel = new JLabel("(ดับเบิ้ลคลิกเพื่อแก้ไข)", JLabel.CENTER);
        hintLabel.setFont(FontManager.getThaiFont(Font.PLAIN, 10));
        hintLabel.setForeground(new Color(150, 150, 150));
        JPanel hintPanel = new JPanel(new BorderLayout());
        hintPanel.setOpaque(false);
        hintPanel.add(nameLabel, BorderLayout.CENTER);
        hintPanel.add(hintLabel, BorderLayout.SOUTH);
        card.remove(nameLabel);
        card.add(hintPanel, BorderLayout.SOUTH);
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showPlayerStatsDialog(player);
                }
            }
        });
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return card;
    }
    
    private void showPlayerStatsDialog(PlayerInfo player) {
        JDialog dialog = new JDialog(this, "แก้ไข Stats - " + player.playerName, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(250, 250, 250));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        titlePanel.setBackground(new Color(240, 240, 240));
        JLabel titleLabel = new JLabel("แก้ไข Stats - " + player.playerName);
        titleLabel.setFont(FontManager.getThaiFont(Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        dialog.add(titlePanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        form.setBackground(new Color(250, 250, 250));

        JLabel lSkill = new JLabel("ทักษะ:");
        lSkill.setFont(FontManager.getThaiFont(Font.BOLD, 14));
        JTextField fSkill = new JTextField(String.valueOf(player.skill));
        fSkill.setFont(FontManager.getThaiFont(14));

        JLabel lEdu = new JLabel("ความรู้:");
        lEdu.setFont(FontManager.getThaiFont(Font.BOLD, 14));
        JTextField fEdu = new JTextField(String.valueOf(player.education));
        fEdu.setFont(FontManager.getThaiFont(14));

        JLabel lHealth = new JLabel("สุขภาพ:");
        lHealth.setFont(FontManager.getThaiFont(Font.BOLD, 14));
        JTextField fHealth = new JTextField(String.valueOf(player.health));
        fHealth.setFont(FontManager.getThaiFont(14));

        JLabel lMoney = new JLabel("เงิน:");
        lMoney.setFont(FontManager.getThaiFont(Font.BOLD, 14));
        JTextField fMoney = new JTextField(String.valueOf(player.money));
        fMoney.setFont(FontManager.getThaiFont(14));

        form.add(lSkill); form.add(fSkill);
        form.add(lEdu); form.add(fEdu);
        form.add(lHealth); form.add(fHealth);
        form.add(lMoney); form.add(fMoney);

        dialog.add(form, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(new Color(250, 250, 250));

        JButton saveButton = new JButton("บันทึก");
        saveButton.setFont(FontManager.getThaiFont(Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(120, 35));
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(_ -> {
            try {
                int newSkill = Integer.parseInt(fSkill.getText().trim());
                int newEdu = Integer.parseInt(fEdu.getText().trim());
                int newHealth = Integer.parseInt(fHealth.getText().trim());
                int newMoney = Integer.parseInt(fMoney.getText().trim());

                player.skill = newSkill;
                player.education = newEdu;
                player.health = newHealth;
                player.money = newMoney;

                PlayerInfo tempPlayer = new PlayerInfo(player.playerId, player.playerName, player.isConnected);
                tempPlayer.skill = newSkill;
                tempPlayer.education = newEdu;
                tempPlayer.health = newHealth;
                tempPlayer.money = newMoney;

                new Thread(() -> server.broadcastPlayerStatsUpdate(tempPlayer)).start();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "กรุณากรอกตัวเลขที่ถูกต้องทุกช่อง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("ยกเลิก");
        cancelButton.setFont(FontManager.getThaiFont(Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(_ -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    private interface StatUpdateCallback {
        void update(int newValue);
    }
    
    private JPanel createStatPanel(String labelText, int initialValue, StatUpdateCallback callback) {
        JPanel panel = new JPanel(new BorderLayout(15, 5));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(FontManager.getThaiFont(Font.BOLD, 15));
        label.setForeground(new Color(60, 60, 60));
        panel.add(label, BorderLayout.WEST);
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        valuePanel.setOpaque(false);
        
        JLabel valueLabel = new JLabel(String.valueOf(initialValue));
        valueLabel.setFont(FontManager.getThaiFont(Font.BOLD, 18));
        valueLabel.setPreferredSize(new Dimension(100, 35));
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setOpaque(true);
        valueLabel.setBackground(Color.WHITE);
        valueLabel.setForeground(new Color(50, 50, 50));
        valueLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton decreaseButton = new JButton("-");
        decreaseButton.setFont(FontManager.getThaiFont(Font.BOLD, 20));
        decreaseButton.setPreferredSize(new Dimension(50, 40));
        decreaseButton.setBackground(new Color(244, 67, 54));
        decreaseButton.setForeground(Color.WHITE);
        decreaseButton.setFocusPainted(false);
        decreaseButton.setBorderPainted(false);
        decreaseButton.addActionListener(_ -> {
            try {
                int current = Integer.parseInt(valueLabel.getText());
                int newValue = Math.max(0, current - 1);
                valueLabel.setText(String.valueOf(newValue));
                callback.update(newValue);
                valuePanel.revalidate();
                valuePanel.repaint();
            } catch (Exception ex) {
                NetworkLogger.getInstance().log("Error decreasing value: " + ex.getMessage());
            }
        });
        valuePanel.add(decreaseButton);
        valuePanel.add(valueLabel);
        
        JButton increaseButton = new JButton("+");
        increaseButton.setFont(FontManager.getThaiFont(Font.BOLD, 20));
        increaseButton.setPreferredSize(new Dimension(50, 40));
        increaseButton.setBackground(new Color(76, 175, 80));
        increaseButton.setForeground(Color.WHITE);
        increaseButton.setFocusPainted(false);
        increaseButton.setBorderPainted(false);
        increaseButton.addActionListener(_ -> {
            try {
                int current = Integer.parseInt(valueLabel.getText());
                int newValue = current + 1;
                valueLabel.setText(String.valueOf(newValue));
                callback.update(newValue);
                valuePanel.revalidate();
                valuePanel.repaint();
            } catch (Exception ex) {
                NetworkLogger.getInstance().log("Error increasing value: " + ex.getMessage());
            }
        });
        valuePanel.add(increaseButton);
        
        JTextField inputField = new JTextField(String.valueOf(initialValue), 5);
        inputField.setFont(FontManager.getThaiFont(Font.PLAIN, 16));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setPreferredSize(new Dimension(100, 35));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        inputField.setVisible(false);
        inputField.addActionListener(_ -> {
            try {
                int newValue = Integer.parseInt(inputField.getText());
                valueLabel.setText(String.valueOf(newValue));
                callback.update(newValue);
                inputField.setVisible(false);
                valueLabel.setVisible(true);
            } catch (NumberFormatException e) {
                inputField.setText(valueLabel.getText());
            }
        });
        valuePanel.add(inputField);
        
        valueLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                valueLabel.setVisible(false);
                inputField.setText(valueLabel.getText());
                inputField.setVisible(true);
                inputField.requestFocus();
                inputField.selectAll();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                valueLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (valueLabel.isVisible()) {
                    valueLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                }
            }
        });
        
        panel.add(valuePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameServer server = new GameServer(8888);
            ServerLogWindow logWindow = server.getLogWindow();
            if (logWindow != null) {
                logWindow.setServer(server);
            }
            
            new Thread(() -> server.start()).start();
        });
    }
}
