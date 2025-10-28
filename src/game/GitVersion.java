package game;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GitVersion {
    private static String version = null;
    
    public static String getVersion() {
        if (version != null) {
            return version;
        }
        
        try {
            Process process = Runtime.getRuntime().exec("git rev-parse --short HEAD");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                version = reader.readLine();
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (Exception e) {
        }
        
        version = "dev";
        return version;
    }
}

