package com.clashwars.cwstats.stats.display;


import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.player.PlayerSettings;
import com.clashwars.cwstats.util.ItemMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerMenu implements Listener {

    private CWStats cws;
    public Map<Integer, ItemMenu> menus = new HashMap<Integer, ItemMenu>();
    private CWItem air = new CWItem(Material.AIR);

    private HashMap<UUID, Integer> playerModes = new HashMap<UUID, Integer>();
    private HashMap<UUID, String> nameInputs = new HashMap<UUID, String>();

    private int playersPerPage = 36;

    public PlayerMenu(CWStats cws) {
        this.cws = cws;
        createMenuIfNeeded();
    }

    private void createMenuIfNeeded() {
        Collection<Player> players = (Collection<Player>)Bukkit.getOnlinePlayers();
        int pagesNeeded = (int)Math.ceil((float)players.size() / playersPerPage);

        if (pagesNeeded > menus.size()) {
            //Create new pages
            for (int i = menus.size() + 1; i <= pagesNeeded; i++) {
                ItemMenu menu = new ItemMenu("player_list_menu_" + i, 54, CWUtil.integrateColor("&4&lPlayer Select " + i));

                menu.setSlot(new CWItem(Material.PAPER).hideTooltips().setName("&6&lINFORMATION").setLore(new String[] {"&7Click on any of the heads to",
                        "&7select that player!", "&7Click on the nametag to enter a username in chat.", "&7Click on the white head to reset.", "&7Use the arrows to navigate pages."}), 0, null);

                menu.setSlot(new CWItem(Material.NAME_TAG).hideTooltips().setName("&3&lSEARCH").setLore(new String[] {"&7Search/enter a specific name.",
                        "&7This menu will close.", "&7And you have to &atype the name in chat&7!", "&7When you do this menu will show again."}), 4, null);
                menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setSkullOwner("cy1337").setName("&c&lRESET").setLore(new String[] {"&7Reset the player back to default.",
                        "&7For the lookup player it will be you.", "&7And for the compare player it will be blank."}), 5, null);
                menu.setSlot(new CWItem(Material.REDSTONE_BLOCK).hideTooltips().setName("&4&lBACK").setLore(new String[] {"&7Go back to the previous menu."}), 8, null);

                menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setName("&7&lPrevious Page").setSkullOwner("MHF_ArrowLeft").setLore(new String[] {"&7Go to the previous page."}), 9, null);
                menu.setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setName("&7&lNext Page").setSkullOwner("MHF_ArrowRight").setLore(new String[] {"&7Go to the next page."}), 17, null);


                Integer[] dividers = new Integer[] {10,11,12,13,14,15,16};
                for (int dividerSlot : dividers) {
                    menu.setSlot(new CWItem(Material.STAINED_GLASS_PANE, 1, (byte)15).setName("&8-----").setLore(new String[] {"&7Hover over the paper", "&7for information!"}), dividerSlot, null);
                }
                menus.put(i, menu);
            }
        } else if (pagesNeeded < menus.size()) {
            //Remove pages
            int pagesToRemove = menus.size() - pagesNeeded;
            for (int i = menus.size(); i > pagesToRemove; i--) {
                Set<Inventory> invs = menus.get(i).getOpenInventories();
                for (Inventory inv : invs) {
                    List<HumanEntity> viewers = inv.getViewers();
                    for (HumanEntity viewer : viewers) {
                        viewer.closeInventory();
                    }
                }
                menus.remove(i);
            }
        }
    }

    public void updatePlayerPreview(Player player, String previewSkull) {
        for (ItemMenu menu : menus.values()) {
            menu.setSlot(new CWItem(Material.SKULL_ITEM).setName("&6&l" + previewSkull).setSkullOwner(previewSkull).setLore(new String[] {"&7This is your selected player."}), 1, player);
        }
    }

    public void updatePlayerList() {
        Collection<Player> players = (Collection<Player>)Bukkit.getOnlinePlayers();
        int i = 0;
        int page = 1;
        for (Player player : players) {
            if (menus.containsKey(page)) {
                menus.get(page).setSlot(new CWItem(Material.SKULL_ITEM).hideTooltips().setName("&6&l" + player.getName()).setSkullOwner(player.getName()).setLore(new String[] {
                        "&7Click to select " + player.getName() + "!"}), i + 18, null);
            }

            i++;
            if (i % playersPerPage == 0) {
                page++;
                i = 0;
            }
        }

        if (i < playersPerPage) {
            for (int ii = i; ii < playersPerPage; ii++) {
                menus.get(page).setSlot(air, ii + 18, null);
            }
        }
    }

    public void showMenu(Player player, int page, int mode) {
        if (mode >= 0) {
            playerModes.put(player.getUniqueId(), mode);
        }

        createMenuIfNeeded();

        player.closeInventory();
        page = Math.max(Math.min(page, menus.size()), 1);
        if (menus.containsKey(page)) {
            menus.get(page).show(player);
        } else {
            menus.values().iterator().next().show(player);
        }

        updatePlayerList();

        if (nameInputs.containsKey(player.getUniqueId())) {
            String name = nameInputs.get(player.getUniqueId());
            if (!name.isEmpty()) {
                updatePlayerPreview(player, name);
            }
        } else {
            updatePlayerPreview(player, player.getName());
        }
    }

    @EventHandler
    private void menuClick(ItemMenu.ItemMenuClickEvent event) {
        Long t = System.currentTimeMillis();
        if (menus.size() < 1) {
            return;
        }

        boolean match = false;
        int page = 1;
        for (int pageID : menus.keySet()) {
            ItemMenu menu = menus.get(pageID);
            if (!event.getItemMenu().getName().equals(menu.getName())) {
                continue;
            }
            if (event.getItemMenu().getID() != menu.getID()) {
                continue;
            }
            match = true;
            page = pageID;
            break;
        }

        if (!match) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        PlayerSettings settings = cws.settingsCfg.getSettings(uuid);
        CWItem item = new CWItem(event.getCurrentItem());

        if (item == null) {
            return;
        }

        event.setCancelled(true);
        int slot = event.getRawSlot();

        int mode = 0;
        if (playerModes.containsKey(uuid)) {
            mode = playerModes.get(uuid);
        }

        if (slot == 4) {
            //Search/enter name
            player.closeInventory();
            player.sendMessage(CWUtil.integrateColor("&8-----------------------------------------"));
            player.sendMessage(CWUtil.integrateColor("&aWrite a name in the chat!"));
            player.sendMessage(CWUtil.integrateColor("&7&o(This message won't be send to others)"));
            player.sendMessage(CWUtil.integrateColor("&8-----------------------------------------"));
            nameInputs.put(uuid, "");
        } else if (slot == 5) {
            //Reset
            if (mode == 0) {
                settings.stat_lookupPlayer = uuid;
            } else if (mode == 1) {
                settings.stat_comparePlayer = null;
            }
            cws.settingsCfg.setSettings(uuid, settings);

            if (nameInputs.containsKey(uuid)) {
                nameInputs.remove(uuid);
            }

            cws.sm.statsMenu.showMenu(player);
        } else if (slot == 8) {
            //Back
            if (nameInputs.containsKey(uuid) && !nameInputs.get(uuid).isEmpty()) {
                OfflinePlayer selectedPlayer = cws.getServer().getOfflinePlayer(nameInputs.get(uuid));

                if (selectedPlayer != null) {
                    if (mode == 0) {
                        settings.stat_lookupPlayer = selectedPlayer.getUniqueId();
                    } else if (mode == 1) {
                        settings.stat_comparePlayer = selectedPlayer.getUniqueId();
                    }
                    cws.settingsCfg.setSettings(uuid, settings);
                }
            }

            cws.sm.statsMenu.showMenu(player);
        } else if (slot == 9) {
            //Previous
            showMenu(player, page++, -1);
        } else if (slot == 17) {
            //Next
            showMenu(player, page--, -1);
        }

        if (slot >= 18 && item.getType() == Material.SKULL_ITEM) {
            //Click on head
            OfflinePlayer clickedPlayer = cws.getServer().getOfflinePlayer(CWUtil.stripAllColor(item.getName()));
            if (clickedPlayer != null) {
                if (mode == 0) {
                    settings.stat_lookupPlayer = clickedPlayer.getUniqueId();
                } else if (mode == 1 && !clickedPlayer.getUniqueId().equals(uuid)) {
                    settings.stat_comparePlayer = clickedPlayer.getUniqueId();
                }
                cws.settingsCfg.setSettings(uuid, settings);
            }

            cws.sm.statsMenu.showMenu(player);
        }
    }

    //TODO: Check for closing inventory and save selected player.


    @EventHandler(priority = EventPriority.NORMAL)
    private void playerChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (nameInputs.containsKey(uuid) && nameInputs.get(uuid).isEmpty()) {

            String name = CWUtil.stripAllColor(event.getMessage().trim());
            OfflinePlayer offlinePlayer = cws.getServer().getOfflinePlayer(name);
            if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                event.getPlayer().sendMessage(CWUtil.integrateColor("&cInvalid player specified!"));
                event.getPlayer().sendMessage(CWUtil.integrateColor("&cReturning back to the menu..."));
                nameInputs.remove(uuid);
            } else {
                event.getPlayer().sendMessage(CWUtil.integrateColor("&aValid player specified!"));
                event.getPlayer().sendMessage(CWUtil.integrateColor("&aReturning back to the menu..."));
                nameInputs.put(uuid, name);
            }

            event.setCancelled(true);
            showMenu(event.getPlayer(), 1, -1);
        }
    }


    @EventHandler
    private void playerJoin(PlayerJoinEvent event) {
        createMenuIfNeeded();
        updatePlayerList();
    }

    @EventHandler
    private void playerQuit(PlayerQuitEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                createMenuIfNeeded();
                updatePlayerList();
            }
        }.runTaskLater(cws, 5);
    }

    @EventHandler
    private void playerKick(PlayerKickEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                createMenuIfNeeded();
                updatePlayerList();
            }
        }.runTaskLater(cws, 5);
    }
}
