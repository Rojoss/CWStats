package com.clashwars.cwstats.listeners;

import com.clashwars.cwstats.CWStats;
import org.bukkit.event.Listener;

public class MainEvents implements Listener {

    private CWStats cws;

    public MainEvents(CWStats cws) {
        this.cws = cws;
    }

}
