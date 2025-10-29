package editor;

import game.PopupWindowEditor;
import javax.swing.SwingUtilities;

public class PopupWindowEditorMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PopupWindowEditor editor = new PopupWindowEditor();
            editor.setVisible(true);
        });
    }
}

