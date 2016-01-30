package io.totemo.ec;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------
/**
 * Represents a group of entities of one type with a representative location
 * used as a teleport target.
 *
 * There is one instance of this class for every non-zero count of entities in
 * any existent Box16Count. These objects are sorted for use in /ec list.
 */
public final class EntityGroup {
    // ------------------------------------------------------------------------
    /**
     * Convenience constructor.
     *
     * @param type entity type.
     * @param count corresponding total number of entities in the World.
     * @param location representative location to use as a teleport target.
     */
    public EntityGroup(EntityType type, int count, Location location) {
        _type = type;
        _count = count;
        _location = location;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the type of all entities in this group.
     *
     * @return the type of all entities in this group.
     */
    public EntityType getEntityType() {
        return _type;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the number of entities of type {@link #getEntityType()} in this
     * group.
     *
     * @return the number of entities of type {@link #getEntityType()} in this
     *         group.
     */
    public int getCount() {
        return _count;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the location of one entity in this group that is close to the
     * average location of all entities in the group.
     *
     * @return the location of one entity in this group that is close to the
     *         average location of all entities in the group.
     */
    public Location getLocation() {
        return _location;
    }

    // ------------------------------------------------------------------------
    /**
     * The entity type.
     */
    protected EntityType _type;

    /**
     * Count of entities of the specified type.
     */
    protected int _count;

    /**
     * Representative location to use as a teleport target.
     */
    protected Location _location;
} // class EntityGroup