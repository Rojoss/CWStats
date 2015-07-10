package com.clashwars.cwstats.stats.internal;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;

public class Stat {

    public int stat_id;
    public int category_id;
    public String name;
    public String short_name;
    public String description;
    public boolean displayed;
    public boolean calculated;
    public String calculation;

    public CWItem item;

    public Stat(int stat_id, int category_id, String name, String short_name, String description, String item, boolean displayed, boolean calculated, String calculation) {
        this.stat_id = stat_id;
        this.category_id = category_id;
        this.name = name;
        this.short_name = short_name;
        this.description = description;
        this.displayed = displayed;
        this.calculated = calculated;
        this.calculation = calculation;


        int id = 1;
        int data = 0;
        if (item != null && item.length() > 0) {
            String[] itemSplit = item.split(":");
            if (itemSplit.length > 0) {
                id = CWUtil.getInt(itemSplit[0]);
                if (itemSplit.length > 1) {
                    data = CWUtil.getInt(itemSplit[1]);
                }
            }
        }
        this.item = new CWItem(Math.max(id, 1), 1, (byte)Math.max(data, 0));
    }

}
