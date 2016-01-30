package io.totemo.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

// ----------------------------------------------------------------------------
/**
 * Stores all the count information for one World.
 */
public class WorldCount {
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     *
     * Don't allocate any capacity as this instance will be clear()'d.
     */
    public WorldCount() {
        _chunkCounts = new ArrayList<ChunkCount>(0);
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if there are per-chunk counts recorded.
     *
     * @return true if there are per-chunk counts recorded.
     */
    public boolean hasCounts() {
        return _chunkCounts.size() != 0;
    }

    // ------------------------------------------------------------------------
    /**
     * Clear all stored counts and reserve space for a specified number of
     * entries.
     *
     * @param count the number of entries to reserve space for.
     */
    public void clear(int count) {
        _chunkCounts = new ArrayList<ChunkCount>(count);
        _sortedGroups = null;
    }

    // ------------------------------------------------------------------------
    /**
     * Count all entities in the specified chunk in the World corresponding to
     * this WorldCount instance.
     *
     * @param chunk
     */
    public void countChunk(Chunk chunk) {
        _chunkCounts.add(new ChunkCount(chunk));
    }

    // ------------------------------------------------------------------------
    /**
     * Inner POD type used to sort total entity counts in descending order for
     * summarise().
     */
    protected static final class Total {
        public EntityType type;
        public int count;

        // --------------------------------------------------------------------
        /**
         * Convenience constructor.
         *
         * @param type entity type.
         * @param count corresponding total number of entities in the World.
         */
        public Total(EntityType type, int count) {
            this.type = type;
            this.count = count;
        }
    } // inner class Total

    // ------------------------------------------------------------------------
    /**
     * Show the total counts of all entities in this world, in sorted order.
     *
     * @param sender the command sender to be sent messages.
     */
    public void summarise(CommandSender sender) {
        EntityCounts accumulator = new EntityCounts();
        for (ChunkCount chunkCount : _chunkCounts) {
            for (int i = 0; i <= 15; ++i) {
                if (chunkCount.hasBox(i)) {
                    chunkCount.getBox(i).addCountsTo(accumulator);
                }
            }
        }

        // Sort totals in descending order for reporting.
        int totalEntities = 0;
        Total[] totals = new Total[EntityCounts.ENTITY_TYPES.length];
        for (int i = 0; i < totals.length; ++i) {
            totals[i] = new Total(EntityCounts.ENTITY_TYPES[i], accumulator.getTotal(i));
            totalEntities += totals[i].count;
        }

        Arrays.sort(totals, new Comparator<Total>() {
            @Override
            public int compare(Total left, Total right) {
                return right.count - left.count;
            }
        });

        StringBuilder s = new StringBuilder(ChatColor.YELLOW.toString());
        s.append("Total: ");
        s.append(totalEntities);
        for (int i = 0; i < totals.length; ++i) {
            if (totals[i].count == 0) {
                break;
            }
            s.append(", ");
            s.append(((i & 1) == 0 ? ChatColor.GOLD : ChatColor.YELLOW).toString());
            s.append(totals[i].type.name());
            s.append(": ");
            s.append(totals[i].count);
        }
        sender.sendMessage(s.toString());
    } // summarise

    // ------------------------------------------------------------------------
    /**
     * List entities to the sender in descending order of occcurence frequency.
     *
     * @param sender the command sender.
     * @paran page the 1-based page number of output to show.
     */
    public void list(CommandSender sender, int page) {
        sortGroups(sender);

        // NB: Page is 1-based.
        final int PAGE_SIZE = EntityControl.CONFIG.PAGE_SIZE;
        int pageCount = (_sortedGroups.length + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page < 1 || page > pageCount) {
            if (pageCount == 0) {
                sender.sendMessage(ChatColor.RED + "There are 0 results.");
            } else {
                sender.sendMessage(ChatColor.RED + "Valid page numbers are 1 to " + pageCount + ".");
            }
        } else {
            String header = ChatColor.translateAlternateColorCodes('&',
                String.format("&f---------- &6Page &e%d &6of &e%d &f----------", page, pageCount));
            sender.sendMessage(header);
            for (int i = (page - 1) * PAGE_SIZE; i < Math.min(page * PAGE_SIZE, _sortedGroups.length); ++i) {
                Location loc = _sortedGroups[i].getLocation();
                String line = String.format("%s(% 3d) %s% 3d %s%-18s %s(%d, %d, %d)",
                    ChatColor.GOLD.toString(), i + 1,
                    ChatColor.GREEN.toString(), _sortedGroups[i].getCount(),
                    ChatColor.YELLOW.toString(), _sortedGroups[i].getEntityType().name(),
                    ChatColor.GOLD.toString(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                sender.sendMessage(line);
            }
            sender.sendMessage(header);
        }
    } // list

    // ------------------------------------------------------------------------
    /**
     * Teleport the sender to the group with the specified 1-based ID.
     *
     * @param sender the command sender.
     * @param id the ID, starting at 1.
     */
    public void tp(CommandSender sender, int id) {
        sortGroups(sender);

        int index = id - 1;
        if (index < 0 || index >= _sortedGroups.length) {
            if (_sortedGroups.length == 0) {
                sender.sendMessage(ChatColor.RED + "There are no results to teleport to.");
            } else {
                sender.sendMessage(ChatColor.RED + "Valid IDs are in the range 1 to " + _sortedGroups.length + ".");
            }
        } else {
            EntityGroup group = _sortedGroups[index];
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.teleport(group.getLocation());
                Location loc = group.getLocation();
                sender.sendMessage(String.format("%sTeleporting you to %s%d %s%s %sat (%d, %d, %d).",
                    ChatColor.GOLD.toString(),
                    ChatColor.GREEN.toString(), group.getCount(),
                    ChatColor.YELLOW.toString(), group.getEntityType().name(),
                    ChatColor.GOLD.toString(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            }
        }
    } // tp

    // ------------------------------------------------------------------------
    /**
     * Cache sorted groups as teleport targets.
     *
     * @param sender CommandSender to send messages to.
     */
    protected void sortGroups(CommandSender sender) {
        if (_sortedGroups == null) {
            long start = System.currentTimeMillis();

            // Sort EntityGroups by entity count and cache the result.
            ArrayList<EntityGroup> groups = new ArrayList<EntityGroup>(_chunkCounts.size());
            for (ChunkCount chunkCount : _chunkCounts) {
                for (int i = 0; i <= 15; ++i) {
                    if (chunkCount.hasBox(i)) {
                        chunkCount.getBox(i).addEntityGroups(groups);
                    }
                }
            }

            // Would you believe ArrayList<>.sort() didn't exist until Java 8?
            _sortedGroups = new EntityGroup[groups.size()];
            groups.toArray(_sortedGroups);
            Arrays.sort(_sortedGroups, new Comparator<EntityGroup>() {
                @Override
                public int compare(EntityGroup left, EntityGroup right) {
                    return right.getCount() - left.getCount();
                }
            });

            long elapsedMillis = System.currentTimeMillis() - start;
            sender.sendMessage(ChatColor.GOLD + String.format("Sorted %d entity groups in %d milliseconds.",
                _sortedGroups.length, elapsedMillis));
        }
    } // sortGroups

    // ------------------------------------------------------------------------
    /**
     * List of {@link ChunkCount}s for all chunks loaded at the time of the
     * count.
     */
    protected ArrayList<ChunkCount> _chunkCounts;

    /**
     * Sorted array of {@link EntityGroup}s which is cached by list() the first
     * time the groups are sorted.
     *
     * The cache is invalidated by clear(). This is stored as an array partly
     * because ArrayList<>.sort() is not available in Java 7, which the server
     * may still be running on.
     */
    protected EntityGroup[] _sortedGroups;
} // class WorldCount