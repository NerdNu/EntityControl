package io.totemo.ec;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------
/**
 * Stores counts of Entities.
 *
 * EntityCounts can be filtered by EntityType and summed.
 */
public class EntityCounts {
    // ------------------------------------------------------------------------
    /**
     * Cache the array of all EntityType enums to avoid unnecessary values()
     * calls.
     */
    public static final EntityType[] ENTITY_TYPES = EntityType.values();

    // ------------------------------------------------------------------------
    /**
     * Clear all counts to zero.
     */
    public void clear() {
        for (int i = 0; i < _counts.length; ++i) {
            _counts[i] = 0;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Count the specified entity.
     *
     * @param entity the entity.
     */
    public void addEntity(Entity entity) {
        ++_counts[entity.getType().ordinal()];
    }

    // ------------------------------------------------------------------------
    /**
     * Return the stored total count of entities of the specified type.
     *
     * @param type the EntityType.
     * @Return the stored total count of entities of the specified type.
     */
    public int getTotal(EntityType type) {
        return _counts[type.ordinal()];
    }

    // ------------------------------------------------------------------------
    /**
     * Return the stored total count of entities of the specified type.
     *
     * @param ordinal the EntityType.ordinal() value identifying the EntityType.
     * @Return the stored total count of entities of the specified type.
     */
    public int getTotal(int ordinal) {
        return _counts[ordinal];
    }

    // ------------------------------------------------------------------------
    /**
     * Increase the counts in the accumulator by the counts in this object.
     *
     * @param accumulator the object that accumulates counts.
     */
    public void addTo(EntityCounts accumulator) {
        for (int i = 0; i < _counts.length; ++i) {
            accumulator._counts[i] += _counts[i];
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Counts for each EntityType, indexed by EntityType.ordinal().
     */
    protected int[] _counts = new int[ENTITY_TYPES.length];
} // class EntityCounts