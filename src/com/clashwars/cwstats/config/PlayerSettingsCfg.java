package com.clashwars.cwstats.config;

import com.clashwars.cwcore.config.internal.EasyConfig;
import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.player.PlayerSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettingsCfg extends EasyConfig {

    public HashMap<String, String> SETTINGS = new HashMap<String, String>();

    public PlayerSettingsCfg(String fileName) {
        this.setFile(fileName);
    }


    public Map<UUID, PlayerSettings> getAllSettings() {
        Map<UUID, PlayerSettings> settings = new HashMap<UUID, PlayerSettings>();
        for (String key : SETTINGS.keySet()) {
            settings.put(UUID.fromString(key), CWStats.inst().getGson().fromJson(SETTINGS.get(key), PlayerSettings.class));
        }
        return settings;
    }

    public PlayerSettings getSettings(UUID uuid) {
        if (!SETTINGS.containsKey(uuid.toString())) {
            return new PlayerSettings();
        }
        return CWStats.inst().getGson().fromJson(SETTINGS.get(uuid.toString()), PlayerSettings.class);
    }

    public void setSettings(UUID uuid, PlayerSettings settings) {
        SETTINGS.put(uuid.toString(), CWStats.inst().getGson().toJson(settings, PlayerSettings.class));
        save();
    }
}
