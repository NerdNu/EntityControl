EntityControl
=============

Track and manage entities.

In its current incarnation, EntityControl does the following in a simple manner:

 * Count all entities in a specified world, either from in-game or from the 
   console. (`/ec count`)
 * Groups entities by type within chunk-aligned 16x16x16 boxes and list all
   such groups in descending order by the number of entities. (`/ec list`)
 * Allow teleportation to groups. (`/ec tp #`)
 
Entities are counted when `/ec count` is run, and information about entity
groups is retained for use by `/ec list` and `/ec tp` until the next count is 
run.


Commands
--------

Note that when running `/ec` from the console, the world defaults to "world"
(if a world of that name does not exist, you must explicitly specify the name of
the world to operate in).

 * `/ec help` - Show usage help.
 * `/ec reload` - Reload the configuration. Requires permission `entitycontrol.admin`.
 * `/ec debug` - Toggle debug logging.
 * `/ec count [<world>]` - Count entities in the specified world, or the player's 
   current world if not specified.
 * `/ec list [<world>]` - List entity groups in the specified world or the player's 
   current world.
 * `/ec tp [<world>] id` - Teleport to a group by its 1-based ID, in an optionally 
   specified world.


Permissions
-----------

 * `entitycontrol.admin` - Permission to administer the plugin (e.g. `/ec reload`).
 * `entitycontrol.user` - Permission to use entity-related commands.


Planned Enhancements
--------------------

The current implementation of EntityControl is very simple.  The entire world is
counted in the same tick that the command is run.  With around 4000 chunks 
loaded, this takes between 15 and 25 milliseconds, with the server stalled for 
the duration.  In order to avoid this pause, in a future version, `/ec count` 
will start a task that counts entities over several ticks (e.g. at most 1ms of 
counting per tick) rather than all at once.

Other desirable features include:

 * Better command line syntax for specifying worlds: use `-w world` to simplify
   the code for handling these arguments.
 * ITEM_FRAME and ARMOR_STAND filtered from `/ec list` by default.  These could
   be re-included by the `-a` (all) flag.
 * Per-user-session `/ec list` results (currently the count and list information 
   is shared by all users.  When different users can elect to filter the list 
   differently, it becomes necessary to remember how each user filtered it in
   order to implement numbering of groups for `/ec tp` correctly.
 * The ability to remove entities by type either within a local area, or with
   a box centred on specified coordinates.  The latter facility would be used
   to remove excessive entities (possibly even from the console) in situations 
   where it is not possible to get near them due to client lag.
 * Find named entities by name and tamed mobs by owner, map-wide.
 

