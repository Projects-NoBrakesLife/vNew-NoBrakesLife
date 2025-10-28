package network;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ServerLogWindow extends JFrame {
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    private SimpleDateFormat dateFormat;
    
    public ServerLogWindow() {
        initializeWindow();
        createComponents();
        centerWindow();
        
        dateFormat = new SimpleDateFormat("HH:mm:ss");
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
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        clearButton = new JButton("Clear Log");
        clearButton.addActionListener(_ -> {
            logArea.setText("");
            addLog("Log cleared");
        });
        buttonPanel.add(clearButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameServer server = new GameServer(8888);
            
            new Thread(() -> server.start()).start();
        });
    }
}

