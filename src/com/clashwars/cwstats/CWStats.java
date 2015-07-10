package com.clashwars.cwstats;

import com.clashwars.clashwars.ClashWars;
import com.clashwars.cwcore.CWCore;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.mysql.MySQL;
import com.clashwars.cwstats.commands.Commands;
import com.clashwars.cwstats.config.PlayerSettingsCfg;
import com.clashwars.cwstats.config.PluginCfg;
import com.clashwars.cwstats.config.StatsCfg;
import com.clashwars.cwstats.stats.DataManager;
import com.clashwars.cwstats.stats.StatsManager;
import com.google.gson.Gson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.logging.Logger;

public class CWStats extends JavaPlugin {

    private static CWStats instance;
    private CWCore cwcore;
    private ClashWars cw;
    private Gson gson = new Gson();

    private Commands cmds;

    private MySQL sql;
    private Connection c;

    public PluginCfg pluginCfg;
    public StatsCfg statsCfg;
    public PlayerSettingsCfg settingsCfg;

    public DataManager dm;
    public StatsManager sm;

    private final Logger log = Logger.getLogger("Minecraft");


    @Override
    public void onDisable() {
        log("disabled");
    }

    @Override
    public void onEnable() {
        Long t = System.currentTimeMillis();
        instance = this;

        Plugin plugin = getServer().getPluginManager().getPlugin("CWCore");
        if (plugin == null || !(plugin instanceof CWCore)) {
            log("CWCore dependency couldn't be loaded!");
            setEnabled(false);
            return;
        }
        cwcore = (CWCore)plugin;

        plugin = getServer().getPluginManager().getPlugin("ClashWars");
        if (plugin == null || !(plugin instanceof ClashWars)) {
            log("ClashWars dependency couldn't be loaded!");
            setEnabled(false);
            return;
        }
        cw = (ClashWars)plugin;

        pluginCfg = new PluginCfg("plugins/CWStats/Config.yml");
        pluginCfg.load();
        statsCfg = new StatsCfg("plugins/CWStats/Stats.yml");
        statsCfg.load();
        settingsCfg = new PlayerSettingsCfg("plugins/CWStats/PlayerSettings.yml");
        settingsCfg.load();

        sql = new MySQL(this, "37.26.106.5", "3306", "clashwar_data", "clashwar_main", pluginCfg.SQL__PASS);
        try {
            c = sql.openConnection();
        } catch(Exception e) {
            log("##############################################################");
            log("Unable to connect to MySQL!");
            log("Stats and all other data won't be synced/stored!");
            log("The game should still be able to run fine but this message shouldn't be ignored!");
            log("##############################################################");
        }

        dm = new DataManager(this);
        sm = new StatsManager(this);

        registerEvents();

        cmds = new Commands(this);

        log("loaded successfully");
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmds.onCommand(sender, cmd, label, args);
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(sm.filterMenu, this);
        pm.registerEvents(sm.statsMenu, this);
        pm.registerEvents(sm.playerMenu, this);
    }


    public void log(Object msg) {
        log.info("[CWStats " + getDescription().getVersion() + "] " + msg.toString());
    }

    public void logError(Object msg) {
        log.severe("[CWStats " + getDescription().getVersion() + "] " + msg.toString());
    }

    public static CWStats inst() {
        return instance;
    }

    public CWCore getCore() {
        return cwcore;
    }

    public ClashWars getCW() {
        return cw;
    }




    public Gson getGson() {
        return gson;
    }

    public Connection getSql() {
        return c;
    }

}
