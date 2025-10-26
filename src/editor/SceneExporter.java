package editor;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class SceneExporter {
    public void exportToClipboard(String code) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(code);
        clipboard.setContents(selection, null);
    }
}

