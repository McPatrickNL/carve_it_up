// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/config/CarveItUpConfig.java
package nl.patrick.carve_it_up.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Universal JSON-based configuration system for Carve It Up.
 * Reads and writes config/carve_it_up.json using standard Gson, compatible with all modloaders.
 */
public class CarveItUpConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData instance = new ConfigData();
    private static File configFile;

    public static class ConfigData {
        public boolean hudEnabled = true;
        public int hudOffsetX = 0;
        public int hudOffsetY = 0;
        public boolean showActionOverlay = true;
        public int defaultGridResolution = 16;
        public double maxCarveReachDistance = 6.0;
        public boolean survivalRequiresMaterials = true;
    }

    public static void initialize(Path configDirectory) {
        configFile = configDirectory.resolve("carve_it_up.json").toFile();
        load();
    }

    public static ConfigData get() {
        if (instance == null) {
            instance = new ConfigData();
        }
        return instance;
    }

    public static void load() {
        if (configFile == null) {
            configFile = new File("config/carve_it_up.json");
        }

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    instance = loaded;
                    return;
                }
            } catch (Exception e) {
                System.err.println("[CarveItUp] Failed to load config file: " + e.getMessage());
            }
        }

        // If file doesn't exist or loading failed, save defaults
        save();
    }

    public static void save() {
        if (configFile == null) {
            configFile = new File("config/carve_it_up.json");
        }

        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            System.err.println("[CarveItUp] Failed to save config file: " + e.getMessage());
        }
    }
}
