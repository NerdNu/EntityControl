package io.totemo.ec;

import org.bukkit.plugin.Plugin;

// ----------------------------------------------------------------------------
/**
 * Configuration wrapper.
 */
public class Configuration {
    /**
     * True if debug messages should be logged.
     */
    public boolean DEBUG;

    /**
     * Size of pages (in lines) from /ec list.
     */
    public int PAGE_SIZE;

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public Configuration(Plugin plugin) {
        _plugin = plugin;
    }

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        _plugin.reloadConfig();
        DEBUG = _plugin.getConfig().getBoolean("debug");
        PAGE_SIZE = _plugin.getConfig().getInt("page_size");
    }

    // ------------------------------------------------------------------------
    /**
     * Save the configuration to disk.
     */
    public void save() {
        _plugin.getConfig().set("debug", DEBUG);
        _plugin.getConfig().set("page_size", PAGE_SIZE);
        _plugin.saveConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Owning plugin.
     */
    private final Plugin _plugin;

} // class Configuration