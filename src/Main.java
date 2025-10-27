import game.*;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        SwingUtilities.invokeLater(() -> {
            mainMenu.setVisible(true);
        });
    }
}
