package editor;

import game.PopupWindowEditor;
import javax.swing.SwingUtilities;
import java.io.File;

public class PopupWindowEditorMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PopupWindowEditor editor = new PopupWindowEditor();
            editor.setVisible(true);
            try {
                if (args != null && args.length > 1 && "--import".equalsIgnoreCase(args[0])) {
                    File f = new File(args[1]);
                    if (f.exists() && f.isFile()) {
                        editor.importCodeFromFile(f);
                        return;
                    }
                }
                // ถ้าไม่ระบุพาธ ให้เปิดหน้าต่าง Import โค้ดอัตโนมัติ
                editor.openImportDialog();
            } catch (Exception ignored) {}
        });
    }
}

