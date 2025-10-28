package editor;

import game.GameEditor;
import javax.swing.SwingUtilities;

public class GameEditorMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameEditor editor = new GameEditor();
            editor.setVisible(true);
        });
    }
}

