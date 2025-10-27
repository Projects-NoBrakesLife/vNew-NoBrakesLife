package editor;

import game.MainMenuEditor;
import javax.swing.SwingUtilities;

public class MainMenuEditorMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenuEditor editor = new MainMenuEditor();
            editor.setVisible(true);
        });
    }
}

