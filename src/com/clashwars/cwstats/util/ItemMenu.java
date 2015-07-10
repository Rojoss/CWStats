package com.clashwars.cwstats.util;

import com.clashwars.cwcore.helpers.CWItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Create a inventory menu with items. */
public class ItemMenu implements Listener {

    private String name;
    private int id;
    private String data;
    private int page;

    private int size;
    private String title;
    private CWItem[] items;

    private Set<Inventory> openInventories = new HashSet<Inventory>();
    static Set<ItemMenu> menus = new HashSet<ItemMenu>();


    /**
     * Create an item menu with the specified name, size and title.
     * The name is the identifier of this inventory.
     * The size is the amount of slots this inventory needs to have. This needs to be a multiplier of 9.
     * The title is displayed on top of the inventory.
     *
     * @param name  The name ID.
     * @param size  The amount of slots (multiplier of 9)
     * @param title The title displayed on top of the GUI.
     */
    public ItemMenu(String name, int size, String title) {
        this.name = name;
        this.size = size;
        this.title = title;
        this.items = new CWItem[size];

        //Generate a unique ID for this menu.
        this.id = new Random().nextInt(Integer.MAX_VALUE - 64) + 64;

        //Add the menu to the static list.
        menus.add(this);
    }



    /**
     * Get the identifier name of the menu.
     *
     * @return Name of the menu.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique ID of the menu.
     * This Id is generated when the inventory is created.
     *
     * @return Unique id
     */
    public int getID() {
        return id;
    }



    /**
     * Get a string with custom data specified with setData.
     *
     * @return String with data if set.
     */
    public String getData() {
        return data;
    }

    /**
     * Add extra data to this menu which can be useful to identify menus.
     *
     * @param data A string with custom data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get a int with the page number if it's set with setPage.
     *
     * @return int with page or -1 if not set.
     */
    public int getPage() {
        return page;
    }

    /**
     * Set the page number of this inventory.
     * This is just used to store extra data and gives no extra functionality.
     *
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }


    /**
     * Get the size of the menu. (amount of slots)
     * @return Size in slots.
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the size of the menu. (amount of slots)
     * This will only update when menus are shown to players again.
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Get the title of the menu which is displayed above the menu.
     * @return Title of the menu.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the menu to the specified title.
     * This will only update when menus are shown to players again.
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }



    /**
     * Get a list with all items in the menu.
     * The list has a item for each slot (air for empty) and the index in the array is also the slot number.
     * @return Array with CWItems.
     */
    public CWItem[] getItems() {
        return items;
    }

    /**
     * Get a set of inventories that have this menu shown.
     * @return Set<Inventory> with all open inventories.
     */
    public Set<Inventory> getOpenInventories() {
        return openInventories;
    }



    /**
     * Set/Update a item at the specified slot.
     * It will also update the menu live for all players that have the menu opened if no player is specified.
     * If a player is specified it will only update for the specified player and not update the item in the list.
     * This means that if the player doesn't have the menu opened then there wont be any changes as it's not stored anywhere.
     * So first open the menu and then you can set player specific items.
     * @param item The item to set
     * @param slot The slot number were to set the item
     * @param player If it's specified it wont change the menu but will only update the opened menu for the specified player with this item.
     */
    public void setSlot(CWItem item, int slot, Player player) {
        if (player == null) {
            this.items[slot] = item;
        }

        for (Inventory inv : openInventories) {
            if (player == null) {
                inv.setItem(slot, item);
            } else {
                if (inv.getViewers().contains(player)) {
                    inv.setItem(slot, item);
                }
            }
        }
    }

    /**
     * Set all menu items to air. (clear the menu)
     * A player can be specified to only clear items for the given player.
     * @see #setSlot(CWItem, int, Player) for more information about specifying a player.
     * @param player
     */
    public void clear(Player player) {
        for (int i = 0; i < size; i++) {
            setSlot(new CWItem(Material.AIR), i, player);
        }
    }



    /**
     * Show this menu to the specified player.
     * Any opened inventories will be closed and the menu will be filled with with the items set.
     * @param player The player to send the inventory to.
     */
    public void show(Player player) {
        player.closeInventory();

        Inventory inv = Bukkit.createInventory(player, size, title);
        inv.setMaxStackSize(id);

        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                inv.setItem(i, items[i]);
            }
        }

        player.openInventory(inv);
        openInventories.add(inv);
    }


    //Listen for inventory actions.
    public static class Events implements Listener {

        //Click Event.
        //Check for clicking on items in the menu and call the ItemMenuClickEvent.
        @EventHandler(priority = EventPriority.HIGHEST)
        public void click(InventoryClickEvent event) {
            //Since ItemMenuClickEvent extends InventoryclickEvent need to make sure it's not the custom event.
            if (event instanceof ItemMenuClickEvent) {
                return;
            }

            Player player = (Player)event.getWhoClicked();
            Inventory inv = event.getInventory();

            //Loop through all menus.
            for (ItemMenu menu : menus) {
                //Check if the clicked inventory is the current menu.
                if (inv.getMaxStackSize() != menu.getID() || !inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)) {
                    continue;
                }

                //Call custom ItemMenuclickEvent
                ItemMenuClickEvent e = new ItemMenuClickEvent(event.getView(), event.getSlotType(), event.getRawSlot(), event.getClick(), event.getAction(), menu);
                Bukkit.getServer().getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }

        //Closing inventories.
        //Remove inventory from open inventories if it's closed.
        @EventHandler(priority = EventPriority.HIGHEST)
        public void close(InventoryCloseEvent event) {
            Player player = (Player)event.getPlayer();
            Inventory inv = event.getInventory();

            for (ItemMenu menu : menus) {
                if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)) {
                    continue;
                }

                menu.getOpenInventories().remove(inv);
            }
        }
    }



    /**
     * Custom event which is fired when a item in a ItemMenu is clicked.
     * This event extends from InventoryClickEvent so all those methods can be used.
     */
    public static class ItemMenuClickEvent extends InventoryClickEvent implements Cancellable {

        private boolean cancelled;
        private ItemMenu menu;

        public ItemMenuClickEvent(InventoryView view, SlotType type, int slot, ClickType click, InventoryAction action, ItemMenu menu) {
            super(view, type, slot, click, action);
            this.menu = menu;
        }

        public ItemMenu getItemMenu() {
            return menu;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancel) {
            cancelled = cancel;
        }
    }


    /**
     * Static method to get all menus created.
     * @return Set of all ItemMenu's
     */
    public static Set<ItemMenu> getMenus() {
        return menus;
    }
}
