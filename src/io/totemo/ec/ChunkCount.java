package io.totemo.ec;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

// ----------------------------------------------------------------------------
/**
 * Stores the entity counts for one Chunk.
 */
public class ChunkCount {
    // ------------------------------------------------------------------------
    /**
     * Construct a ChunkCount and populate it with entity counts for the
     * specified Chunk.
     *
     * @param chunk the Chunk to count.
     */
    public ChunkCount(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            Location loc = entity.getLocation();
            int boxIndex = loc.getBlockY() / 16;
            getBox(boxIndex).countEntity(entity);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the box with the specified index exists.
     *
     * @param index the index in the range [0,15], corresponding to Y
     *        coordinates in the range [index*16, index*16+15].
     * @return true if the box with the specified index exists.
     */
    public boolean hasBox(int index) {
        return _boxes[index] != null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the box with the specified index.
     *
     * The {@link Box16Count} instance is created on demand.
     *
     * @param index the index in the range [0,15], corresponding to Y
     *        coordinates in the range [index*16, index*16+15].
     *
     */
    public Box16Count getBox(int index) {
        Box16Count box = _boxes[index];
        if (box == null) {
            box = _boxes[index] = new Box16Count();
        }
        return box;
    }

    // ------------------------------------------------------------------------
    /**
     * Counts for the 16 boxes in this chunk, listed from lowest altitude (Y in
     * [0,15]) to highest (Y in [240,255]).
     */
    protected Box16Count[] _boxes = new Box16Count[16];
} // class ChunkCount