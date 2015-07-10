package com.clashwars.cwstats.config;

import com.clashwars.cwcore.config.internal.EasyConfig;

/**
 * Main plugin configuration file with all general config settings.
 */
public class PluginCfg extends EasyConfig {

    public String SQL__PASS = "SECRET";

    public PluginCfg(String fileName) {
        this.setFile(fileName);
    }
}
