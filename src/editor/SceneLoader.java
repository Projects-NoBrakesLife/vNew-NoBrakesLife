package editor;

import java.util.ArrayList;
import game.GameObject;
import data.GameObjectData;

public class SceneLoader {
    public void loadFromCode(String code, EditorManager editorManager) {
        try {
            editorManager.getObjects().clear();
            
            ArrayList<GameObjectData> sceneData = parseSceneCode(code);
            
            for (GameObjectData data : sceneData) {
                GameObject obj = new GameObject(
                    data.getImagePath(),
                    data.getX(),
                    data.getY(),
                    data.getWidth(),
                    data.getHeight()
                );
                obj.setRotation(data.getRotation());
                editorManager.addObject(obj);
            }
        } catch (Exception ex) {
        }
    }
    
    private ArrayList<GameObjectData> parseSceneCode(String code) {
        ArrayList<GameObjectData> sceneData = new ArrayList<>();
        
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("sceneData.add(new GameObjectData(")) {
                try {
                    String content = line.substring(
                        line.indexOf("(") + 1,
                        line.lastIndexOf(")")
                    );
                    String[] params = content.split(",");
                    
                    if (params.length >= 6) {
                        String imagePath = params[0].replaceAll("\"", "");
                        double x = Double.parseDouble(params[1].trim());
                        double y = Double.parseDouble(params[2].trim());
                        double width = Double.parseDouble(params[3].trim());
                        double height = Double.parseDouble(params[4].trim());
                        double rotation = Double.parseDouble(params[5].trim());
                        
                        sceneData.add(new GameObjectData(imagePath, x, y, width, height, rotation));
                    }
                } catch (Exception ex) {
                }
            }
        }
        
        return sceneData;
    }
}

