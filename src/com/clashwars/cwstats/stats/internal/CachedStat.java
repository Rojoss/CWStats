package com.clashwars.cwstats.stats.internal;

public class CachedStat {

    public int data_id;
    public int game_id;
    public int player_id;
    public int stat_id;
    public float value = 0;

    public CachedStat(int data_id, int game_id, int player_id, int stat_id, float value) {
        this.data_id = data_id;
        this.game_id = game_id;
        this.player_id = player_id;
        this.stat_id = stat_id;
        this.value = value;
    }

}
