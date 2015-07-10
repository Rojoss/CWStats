package com.clashwars.cwstats.commands;

import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {

    private CWStats cws;

    public Commands(CWStats cws) {
        this.cws = cws;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("stats") || label.equalsIgnoreCase("stat") || label.equalsIgnoreCase("statistics")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Util.formatMsg("&cPlayer command only."));
                return true;
            }
            Player player = (Player)sender;

            if (cws.settingsCfg.getSettings(player.getUniqueId()).statsDirect) {
                cws.sm.statsMenu.showMenu(player);
            } else {
                cws.sm.filterMenu.showMenu(player);
            }
            return true;
        }

        return false;
    }
}
