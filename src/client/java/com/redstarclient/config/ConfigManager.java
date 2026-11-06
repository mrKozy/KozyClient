// ☭ Red Star Client — ConfigManager
package com.redstarclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redstarclient.RedStarClient;
import com.redstarclient.module.Module;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads and saves module states and settings to JSON config profiles.
 * Config folder is placed in C:\Redstar Client configs (with graceful fallback).
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CONFIG_TYPE = new TypeToken<Map<String, ModuleConfig>>() {}.getType();

    private final Path configDirectory;
    private String currentConfigName = "default";

    public ConfigManager() {
        Path preferredDir = Paths.get("C:", "Redstar Client configs");
        Path chosenDir;
        try {
            if (!Files.exists(preferredDir)) {
                Files.createDirectories(preferredDir);
            }
            chosenDir = preferredDir;
        } catch (Throwable t) {
            // Fallback to user home directory or gameDir
            String home = System.getProperty("user.home", ".");
            chosenDir = Paths.get(home, "Redstar Client configs");
            try {
                Files.createDirectories(chosenDir);
            } catch (IOException ignored) {}
        }
        this.configDirectory = chosenDir;
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }

    public String getCurrentConfigName() {
        return currentConfigName;
    }

    public List<String> listConfigs() {
        List<String> list = new ArrayList<>();
        File folder = configDirectory.toFile();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    list.add(name.substring(0, name.length() - 5));
                }
            }
        }
        if (list.isEmpty()) {
            list.add("default");
        }
        Collections.sort(list);
        return list;
    }

    public Path resolveConfigPath(String name) {
        String cleanName = name.trim();
        if (cleanName.toLowerCase().endsWith(".json")) {
            cleanName = cleanName.substring(0, cleanName.length() - 5);
        }
        if (cleanName.isEmpty()) cleanName = "default";
        return configDirectory.resolve(cleanName + ".json");
    }

    /** Persists all module configs to active profile. */
    public void save() {
        saveConfig(currentConfigName);
    }

    public boolean saveConfig(String name) {
        if (name == null || name.trim().isEmpty()) name = "default";
        this.currentConfigName = name.trim();
        Path targetFile = resolveConfigPath(currentConfigName);

        Map<String, ModuleConfig> data = new HashMap<>();
        for (Module m : RedStarClient.MODULES.getAll()) {
            data.put(m.getName(), m.toConfig());
        }

        try {
            String json = GSON.toJson(data);
            Files.writeString(targetFile, json, StandardCharsets.UTF_8);
            System.out.println("[☭ Red Star Client] Config saved to: " + targetFile.toAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("[☭ Red Star Client] Failed to save config: " + e.getMessage());
            return false;
        }
    }

    /** Loads active config or disables all modules if no config exists. */
    public void load() {
        loadConfig(currentConfigName);
    }

    public boolean loadConfig(String name) {
        if (name == null || name.trim().isEmpty()) name = "default";
        this.currentConfigName = name.trim();
        Path targetFile = resolveConfigPath(currentConfigName);

        // If file does not exist, ensure all modules are disabled (first launch / no config)
        if (!Files.exists(targetFile)) {
            for (Module m : RedStarClient.MODULES.getAll()) {
                m.setEnabled(false);
            }
            return false;
        }

        try {
            String json = Files.readString(targetFile, StandardCharsets.UTF_8);
            Map<String, ModuleConfig> data = GSON.fromJson(json, CONFIG_TYPE);
            if (data == null) return false;

            // Reset all modules first to cleanly apply the profile
            for (Module m : RedStarClient.MODULES.getAll()) {
                m.setEnabled(false);
            }

            for (Map.Entry<String, ModuleConfig> entry : data.entrySet()) {
                Module m = RedStarClient.MODULES.get(entry.getKey());
                if (m != null) {
                    try {
                        m.fromConfig(entry.getValue());
                    } catch (Throwable t) {
                        System.err.println("[☭ Red Star Client] Error loading module config for " + entry.getKey() + ": " + t.getMessage());
                    }
                }
            }
            System.out.println("[☭ Red Star Client] Config loaded from: " + targetFile.toAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("[☭ Red Star Client] Failed to load config: " + e.getMessage());
            return false;
        }
    }
}
