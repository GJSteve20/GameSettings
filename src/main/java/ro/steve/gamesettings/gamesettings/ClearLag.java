package ro.steve.gamesettings.gamesettings;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.util.SendMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearLag implements GameSettings, Listener {

    private final YamlConfiguration yml;
    private final boolean enabled;
    private final int max_entities;
    private final Map<String, Integer> chunks;
    private final List<EntityType> entityTypes;
    private final List<Integer> announcer;
    private final List<World> worlds;
    private final ConcurrentMap<String, BukkitTask> runnableMap;
    private final String removed_sound;
    private final String wait_sound;
    private int clearlag_time;

    public ClearLag() {
        yml = loadConfig("clearlag");
        enabled = yml.getBoolean("enabled");
        max_entities = yml.getInt("max-entities");
        chunks = new HashMap<>();
        entityTypes = new ArrayList<>();
        worlds = new ArrayList<>();
        announcer = yml.getIntegerList("Announcer");
        wait_sound = yml.getString("wait_sound");
        removed_sound = yml.getString("removed_sound");
        runnableMap = new ConcurrentHashMap<>();
        clearlag_time = yml.getInt("default-time");
        loadEntityTypes();
        scheduleDeletion();
    }

    private void loadEntityTypes() {
        yml.getStringList("Entities").forEach(e -> {
            entityTypes.add(EntityType.valueOf(e));
        });
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!enabled) {
            return;
        }
        if (!entityTypes.contains(event.getEntityType())) {
            return;
        }
        Chunk c = event.getEntity().getLocation().getChunk();
        String forMap = event.getEntity().getWorld().getName() + ";" + c.getX() + ";" + c.getZ();
        if (chunks.containsKey(forMap)) {
            chunks.put(forMap, chunks.get(forMap) + 1);
        } else {
            chunks.put(forMap, 1);
        }
        if (chunks.get(forMap) != null) {
            if (chunks.get(forMap) >= max_entities) {
                scheduleDelete(forMap);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!enabled) {
            return;
        }
        Chunk c = event.getItemDrop().getLocation().getChunk();
        String forMap = event.getItemDrop().getWorld().getName() + ";" + c.getX() + ";" + c.getZ();
        if (chunks.containsKey(forMap)) {
            chunks.put(forMap, chunks.get(forMap) + event.getItemDrop().getItemStack().getAmount());
        } else {
            chunks.put(forMap, event.getItemDrop().getItemStack().getAmount());
        }
        if (chunks.get(forMap) != null) {
            if (chunks.get(forMap) >= max_entities) {
                scheduleDelete(forMap);
            }
        }
    }

    @EventHandler
    public void onItemPick(PlayerPickupItemEvent event) {
        if (!enabled) {
            return;
        }
        Chunk c = event.getItem().getLocation().getChunk();
        String forMap = event.getItem().getWorld().getName() + ";" + c.getX() + ";" + c.getZ();
        if (chunks.containsKey(forMap)) {
            chunks.put(forMap, chunks.get(forMap) - event.getItem().getItemStack().getAmount());
        }
        if (chunks.get(forMap) != null) {
            if (chunks.get(forMap) >= max_entities) {
                scheduleDelete(forMap);
            }
        }
    }

    private void scheduleDelete(String forMap) {
        if (!runnableMap.containsKey(forMap)) {
            BukkitTask run = new BukkitRunnable() {
                int time = 30;
                final String[] split = forMap.split(";");
                final World world = Bukkit.getWorld(split[0]);
                final Chunk chunk = world.getChunkAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                @Override
                public void run() {
                    if (time < 1) {
                        chunks.remove(forMap);
                        int length = chunk.getEntities().length;
                        Arrays.stream(chunk.getEntities()).forEach(e -> {
                            if (entityTypes.contains(e.getType())) {
                                if (e.getCustomName() != null) {
                                    return;
                                } else {
                                    e.remove();
                                }
                            }
                        });
                        Arrays.stream(chunk.getEntities()).forEach(e -> {
                            if (e instanceof Player) {
                                SendMessage.sendAction((Player) e, "removed-entities", "%amount%;" + length);
                                String[] split = removed_sound.split(";");
                                ((Player)e).playSound(e.getLocation(), Sound.valueOf(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                            }
                        });
                        cancel();
                        runnableMap.remove(forMap);
                    } else {
                        if (announcer.contains(time)) {
                            Arrays.stream(chunk.getEntities()).forEach(e -> {
                                if (e instanceof Player) {
                                    SendMessage.sendAction((Player) e, "clear-chunk", "%time%;" + time);
                                    String[] split = wait_sound.split(";");
                                    ((Player)e).playSound(e.getLocation(), Sound.valueOf(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                                }
                            });
                        }
                        time--;
                    }
                }
            }.runTaskTimer(GameSettingsMain.getInstance(), 0L, 20L);
            runnableMap.put(forMap, run);
        }
    }

    public void scheduleDeletion() {
        new BukkitRunnable() {
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    clearlag_time = yml.getInt("default-time");
                    return;
                }
                if (announcer.contains(clearlag_time)) {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        SendMessage.sendAction(p, "clear", "%time%;" + clearlag_time, "%world%;" + p.getWorld().getName());
                        String[] split = wait_sound.split(";");
                        p.playSound(p.getLocation(), Sound.valueOf(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                    });
                }
                clearlag_time--;
                if (clearlag_time < 1) {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (!worlds.contains(p.getWorld())) {
                            worlds.add(p.getWorld());
                        }
                    });
                    worlds.forEach(w -> {
                        AtomicInteger entities = new AtomicInteger();
                        w.getEntities().forEach(e -> {
                            if (!entityTypes.contains(e.getType())) {
                                return;
                            }
                            if (e.getCustomName() != null) {
                                return;
                            }
                            e.remove();
                            entities.getAndIncrement();
                        });
                        w.getPlayers().forEach(p -> {
                            SendMessage.sendAction(p, "removed-entities-world", "%amount%;" + entities, "%world%;" + w.getName());
                            String[] split = removed_sound.split(";");
                            p.playSound(p.getLocation(), Sound.valueOf(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        });
                    });
                    clearlag_time = yml.getInt("default-time");
                }
            }
        }.runTaskTimer(GameSettingsMain.getInstance(), 0L, 20L);
    }
}
