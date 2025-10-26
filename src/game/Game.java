
package game;

import javax.swing.*;

public class Game {
    private GameWindow window;
    
    public Game() {
        initializeComponents();
    }
    
    private void initializeComponents() {
        window = new GameWindow();
    }
    
    public void start() {
        SwingUtilities.invokeLater(() -> {
            window.setVisible(true);
        });
    }
}

