package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    private static final Map<String, String> envData = new HashMap<>();

    static {
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    envData.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println(".env dosyası okunamadı: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return envData.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        return envData.getOrDefault(key, defaultValue);
    }
}
