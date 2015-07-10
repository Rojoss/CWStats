package com.clashwars.cwstats.util;

import com.clashwars.cwcore.utils.CWUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {

    public static String formatMsg(String msg) {
        return CWUtil.integrateColor("&8[&4CW&8] &6" + msg);
    }


    public static String getTimeStamp() {
        return getTimeStamp("[dd-MM HH:mm:ss]");
    }

    public static String getTimeStamp(String syntax) {
        return new SimpleDateFormat(syntax).format(Calendar.getInstance().getTime());
    }

    public static String timeStampToDateString(Timestamp timestamp) {
        return new SimpleDateFormat("dd MMM yyyy").format(timestamp);
    }
}
