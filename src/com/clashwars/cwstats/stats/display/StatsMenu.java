package com.clashwars.cwstats.stats.display;

import com.clashwars.cwcore.ItemMenu;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.player.PlayerSettings;
import com.clashwars.cwstats.stats.internal.CachedStat;
import com.clashwars.cwstats.stats.internal.Stat;
import com.clashwars.cwstats.stats.internal.StatCategory;
import com.clashwars.cwstats.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class StatsMenu implements Listener {

    private CWStats cws;
    public ItemMenu menu;
    private CWItem air = new CWItem(Material.AIR);

    List<Integer> categorySlots = Arrays.asList(new Integer[] {1,9,10,18,19,27,28,36,37,45,46});
    List<Integer> statSlots = Arrays.asList(new Integer[] {22,23,24,25,26,30,31,32,33,34,35,39,40,41,42,43,44,48,49,50,51,52,53});

    private HashMap<UUID, Set<CachedStat>> userStats = new HashMap<>();

    public StatsMenu(CWStats cws) {
        this.cws = cws;
        menu = new ItemMenu("stats_display_menu", 54, CWUtil.integrateColor("&4&lStats Display"));

        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lCATEGORIES").setLore(new String[] {"&7These are the different stat categories",
                "&7Click one of them to see the stats of that category"}), 0, null);
        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lINFORMATION").setLore(new String[] {"&7Below you can see all the stats", "&7of the category your selected left.",
                "&7Click on the skulls right to change players.", "&7The second skull can be set to compare stats.", "&7Click on the hopper to go back to filtering."}), 3, null);
        menu.setSlot(new CWItem(Material.HOPPER).hideTooltips().setName("&4&lBACK").setLore(new String[] {"&7Go back to the filter menu."}), 8, null);

        Integer[] dividers = new Integer[] {2,11,20,29,38,47,12,13,14,15,16,17};
        for (int dividerSlot : dividers) {
            menu.setSlot(new CWItem(Material.STAINED_GLASS_PANE, 1, (byte)15).hideTooltips().setName("&8-----").setLore(new String[] {"&7Hover over the paper", "&7for information!"}), dividerSlot, null);
        }
    }

    public void showMenu(Player player) {
        PlayerSettings settings = cws.settingsCfg.getSettings(player.getUniqueId());

        player.closeInventory();
        menu.show(player);

        if (settings.stat_lookupPlayer == null) {
            settings.stat_lookupPlayer = player.getUniqueId();
            cws.settingsCfg.setSettings(player.getUniqueId(), settings);
        }

        List<Integer> gameIds = new ArrayList<>();
        if (settings.stat_firstTime == null && settings.stat_secondTime == null) {
            //Local stats (if both times are null)
        } else if ((settings.stat_firstTime == null || settings.stat_secondTime == null) || settings.stat_firstTime.equals(settings.stat_secondTime)) {
            //Stats from specific game (if one time is null or if both are the same)
            gameIds.add(cws.dm.getClosestGame(settings.stat_firstTime).game_id);
        } else {
            //Stats between two times. (both times are set)
            gameIds.addAll(cws.dm.getGamesBetween(settings.stat_firstTime, settings.stat_secondTime));
        }

        userStats.put(player.getUniqueId(), cws.sm.getUserStats(player, gameIds));
        if (settings.stat_comparePlayer != null) {
            userStats.put(settings.stat_comparePlayer, cws.sm.getUserStats(settings.stat_comparePlayer, gameIds));
        }

        updateCategories(player, settings.stat_categorySelected);
        updatePlayerHead(player, CWUtil.getName(settings.stat_lookupPlayer));
        updateCompareHead(player, CWUtil.getName(settings.stat_comparePlayer));
        updateStats(player);
    }

    private void updateCategories(Player player, int activeCategory) {
        StatCategory[] statCats = cws.dm.getCats();
        for  (int i = 0; i < statCats.length && i < categorySlots.size(); i++) {
            if (activeCategory == statCats[i].category_id) {
                menu.setSlot(statCats[i].item.clone().setName("&6" + statCats[i].name).setLore(new String[] {"&aClick &7to select this category!", "&0" + statCats[i].category_id}).makeGlowing(), categorySlots.get(i), player);
            } else {
                menu.setSlot(statCats[i].item.clone().setName("&6" + statCats[i].name).setLore(new String[] {"&aClick &7to select this category!", "&0" + statCats[i].category_id}), categorySlots.get(i), player);
            }
        }
    }

    private void updatePlayerHead(Player player, String skullOwner) {
        if (skullOwner == null || player.getName().equals(skullOwner)) {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&6&l" + skullOwner).setLore(new String[] {"&7You are displaying your own stats.",
                    "&aclick &7to view another player his stats!"}), 5, player);
        } else {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&6&l" + skullOwner).setLore(new String[] {"&7You are displaying " + skullOwner + " his stats.",
                    "&aclick &7to switch to another player his stats!"}), 5, player);
        }
    }

    private void updateCompareHead(Player player, String skullOwner) {
        if (skullOwner == null || skullOwner.equalsIgnoreCase("cy1337")) {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&c&lNot Comparing").setLore(new String[] {"&7You are not comparing stats.",
                    "&aclick &7to select a player, and compare stats!"}), 6, player);
        } else {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner(skullOwner).setName("&6&l" + skullOwner).setLore(new String[] {"&7You are comparing stats with " + skullOwner + "!",
                    "&aclick &7to select another player!", "&7&o(Hover over the items to see the difference", "&7&oalso, glowing items are better than " + skullOwner + "!)"}), 6, player);
        }
    }

    private void updateStats(Player player) {
        PlayerSettings settings = cws.settingsCfg.getSettings(player.getUniqueId());

        List<Stat> stats = cws.dm.getStats(settings.stat_categorySelected);
        if (stats == null || stats.size() < 1) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
            player.sendMessage(Util.formatMsg("&4Stats aren't synced yet. &7Please wait and try again!"));
            return;
        }

        //Get all the stats
        HashMap<Integer, Float> playerStatValues = new HashMap<Integer, Float>();
        HashMap<Integer, Float> compareStatValues = new HashMap<Integer, Float>();
        for (Stat stat : stats) {
            float statValue = 0;
            if (settings.stat_firstTime == null && settings.stat_secondTime == null) {
                statValue = cws.sm.getLocalStatVal(settings.stat_lookupPlayer, stat.stat_id);
            } else {
                Set<CachedStat> cachedStats = userStats.get(player.getUniqueId());
                if (cachedStats != null) {
                    for (CachedStat cachedStat : cachedStats) {
                        if (cachedStat.stat_id == stat.stat_id) {
                            statValue += cachedStat.value;
                        }
                    }
                }
            }
            playerStatValues.put(stat.stat_id, statValue);

            if (settings.stat_comparePlayer != null) {
                statValue = 0;
                if (settings.stat_firstTime == null && settings.stat_secondTime == null) {
                    statValue = cws.sm.getLocalStatVal(settings.stat_comparePlayer, stat.stat_id);
                } else {
                    Set<CachedStat> cachedStats = userStats.get(settings.stat_comparePlayer);
                    if (cachedStats != null) {
                        for (CachedStat cachedStat : cachedStats) {
                            if (cachedStat.stat_id == stat.stat_id) {
                                statValue += cachedStat.value;
                            }
                        }
                    }
                }
                compareStatValues.put(stat.stat_id, statValue);
            }
        }

        if (playerStatValues == null || playerStatValues.size() < 1) {
            cws.sm.filterMenu.showMenu(player);
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
            player.sendMessage(Util.formatMsg("&4No stats found! &7Please check your filter settings."));
            return;
        }

        playerStatValues = cws.sm.calculateStats(playerStatValues);
        compareStatValues = cws.sm.calculateStats(compareStatValues);

        //Display all the stats.
        int i;
        int iSub = 0;
        for (i = 0; i < stats.size() && i < statSlots.size(); i++) {
            Stat stat = stats.get(i);
            //Don't display stats that have displayed set to false.
            if (stat == null || !stat.displayed) {
                iSub++;
                continue;
            }

            CWItem item = stat.item.clone().setName("&6" + stat.name);

            float statValue = 0;
            if (playerStatValues != null && playerStatValues.containsKey(stat.stat_id)) {
                statValue = playerStatValues.get(stat.stat_id);
            }
            float statValueCompare = 0;
            if (compareStatValues != null && compareStatValues.containsKey(stat.stat_id)) {
                statValueCompare = compareStatValues.get(stat.stat_id);
            }

            String valueStr = "";
            if (stat.name.toLowerCase().contains("[server stat]")) {
                valueStr += "&a&l" + valueToStr(stat.stat_id, statValue);
            } else {
                if (statValue > statValueCompare) {
                    if (settings.stat_comparePlayer != null) {
                        valueStr += "&a&l" + valueToStr(stat.stat_id, statValue) + " &7<> &c" + valueToStr(stat.stat_id, statValueCompare);
                        item.makeGlowing();
                    } else {
                        valueStr += "&a&l" + valueToStr(stat.stat_id, statValue);
                    }
                } else if (statValue == statValueCompare) {
                    if (settings.stat_comparePlayer != null) {
                        valueStr += "&6&l" + valueToStr(stat.stat_id, statValue) + " &7<> &6&l" + valueToStr(stat.stat_id, statValueCompare);
                    } else {
                        valueStr += "&6&l" + valueToStr(stat.stat_id, statValue);
                    }
                } else {
                    valueStr += "&c" + valueToStr(stat.stat_id, statValue) + " &7<> &a&l" + valueToStr(stat.stat_id, statValueCompare);
                }
            }

            item.addLore("&8Value&7: &a&l" + valueStr);
            item.addLore("&7&o" + stat.description);


            //Update the stat item.
            menu.setSlot(item.hideTooltips(), statSlots.get(i - iSub), player);
        }

        //Clear remaining slots
        i -= iSub;
        for (; i < statSlots.size(); i++) {
            menu.setSlot(air, statSlots.get(i), player);
        }

        menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lSTATS").setLore(new String[] {"&7in this box you can see your stats!",
                "&7Stats are displayed based on this settings&8:",
                "&3Player&8: &b" + CWUtil.getName(settings.stat_lookupPlayer),
                "&3Compare player&8: &b" + (settings.stat_comparePlayer == null ? "&cNone" : CWUtil.getName(settings.stat_comparePlayer)),
                "&3Start date&8: &b" + (settings.stat_firstTime == null ? "&alocal &7(Current game)" : Util.timeStampToDateString(settings.stat_firstTime)),
                "&3End date&8: &b" + (settings.stat_secondTime == null ? "&alocal &7(Current game)" : Util.timeStampToDateString(settings.stat_secondTime)),
                "&3Amount of games&8: &b" + (settings.stat_secondTime == null ? "current only" : cws.dm.getGamesBetween(settings.stat_firstTime, settings.stat_secondTime).size()) }), 21, player);
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

        if (item == null) {
            return;
        }

        event.setCancelled(true);
        int slot = event.getRawSlot();
        String name = CWUtil.stripAllColor(item.getName());

        if (categorySlots.contains(slot)) {
            int selectedID = Math.max(CWUtil.getInt(CWUtil.stripAllColor(item.getLore(1))), 1);
            settings.stat_categorySelected = selectedID;
            cws.settingsCfg.setSettings(player.getUniqueId(), settings);
            updateCategories(player, selectedID);
            updateStats(player);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        }

        if (slot == 5) {
            cws.sm.playerMenu.showMenu(player, 1, 0);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        } else if (slot == 6) {
            cws.sm.playerMenu.showMenu(player, 1, 1);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        } else if (slot == 8) {
            cws.sm.filterMenu.showMenu(player);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
        }
    }

    private String valueToStr(int statID, Float value) {
        //Format times
        if (statID == 1 || statID == 78) {
            return CWUtil.formatTime(value.longValue(), "%Dd %H:%M:%S", true);
        }
        if (value == null || value == -1 || value.isInfinite() ||value.isNaN()) {
            return "-";
        }
        if (value % 1 == 0) {
            return "" + value.intValue();
        }
        if (value > 0) {
            return "" + CWUtil.round(value, 4);
        } else {
            return "" + 0;
        }
    }
}
