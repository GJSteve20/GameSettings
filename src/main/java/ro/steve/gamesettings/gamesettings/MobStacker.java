package ro.steve.gamesettings.gamesettings;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import ro.steve.gamesettings.util.Color;

import java.util.ArrayList;
import java.util.List;

public class MobStacker implements GameSettings, Listener {

    private final boolean enabled;
    private final int distance;
    private final String name;
    private final int stack;
    private final List<EntityType> types;

    public MobStacker() {
        types = new ArrayList<>();
        YamlConfiguration yml = loadConfig("mob-stacker");
        enabled = yml.getBoolean("enabled");
        distance = yml.getInt("distance");
        stack = yml.getInt("stack");
        name = yml.getString("name");
        yml.getStringList("Mobs").forEach(m -> types.add(EntityType.valueOf(m)));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (!enabled) {
            return;
        }
        if (!types.contains(event.getEntityType())) {
            return;
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }
        boolean found = false;
        String m = name;
        for (Entity e : event.getEntity().getNearbyEntities(distance, distance, distance)) {
            if (found) {
                continue;
            }
            if (e.getType() != event.getEntityType()) {
                continue;
            }
            if (e.getPersistentDataContainer().has(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER)) {
                Integer entities = e.getPersistentDataContainer().get(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER);
                assert entities != null;
                if (entities < stack) {
                    event.setCancelled(true);
                    e.getPersistentDataContainer().set(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER, (entities + 1));
                    e.setCustomNameVisible(true);
                    e.setCustomName(Color.process(m.replace("%type%", e.getType().toString()).replace("%amount%", "" + (entities + 1))));
                    found = true;
                }
            }
        }
        if (!found) {
            event.getEntity().getPersistentDataContainer().set(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER, 1);
            event.getEntity().setCustomNameVisible(true);
            event.getEntity().setCustomName(Color.process(m).replace("%type%", event.getEntityType().toString()).replace("%amount%", "" + 1));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!enabled) {
            return;
        }
        if (!types.contains(event.getEntityType())) {
            return;
        }
        if (!event.getEntity().getPersistentDataContainer().has(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER)) {
            return;
        }
        Integer entities = event.getEntity().getPersistentDataContainer().get(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER);
        assert entities != null;
        EntityType type = event.getEntityType();
        Location l = event.getEntity().getLocation();
        spawnEntity(type, l, entities);
    }

    private void spawnEntity(EntityType type, Location location, Integer entities) {
        if (entities > 1) {
            Entity ent = location.getWorld().spawnEntity(location, type);
            ent.setCustomNameVisible(true);
            ent.setCustomName(Color.process(name).replace("%type%", type.toString()).replace("%amount%", "" + (entities - 1)));
            ent.getPersistentDataContainer().set(NamespacedKey.minecraft("mob-stacker"), PersistentDataType.INTEGER, (entities - 1));
        }
    }
}
