package com.clashwars.cwstats.stats.internal;

import java.util.HashMap;

public class StatsData {

    private HashMap<Integer, Float> statData = new HashMap<Integer, Float>();

    public StatsData() {
        //--
    }

    public HashMap<Integer, Float> getData() {
        return statData;
    }

    public float get(int statID) {
        if (statData.containsKey(statID)) {
            return statData.get(statID);
        }
        return -1;
    }

    public void change(int statID, float value) {
        if (statData.containsKey(statID)) {
            statData.put(statID, statData.get(statID) + value);
        } else {
            statData.put(statID, value);
        }
    }

    public void set(int statID, float value) {
        statData.put(statID, value);
    }
}
