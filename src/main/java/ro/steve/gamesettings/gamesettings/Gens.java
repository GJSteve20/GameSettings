package ro.steve.gamesettings.gamesettings;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.inventory.GameSettingsInventory;
import ro.steve.gamesettings.item.ItemBuilder;
import ro.steve.gamesettings.util.Color;
import ro.steve.gamesettings.util.SendMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Gens implements GameSettings, Listener {

    private final GameSettingsStorage gss;
    private final YamlConfiguration yml;
    private final boolean enabled;
    private final int chunk_limiter;

    public Gens() {
        gss = GameSettingsMain.getInstance().getGameSettingsStorage();
        yml = loadConfig("gens");
        enabled = yml.getBoolean("enabled");
        chunk_limiter = yml.getInt("chunk_limiter");
        loadBlocks();
        loadRunnables();
    }

    private void loadRunnables() {
        int distance = yml.getInt("player_distance");
        yml.getConfigurationSection("Gens").getKeys(false).forEach(c -> {
            int time = yml.getInt("Gens." + c + ".time");
            new BukkitRunnable() {
                public void run() {
                    if (gss.getEventBlocks().isEmpty()) {
                        return;
                    }
                    gss.getEventBlocks().forEach((l, t) -> {
                        if (c.equalsIgnoreCase(t)) {
                            List<String> lore = new ArrayList<>();
                            if (yml.get("Gens." + t + ".item.lore") != null) {
                                yml.getStringList("Gens." + t + ".item.lore").forEach(m -> lore.add(Color.process(m)));
                            }
                            if (l.getWorld().getNearbyEntities(l, distance, distance, distance).stream().filter(e -> e instanceof Player).toList().isEmpty()) {
                                return;
                            }
                            ItemStack item = new ItemBuilder()
                                    .type(Material.valueOf(yml.getString("Gens." + t + ".item.material")))
                                    .name(Color.process(yml.getString("Gens." + t + ".item.name")))
                                    .lore(lore)
                                    .build();
                            Location spawn = l.clone();
                            spawn.add(0.5, 1.5, 0.5);
                            spawn.setDirection(new Vector(0, 0, 0));
                            spawn.getWorld().dropItem(spawn, item).setVelocity(new Vector(0, 0, 0));
                        }
                    });
                }
            }.runTaskTimer(GameSettingsMain.getInstance(), time * 20L, time * 20L);
        });
    }

    private void loadBlocks() {
        File file = new File(GameSettingsMain.getInstance().getDataFolder(), "blocks.yml");
        if (file.exists()) {
            YamlConfiguration blocks = YamlConfiguration.loadConfiguration(file);
            blocks.getConfigurationSection("Blocks").getKeys(false).forEach(c -> {
                String[] split = blocks.getString("Blocks." + c + ".location").split(";");
                Location loc = new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                String tier = blocks.getString("Blocks." + c + ".tier");
                if (loc.getBlock().getType() == Material.valueOf(this.yml.getString("Gens." + tier + ".material"))) {
                    gss.addEventBlock(loc, tier);
                }
            });
        }
        file.delete();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!enabled) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (event.getItemInHand().getItemMeta() == null) {
            return;
        }
        if (!event.getItemInHand().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("gens.tier"), PersistentDataType.STRING)) {
            return;
        }
        AtomicInteger chunkGens = new AtomicInteger(0);
        gss.getEventBlocks().forEach((l, t) -> {
            if (!l.getWorld().equals(event.getBlock().getWorld())) {
                return;
            }
            if (!l.getChunk().equals(event.getBlock().getChunk())) {
                return;
            }
            chunkGens.incrementAndGet();
        });
        if (chunkGens.get() == chunk_limiter) {
            event.setCancelled(true);
            SendMessage.sendMessage(event.getPlayer(), "max-gens-per-chunk", "%limit%;" + chunk_limiter);
            return;
        }
        String tier = event.getItemInHand().getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("gens.tier"), PersistentDataType.STRING);
        gss.addEventBlock(event.getBlock().getLocation(), tier);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!enabled) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (!gss.getEventBlocks().containsKey(event.getBlock().getLocation())) {
            return;
        }
        event.setCancelled(true);
        if (event.getPlayer().getInventory().firstEmpty() == -1) {
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), new ItemBuilder()
                    .type(Material.valueOf(yml.getString("Gens." + gss.getEventBlocks().get(event.getBlock().getLocation()) + ".material")))
                    .amount(1)
                    .name(Color.process(yml.getString("Gens." + gss.getEventBlocks().get(event.getBlock().getLocation()) + ".name")))
                    .glowing(true)
                    .withPersistent("gens.tier", PersistentDataType.STRING, gss.getEventBlocks().get(event.getBlock().getLocation()))
                    .build());
            SendMessage.sendAction(event.getPlayer(), "full-inventory");
        } else {
            event.getPlayer().getInventory().addItem(new ItemBuilder()
                    .type(Material.valueOf(yml.getString("Gens." + gss.getEventBlocks().get(event.getBlock().getLocation()) + ".material")))
                    .amount(1)
                    .name(Color.process(yml.getString("Gens." + gss.getEventBlocks().get(event.getBlock().getLocation()) + ".name")))
                    .glowing(true)
                    .withPersistent("gens.tier", PersistentDataType.STRING, gss.getEventBlocks().get(event.getBlock().getLocation()))
                    .build());
        }
        gss.removeEventBlock(event.getBlock().getLocation());
        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (!enabled) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!gss.getEventBlocks().containsKey(event.getClickedBlock().getLocation())) {
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        String tier = gss.getEventBlocks().get(event.getClickedBlock().getLocation());
        String next = "Tier_" + (Integer.parseInt(tier.substring(5)) + 1);
        if (yml.get("Gens." + next) == null) {
            SendMessage.sendAction(event.getPlayer(), "max-gen");
            return;
        }
        event.getPlayer().openInventory(new GameSettingsInventory("confirm", event.getClickedBlock().getLocation()).getInventory());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!enabled) {
            return;
        }
        event.blockList().removeIf(b -> GameSettingsMain.getInstance().getGameSettingsStorage().getEventBlocks().containsKey(b.getLocation()));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!enabled) {
            return;
        }
        event.blockList().removeIf(b -> GameSettingsMain.getInstance().getGameSettingsStorage().getEventBlocks().containsKey(b.getLocation()));
    }

    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent event) {
        if (!enabled) {
            return;
        }
        for (Block b : event.getBlocks()) {
            if (GameSettingsMain.getInstance().getGameSettingsStorage().getEventBlocks().containsKey(b.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
