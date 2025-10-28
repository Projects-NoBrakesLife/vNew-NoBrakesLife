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
            Process process = Runtime.getRuntime().exec("git describe --tags --always");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                version = reader.readLine();
                if (version != null && !version.isEmpty()) {
                    return version.trim();
                }
            }
        } catch (Exception e) {
        }
        
        try {
            Process process = Runtime.getRuntime().exec("git rev-list --count HEAD");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String count = reader.readLine();
                if (count != null && !count.isEmpty()) {
                    version = "1." + count.trim();
                    return version;
                }
            }
        } catch (Exception e) {
        }
        
        version = "1.0";
        return version;
    }
}

