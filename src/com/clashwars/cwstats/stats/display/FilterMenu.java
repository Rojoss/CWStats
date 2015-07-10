package com.clashwars.cwstats.stats.display;

import com.clashwars.cwcore.ItemMenu;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.player.PlayerSettings;
import com.clashwars.cwstats.stats.internal.Game;
import com.clashwars.cwstats.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Timestamp;
import java.util.UUID;

public class FilterMenu implements Listener {

    private CWStats cws;
    public ItemMenu menu;
    private CWItem air = new CWItem(Material.AIR);

    public FilterMenu(CWStats cws) {
        this.cws = cws;
        menu = new ItemMenu("stats_filter_menu", 45, CWUtil.integrateColor("&4&lStats Filter"));

        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lINFORMATION").setLore(new String[] {"&7Here you can specify what stats you want.",
                "&7On the left side are some general settings.", "&7And on the right side you can specify games/times.", "&6Hover over the other papers for more info!",
                "&7When something is &5glowing &7it's selected!", "&aClick &7on the &agreen dye &7to look up stats with your settings!"}), 0, null);
        menu.setSlot(new CWItem(Material.INK_SACK, 1, (byte)10).hideTooltips().setName("&a&lSUBMIT").setLore(new String[] {"&7Look up stats with the specified settings below."}), 3, null);
        menu.setSlot(new CWItem(Material.INK_SACK, 1, (byte)10).hideTooltips().setName("&a&lSUBMIT").setLore(new String[] {"&7Look up stats with the specified settings below."}), 4, null);
        menu.setSlot(new CWItem(Material.INK_SACK, 1, (byte)10).hideTooltips().setName("&a&lSUBMIT").setLore(new String[] {"&7Look up stats with the specified settings below."}), 5, null);
        menu.setSlot(new CWItem(Material.REDSTONE_BLOCK).hideTooltips().setName("&4&lCLOSE").setLore(new String[] {"&7Stop displaying stats!"}), 8, null);


        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lLOOKUP PLAYER").setLore(new String[] {"&7Here you can set the main player to look up.", "&7By default this will be you.",
                "&7But you can click it to see someone else."}), 18, null);

        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lCOMPARE PLAYER").setLore(new String[] {"&7Here you can set a player to compare stats with.", "&7By default this will be blank.",
                "&7But you can click it to set it to someone else."}), 27, null);

        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lLEADERBOARDS").setLore(new String[] {"&7These will take you to the leaderboards!", "&7Make sure you specified the time/games",
                "&7you want to see the leaderboard from on the right!"}), 36, null);
        menu.setSlot(new CWItem(Material.GOLD_BLOCK).hideTooltips().setName("&6&lLEADERBOARD").setLore(new String[] {"&7Go to the leaderboard!"}), 37, null);


        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lPREVIOUS GAMES").setLore(new String[] {"&7These represent the previous game.", "&7The green one is the current game",
                "&7and this will only work if there is a game running!"}), 22, null);
        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lPRESETS").setLore(new String[] {"&7Here you can select a predefined timespan.", "&7From left to right:",
                "&7today, last week, last month, all time"}), 31, null);
        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lRANGE").setLore(new String[] {"&7Here you can select two different games.", "&7And it will display stats from all games",
                "&7between the two chosen games!", "&7The &3left item &7represents the &3first game&7.", "&7The &bright item &7represents the &bsecond game&7.", "&7Modify it by &aleft &7and &aright clicking."}), 40, null);

        Integer[] dividers = new Integer[] {9,10,11,12,13,14,15,16,17,21,30,39};
        for (int dividerSlot : dividers) {
            menu.setSlot(new CWItem(Material.STAINED_GLASS_PANE, 1, (byte)15).hideTooltips().setName("&8-----").setLore(new String[] {"&7Hover over the paper", "&7for information!"}), dividerSlot, null);
        }
    }

    public void showMenu(Player player) {
        PlayerSettings settings = cws.settingsCfg.getSettings(player.getUniqueId());

        if (settings.stat_lookupPlayer == null) {
            settings.stat_lookupPlayer = player.getUniqueId();
            cws.settingsCfg.setSettings(player.getUniqueId(), settings);
        }

        player.closeInventory();
        menu.show(player);

        updatePlayerHead(player, CWUtil.getName(settings.stat_lookupPlayer));
        updateCompareHead(player, CWUtil.getName(settings.stat_comparePlayer));

        //Set time option based of settings.
        if (settings.stat_firstTime == null && settings.stat_secondTime == null) {
            //Current game
            updateDates(player, 23);
        } else if ((settings.stat_firstTime == null || settings.stat_secondTime == null) || settings.stat_firstTime.equals(settings.stat_secondTime)) {
            //Certain game
            Game[] games = cws.dm.getGames();
            int highest = 0;
            for (Game game : games) {
                if (game.game_id > highest) {
                    highest = game.game_id;
                }
            }

            Game closestGame = cws.dm.getClosestGame(settings.stat_firstTime);
            if (closestGame.game_id == highest) {
                updateDates(player, 24);
            } else if (closestGame.game_id == highest-1) {
                updateDates(player, 25);
            } else if (closestGame.game_id == highest-2) {
                updateDates(player, 26);
            } else {
                updateDates(player, 41);
            }
        } else {
            //Timespan
            Long timeDiff = settings.stat_secondTime.getTime() - settings.stat_firstTime.getTime();
            int msDay = 1000 * 60 * 60 * 24;
            if (timeDiff == msDay) {
                updateDates(player, 32);
            } else if (timeDiff == msDay * 7) {
                updateDates(player, 33);
            } else if (timeDiff == msDay * 30) {
                updateDates(player, 34);
            } else if (settings.stat_firstTime.equals(Timestamp.valueOf("2015-01-01 00:00:00.0"))) {
                updateDates(player, 35);
            } else {
                updateDates(player, 41);
            }
        }
    }


    private void updatePlayerHead(Player player, String skullOwner) {
        if (skullOwner == null || player.getName().equals(skullOwner)) {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&6&l" + skullOwner).setLore(new String[] {"&7You will display your own stats.",
                    "&aclick &7to select another player!"}), 19, player);
        } else {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&6&l" + skullOwner).setLore(new String[] {"&7You will display " + skullOwner + " his stats.",
                    "&aclick &7to select another player!"}), 19, player);
        }
    }

    private void updateCompareHead(Player player, String skullOwner) {
        if (skullOwner == null || skullOwner.equalsIgnoreCase("cy1337")) {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&c&lNo compare player").setLore(new String[] {"&7You will not compare stats.",
                    "&aclick &7to select a player!"}), 28, player);
        } else {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&6&l" + skullOwner).setLore(new String[] {"&7You will compare stats with " + skullOwner + "!",
                    "&aclick &7to select another player!"}), 28, player);
        }
    }

    private void updateDates(Player player, int selectedSlot) {
        PlayerSettings settings = cws.settingsCfg.getSettings(player.getUniqueId());
        CWItem item;

        //Previous games
        item = new CWItem(Material.STAINED_CLAY, 1, (byte)5).hideTooltips().setName("&2Current Game").setLore(new String[] {"&7View &alocal stats &7from the current game.",
                        "&7This will only work if there is a game running!", "&7When the game ends these local stats will be uploaded.", "&cAll other options won't include these local stats!"});
        if (selectedSlot == 23)
            item.makeGlowing();
        menu.setSlot(item, 23, player);

        item = new CWItem(Material.STAINED_CLAY, 1, (byte)4).hideTooltips().setName("&2Previous game").setLore(new String[] {"&7View stats from the previous game!"});
        if (selectedSlot == 24)
            item.makeGlowing();
        menu.setSlot(item, 24, player);

        item = new CWItem(Material.STAINED_CLAY, 1, (byte)1).hideTooltips().setName("&22 games back").setLore(new String[] {"&7iew stats from 2 games back."});
        if (selectedSlot == 25)
            item.makeGlowing();
        menu.setSlot(item, 25, player);

        item = new CWItem(Material.STAINED_CLAY, 1, (byte)14).hideTooltips().setName("&23 games back").setLore(new String[] {"&7View stats from 3 games back."});
        if (selectedSlot == 26)
            item.makeGlowing();
        menu.setSlot(item, 26, player);


        //Presets
        item = new CWItem(Material.STAINED_CLAY, 1, (byte)15).hideTooltips().setName("&2Today").setLore(new String[] {"&7View all stats from today.", "&7If there hasn't been a game today",
                "&7there might be no stats!"});
        if (selectedSlot == 32)
            item.makeGlowing();
        menu.setSlot(item, 32, player);

        item = new CWItem(Material.STAINED_CLAY, 1, (byte)7).hideTooltips().setName("&2Last week").setLore(new String[] {"&7View all stats from last week.", "&7It will include all games between",
                "&7now and 7 days ago!"});
        if (selectedSlot == 33)
            item.makeGlowing();
        menu.setSlot(item, 33, player);

        item = new CWItem(Material.STAINED_CLAY, 1, (byte)8).hideTooltips().setName("&2Last month").setLore(new String[] {"&7View all stats from last month.", "&7It will include all games between",
                "&7now and 30 days ago!"});
        if (selectedSlot == 34)
            item.makeGlowing();
        menu.setSlot(item, 34, player);

        item = new CWItem(Material.STAINED_CLAY, 1, (byte)0).hideTooltips().setName("&2&lALL TIME").setLore(new String[] {"&7View all stats from all time.", "&7Data might get erased after a long time",
                "&7but in general this are all your stats!"});
        if (selectedSlot == 35)
            item.makeGlowing();
        menu.setSlot(item, 35, player);


        //Range
        if (settings.stat_firstTime != null && settings.stat_secondTime != null) {
            item = new CWItem(Material.STAINED_CLAY, 1, (byte)10).hideTooltips().setName("&2&lRANGE").setLore(new String[] {"&7Time 1&8: &a" + CWUtil.timeStampToDateString(settings.stat_firstTime),
                    "&7Time 2&8: &a" + CWUtil.timeStampToDateString(settings.stat_secondTime),
                    "&7Games&8: &6" + cws.dm.getGamesBetween(settings.stat_firstTime, settings.stat_secondTime).size()});
        } else {
            item = new CWItem(Material.STAINED_CLAY, 1, (byte)10).hideTooltips().setName("&6Range").setLore(new String[] {"&cNo range specified!"});
        }
        if (selectedSlot == 41)
            item.makeGlowing();
        menu.setSlot(item, 41, player);

        if (settings.stat_firstTime != null && settings.stat_secondTime != null) {
            item = new CWItem(Material.STAINED_CLAY, 1, (byte)6).hideTooltips().setName("&2" + CWUtil.timeStampToDateString(settings.stat_firstTime)).setLore(new String[]{
                    "&3Left click &7to &3increase &7the date.", "&bRight click &7to &bdecrease &7the date.", "&7Hold down &ashift &7to increase/decrease per week!"});
        } else {
            item = new CWItem(Material.STAINED_CLAY, 1, (byte)6).hideTooltips().setName("&2First time").setLore(new String[]{
                    "&3Left click &7to &3increase &7the date.", "&bRight click &7to &bdecrease &7the date.", "&7Hold down &ashift &7to increase/decrease per week!"});
        }
        if (selectedSlot == 41)
            item.makeGlowing();
        menu.setSlot(item, 43, player);

        if (settings.stat_firstTime != null && settings.stat_secondTime != null) {
            item = new CWItem(Material.STAINED_CLAY, 1, (byte)11).hideTooltips().setName("&2" + CWUtil.timeStampToDateString(settings.stat_secondTime)).setLore(new String[]{
                    "&3Left click &7to &3increase &7the date.", "&bRight click &7to &bdecrease &7the date.", "&7Hold down &ashift &7to increase/decrease per week!"});
        } else {
            item = new CWItem(Material.STAINED_CLAY, 1, (byte)11).hideTooltips().setName("&2Second time").setLore(new String[]{
                    "&3Left click &7to &3increase &7the date.", "&bRight click &7to &bdecrease &7the date.", "&7Hold down &ashift &7to increase/decrease per week!"});
        }
        if (selectedSlot == 41)
            item.makeGlowing();
        menu.setSlot(item, 44, player);
    }



    @EventHandler
    private void menuClick(ItemMenu.ItemMenuClickEvent event) {
        Long t = System.currentTimeMillis();
        if (menu == null) {
            return;
        }
        if (!event.getItemMenu().getName().equals(menu.getName())) {
            return;
        }
        if (event.getItemMenu().getID() != menu.getID()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        PlayerSettings settings = cws.settingsCfg.getSettings(player.getUniqueId());
        UUID uuid = player.getUniqueId();
        CWItem item = new CWItem(event.getCurrentItem());

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (event.getRawSlot() > menu.getSize()) {
            return;
        }

        event.setCancelled(true);
        int slot = event.getRawSlot();
        String name = CWUtil.stripAllColor(item.getName());


        boolean timeEdit = false;
        if (slot == 3 || slot == 4 || slot == 5) {
            //Submit buttons
            cws.sm.statsMenu.showMenu(player);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        } else if (slot == 8) {
            //Close button
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        } else if (slot == 19) {
            //Lookup player
            cws.sm.playerMenu.showMenu(player, 1, 0);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        } else if (slot == 28) {
            //Compare player
            cws.sm.playerMenu.showMenu(player, 1, 1);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        } else if (slot == 37) {
            //Leaderboard
            player.closeInventory();
            player.sendMessage(Util.formatMsg("&cLeaderboards aren't available yet."));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
            return;
        } else if (slot == 23) {
            //Current game
            settings.stat_firstTime = null;
            settings.stat_secondTime = null;
            timeEdit = true;
        } else if (slot == 24) {
            //Previous game
            Game closestGame = cws.dm.getClosestGame(new Timestamp(System.currentTimeMillis()));
            settings.stat_firstTime = closestGame.date;
            settings.stat_secondTime = closestGame.date;
            timeEdit = true;
        } else if (slot == 25) {
            //2 games ago
            Game closestGame = cws.dm.getClosestGame(new Timestamp(System.currentTimeMillis()));
            Game prevGame = cws.dm.getGame(closestGame.game_id - 1);
            if (prevGame == null) {
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                return;
            }
            settings.stat_firstTime = prevGame.date;
            settings.stat_secondTime = prevGame.date;
            timeEdit = true;
        } else if (slot == 26) {
            //3 games ago
            Game closestGame = cws.dm.getClosestGame(new Timestamp(System.currentTimeMillis()));
            Game prevGame = cws.dm.getGame(closestGame.game_id - 2);
            if (prevGame == null) {
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                return;
            }
            settings.stat_firstTime = prevGame.date;
            settings.stat_secondTime = prevGame.date;
            timeEdit = true;
        } else if (slot == 32) {
            //Today
            settings.stat_firstTime = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
            settings.stat_secondTime = new Timestamp(System.currentTimeMillis());
            timeEdit = true;
        } else if (slot == 33) {
            //Last week
            settings.stat_firstTime = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7));
            settings.stat_secondTime = new Timestamp(System.currentTimeMillis());
            timeEdit = true;
        } else if (slot == 34) {
            //Last month
            settings.stat_firstTime = new Timestamp(System.currentTimeMillis() - 2592000000l);
            settings.stat_secondTime = new Timestamp(System.currentTimeMillis());
            timeEdit = true;
        } else if (slot == 35) {
            //All time
            settings.stat_firstTime = Timestamp.valueOf("2015-01-01 00:00:00.0");
            settings.stat_secondTime = new Timestamp(System.currentTimeMillis());
            timeEdit = true;
        } else if (slot == 43 || slot == 44) {
            //Range - First date
            int val = 0;
            if (event.isLeftClick()) {
                val = 1000 * 60 * 60 * 24;
            } else if (event.isRightClick()) {
                val = -(1000 * 60 * 60 * 24);
            }
            if (event.isShiftClick()) {
                val *= 7;
            }

            if (settings.stat_firstTime == null) {
                settings.stat_firstTime = new Timestamp(System.currentTimeMillis());
            }
            if (settings.stat_secondTime == null) {
                settings.stat_secondTime = new Timestamp(System.currentTimeMillis());
            }

            if (slot == 43) {
                Timestamp newTime = new Timestamp(settings.stat_firstTime.getTime() + val);
                if (newTime.after(settings.stat_secondTime)) {
                    player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                    return;
                }
                settings.stat_firstTime = newTime;
            } else {
                Timestamp newTime = new Timestamp(settings.stat_secondTime.getTime() + val);
                if (newTime.before(settings.stat_firstTime)) {
                    player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                    return;
                }

                settings.stat_secondTime = newTime;
            }
            timeEdit = true;
            slot = 41;
        }

        if (timeEdit) {
            cws.settingsCfg.setSettings(uuid, settings);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
            updateDates(player, slot);
        }
    }

}
