package com.clashwars.cwstats.stats.internal;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;

public class StatCategory {

    public int category_id;
    public String name;
    public CWItem item;

    public StatCategory(int category_id, String name, String item) {
        this.category_id = category_id;
        this.name = name;

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
