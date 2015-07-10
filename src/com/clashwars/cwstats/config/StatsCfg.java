package com.clashwars.cwstats.config;

import com.clashwars.cwcore.config.internal.EasyConfig;
import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.stats.internal.StatsData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsCfg extends EasyConfig {

    public HashMap<String, String> STATS = new HashMap<String, String>();
    public String SERVER_STATS = "";

    public StatsCfg(String fileName) {
        this.setFile(fileName);
    }

    public Map<UUID, StatsData> getStats() {
        Map<UUID, StatsData> stats = new HashMap<UUID, StatsData>();
        for (String key : STATS.keySet()) {
            stats.put(UUID.fromString(key), CWStats.inst().getGson().fromJson(STATS.get(key), StatsData.class));
        }
        return stats;
    }

    public StatsData getPlayerStats(UUID uuid) {
        return CWStats.inst().getGson().fromJson(STATS.get(uuid.toString()), StatsData.class);
    }

    public void setPlayerStats(UUID uuid, StatsData sd) {
        STATS.put(uuid.toString(), CWStats.inst().getGson().toJson(sd, StatsData.class));
    }

    public void removePlayerStats(UUID uuid) {
        STATS.remove(uuid.toString());
    }

    public StatsData getServerStats() {
        return CWStats.inst().getGson().fromJson(SERVER_STATS, StatsData.class);
    }

    public void setServerStats(StatsData sd) {
        SERVER_STATS = CWStats.inst().getGson().toJson(sd, StatsData.class);
    }

    public void removeServerStats() {
        SERVER_STATS = "";
    }

    public void removeStats() {
        STATS.clear();
        save();
    }
}
