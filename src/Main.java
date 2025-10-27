import game.*;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        LoadingScreen loadingScreen = new LoadingScreen();
        SwingUtilities.invokeLater(() -> {
            loadingScreen.setVisible(true);
        });
    }
}
