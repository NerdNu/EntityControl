package io.totemo.ec;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// ----------------------------------------------------------------------------
/**
 * Main plugin class.
 */
public class EntityControl extends JavaPlugin {
    /**
     * Configuration instance.
     */
    public static Configuration CONFIG;

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        CONFIG = new Configuration(this);
        CONFIG.reload();
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     *
     *      <ul>
     *      <li>/ec help</li>
     *      <li>/ec reload</li>
     *      <li>/ec debug</li>
     *      <li>/ec count [world]</li>
     *      <li>/ec list [world] [page]</li>
     *      <li>/ec tp [world] id</li>
     *      </ul>
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ec")) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                // Let Bukkit show usage help from plugin.yml.
                return false;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("EntityControl.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload the configuration.");
                    return true;
                }
                CONFIG.reload();
                sender.sendMessage(ChatColor.GOLD + "EntityControl configuration reloaded.");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("debug")) {
                CONFIG.DEBUG = !CONFIG.DEBUG;
                sender.sendMessage(ChatColor.GOLD + "EntityControl debug logging " + (CONFIG.DEBUG ? "ENABLED." : "DISABLED."));
                CONFIG.save();
                return true;
            }
            if (handleCount(sender, args)) {
                return true;
            }
            if (handleList(sender, args)) {
                return true;
            }
            if (handleTP(sender, args)) {
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Invalid command syntax.");
        return false;
    } // onCommand

    // ------------------------------------------------------------------------
    /**
     * Handle /ec count [world].
     *
     * @param sender the command sender.
     * @param args command arguments.
     * @return true if command was handled.
     */
    protected boolean handleCount(CommandSender sender, String[] args) {
        if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("count")) {
            World world = getWorldFromArgs(sender, args, (args.length == 2) ? 1 : -1);
            if (world != null) {
                countWorld(sender, world);
            }
            return true;
        } else {
            return false;
        }
    } // handleCount

    // ------------------------------------------------------------------------
    /**
     * Handle /ec list [world] [page].
     *
     * @param sender the command sender.
     * @param args command arguments.
     * @return true if command was handled; false if Bukkit should do standard
     *         error handling.
     */
    protected boolean handleList(CommandSender sender, String[] args) {
        if (args.length >= 1 && args.length <= 3 && args[0].equalsIgnoreCase("list")) {
            World world;
            int page = 1;

            if (args.length == 3) {
                world = getWorldFromArgs(sender, args, 1);

                // Try to parse args[2] as a page number.
                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + args[2] + " is not a valid page number.");
                    return true;
                }

            } else if (args.length == 2) {
                // Try to parse args[1] as a page number.
                try {
                    page = Integer.parseInt(args[1]);
                    world = getWorldFromArgs(sender, args, -1);
                } catch (NumberFormatException ex) {
                    // So it's probably a world name.
                    world = getWorldFromArgs(sender, args, 1);
                }

            } else { // args.length == 1
                world = getWorldFromArgs(sender, args, -1);
            }

            // Check that the parsed world name is ok.
            if (world == null) {
                return true;
            }

            WorldCount worldCount = getWorldCount(world.getName());
            if (!worldCount.hasCounts()) {
                countWorld(sender, world);
            }

            worldCount.list(sender, page);
            return true;
        } else {
            return false;
        }
    } // handleList

    // ------------------------------------------------------------------------
    /**
     * Handle /ec tp [world] id.
     *
     * @param sender the command sender.
     * @param args command arguments.
     * @return true if command was handled; false if Bukkit should do standard
     *         error handling.
     */
    protected boolean handleTP(CommandSender sender, String[] args) {
        if (args.length >= 2 && args.length <= 3 && args[0].equalsIgnoreCase("tp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be in game to teleport.");
                return true;
            }

            int id;
            World world;
            if (args.length == 3) {
                world = getWorldFromArgs(sender, args, 1);

                // Try to parse args[2] as an ID.
                try {
                    id = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + args[2] + " is not a valid id number.");
                    return true;
                }

            } else { // args.length == 2
                world = getWorldFromArgs(sender, args, -1);

                // Try to parse args[1] as an ID.
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + args[1] + " is not a valid id number.");
                    return true;
                }
            }

            // Check that the parsed world name is ok.
            if (world == null) {
                return true;
            }

            WorldCount worldCount = getWorldCount(world.getName());
            if (!worldCount.hasCounts()) {
                countWorld(sender, world);
            }

            worldCount.tp(sender, id);

            return true;

        } else {
            return false;
        }
    } // handleTP

    // ------------------------------------------------------------------------
    /**
     * Return the world specified in the commmand arguments, a suitable default,
     * or null if that is not possible.
     *
     * @param sender the command sender.
     * @param args the command arguments.
     * @param argIndex the index of the world name in args[], or out of range
     *        (e.g. -1) if not specified there.
     * @return the world specified in the commmand arguments, a suitable
     *         default, or null if that is not possible.
     */
    protected World getWorldFromArgs(CommandSender sender, String[] args, int argIndex) {
        World world = null;
        if (argIndex >= 0 && argIndex < args.length) {
            world = Bukkit.getServer().getWorld(args[argIndex]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Invalid world name specified.");
                return null;
            }
        }

        if (world == null) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                world = player.getLocation().getWorld();
            } else {
                world = Bukkit.getServer().getWorld("world");
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "Default world \"world\" doesn't exist. You must specify a world when running from Console.");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "Counting in the default world, \"world\".");
                }
            }
        }
        return world;
    } // getWorldFromArgs

    // ------------------------------------------------------------------------
    /**
     * Get or create the {@link WorldCount} corresponding to the named World.
     *
     * @param worldName the name of the World.
     * @return the {@link WorldCount}.
     */
    protected WorldCount getWorldCount(String worldName) {
        WorldCount count = _worldCounts.get(worldName);
        if (count == null) {
            count = new WorldCount();
            _worldCounts.put(worldName, count);
        }
        return count;
    }

    // ------------------------------------------------------------------------
    /**
     * Update the stored counts for the specified world.
     *
     * @param sender the comamnd sender to be notified of results.
     * @param world the (non-null) World.
     */
    protected void countWorld(CommandSender sender, World world) {
        long start = System.currentTimeMillis();
        Chunk[] loadedChunks = world.getLoadedChunks();

        WorldCount count = getWorldCount(world.getName());
        count.clear(loadedChunks.length);
        for (Chunk chunk : loadedChunks) {
            count.countChunk(chunk);
        }

        count.summarise(sender);
        long elapsedMillis = System.currentTimeMillis() - start;
        sender.sendMessage(ChatColor.GOLD + String.format("Counted %d loaded chunks in %d milliseconds.", loadedChunks.length, elapsedMillis));
    } // countWorld

    // ------------------------------------------------------------------------
    /**
     * Map from world name to corresponding {@link WorldCount}s.
     */
    protected HashMap<String, WorldCount> _worldCounts = new HashMap<String, WorldCount>();
} // class EntityControl