package io.totemo.ec;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------
/**
 * Holds the entity counts for 1/16th of a Chunk as a 16x16x16 box.
 *
 * An instance of this class will not exist for a particular Location unless the
 * box contains at least one entity.
 */
public class Box16Count {
    // ------------------------------------------------------------------------
    /**
     * Count the specified entity that is within this box.
     *
     * Also update a per-entity-type average location, and a representative
     * location, which is the entity location when it happens to be closer to
     * the average than some previous entity location that was considered. (It's
     * not guaranteed to be the entity location strictly closest to the average
     * as that would require that they are all considered *after* the average is
     * finalised.)
     */
    public void countEntity(Entity entity) {
        _counts.addEntity(entity);

        int typeOrdinal = entity.getType().ordinal();
        Location entityLoc = entity.getLocation();

        if (_averageLocation[typeOrdinal] == null) {
            // First entity of this type.
            _averageLocation[typeOrdinal] = entityLoc.clone();
            _representativeLocation[typeOrdinal] = entityLoc;
        } else {
            try {
                // At least one entity of this type has been counted before.
                Location average = _averageLocation[typeOrdinal].multiply(_counts.getTotal(typeOrdinal) - 1);
                average.add(entityLoc);
                average.multiply(1.0 / _counts.getTotal(typeOrdinal));
                _averageLocation[typeOrdinal] = average;

                // Change the representative location to that of the new entity
                // only if the new entity is closer to the new average that the
                // old representative location (another entity location).
                if (average.distanceSquared(entityLoc) < average.distanceSquared(_representativeLocation[typeOrdinal])) {
                    _representativeLocation[typeOrdinal] = entityLoc;
                }
            } catch (Exception ex) {
                World w1 = _averageLocation[typeOrdinal].getWorld();
                World w2 = entityLoc.getWorld();
                String w1Name = w1 != null ? w1.getName() : "null";
                String w2Name = w2 != null ? w2.getName() : "null";
                Bukkit.getServer().getLogger().info("Mixing worlds in countEntity():" +
                                                    ex.getMessage() + ": " + w1Name + " vs " + w2Name);
            }
        }
    } // countEntity

    // ------------------------------------------------------------------------
    /**
     * Accumulate the entity counts for this 16x16x16 box into the totals.
     *
     * @param totals the running sum of entity counts by type, modified by this
     *        method.
     */
    public void addCountsTo(EntityCounts totals) {
        _counts.addTo(totals);
    }

    // ------------------------------------------------------------------------
    /**
     * Add all {@link EntityGroup}s corresponding to non-zero counts of specific
     * entity types for this box to the parameter ArrayList<>.
     *
     * @param groups ArrayList<> of EntityGroups to be added to.
     */
    public void addEntityGroupsTo(ArrayList<EntityGroup> groups) {
        for (EntityType type : EntityCounts.ENTITY_TYPES) {
            int count = _counts.getTotal(type);
            if (count != 0) {
                groups.add(new EntityGroup(type, count, _representativeLocation[type.ordinal()]));
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Entity counts indexed by entity type.
     */
    protected EntityCounts _counts = new EntityCounts();

    /**
     * Median of the Locations of all entities in this 16x16x16 box, by entity
     * type.
     */
    protected Location[] _averageLocation = new Location[EntityCounts.ENTITY_TYPES.length];

    /**
     * Location of the entity that is closest to the average entity location for
     * its type.
     */
    protected Location[] _representativeLocation = new Location[EntityCounts.ENTITY_TYPES.length];

} // class Box16Count