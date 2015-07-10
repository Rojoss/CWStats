package com.clashwars.cwstats.player;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerSettings {

    public boolean statsDirect = false;

    public int stat_categorySelected = 1;
    public int stat_statSelected = 1;
    public UUID stat_lookupPlayer = null;
    public UUID stat_comparePlayer = null;
    public Timestamp stat_firstTime = null;
    public Timestamp stat_secondTime = null;

    public PlayerSettings() {
        //--
    }

}
