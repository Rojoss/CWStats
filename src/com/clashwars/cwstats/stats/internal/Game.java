package com.clashwars.cwstats.stats.internal;

import java.sql.Timestamp;

public class Game {

    public int game_id;
    public Timestamp date;
    public String game_type;

    public Game(int game_id, Timestamp date, String game_type) {
        this.game_id = game_id;
        this.date = date;
        this.game_type = game_type;
    }

}
