package ro.steve.gamesettings.gamesettings;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ro.steve.gamesettings.util.SendMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Elevator implements GameSettings, Listener {

    private final boolean enabled;
    private final String permission;
    private final Material material;
    private final String sound;
    private final List<Material> air_material;

    public Elevator() {
        YamlConfiguration yml = loadConfig("elevator");
        enabled = yml.getBoolean("enabled");
        permission = yml.getString("permission");
        material = Material.valueOf(yml.getString("block"));
        sound = yml.getString("sound");
        air_material = new ArrayList<>();
        loadAirMaterial(yml);
    }

    private void loadAirMaterial(YamlConfiguration yml) {
        if (!enabled) {
            return;
        }
        yml.getStringList("allowed_blocks").forEach(m -> {
            if (m.startsWith("~")) {
                Arrays.stream(Material.values()).filter(v -> v.toString().endsWith(m.substring(1))).forEach(air_material::add);
            } else {
                air_material.add(Material.valueOf(m));
            }
        });
    }

    @EventHandler
    public void onJumping(PlayerStatisticIncrementEvent event) {
        if (!enabled) {
            return;
        }
        if (!event.getPlayer().hasPermission(permission)) {
            return;
        }
        if (event.getStatistic() != Statistic.JUMP) {
            return;
        }
        if (event.getPlayer().getLocation().clone().subtract(0, 1, 0).getBlock().getType() != material) {
            return;
        }
        AtomicReference<Block> block = new AtomicReference<>();
        Location p = event.getPlayer().getLocation().clone();
        int x = p.getBlockX();
        int y = p.getBlockY() + 1;
        int z = p.getBlockZ();
        for (int i = 0; i < 320; i++) {
            Location l = new Location(p.getWorld(), x, y + i, z);
            if (block.get() != null) {
                continue;
            }
            if (l.getBlock().getType() == material) {
                block.set(l.getBlock());
            }
        }
        if (block.get() != null) {
            if (!checkAir(block.get().getLocation())) {
                Location l = block.get().getLocation().add(0.5, 1, 0.5);
                l.setDirection(event.getPlayer().getLocation().getDirection());
                l.setYaw(event.getPlayer().getLocation().getYaw());
                event.getPlayer().teleport(l);
                String[] split = sound.split(";");
                SendMessage.sendAction(event.getPlayer(), "elevator-up");
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.valueOf(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            }
        }
    }

    private boolean checkAir(Location l) {
        AtomicBoolean b = new AtomicBoolean(false);
        for (int i = 1; i < 3; i++) {
            if (b.get()) {
                break;
            }
            if (!air_material.contains(l.clone().add(0, i, 0).getBlock().getType())) {
                b.set(true);
            }
        }
        return b.get();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!enabled) {
            return;
        }
        if (!event.getPlayer().hasPermission(permission)) {
            return;
        }
        if (!event.isSneaking()) {
            return;
        }
        if (event.getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType() != material) {
            return;
        }
        Block block = null;
        Location p = event.getPlayer().getLocation().clone();
        int x = p.getBlockX();
        int y = p.getBlockY() - 1;
        int z = p.getBlockZ();
        for (int i = 1; i < 320; i++) {
            Location l = new Location(p.getWorld(), x, y - i, z);
            if (block != null) {
                continue;
            }
            if (l.getBlock().getType() == material) {
                block = l.getBlock();
            }
        }
        if (block != null) {
            if (!checkAir(block.getLocation())) {
                Location l = block.getLocation().add(0.5, 1, 0.5);
                l.setDirection(event.getPlayer().getLocation().getDirection());
                l.setYaw(event.getPlayer().getLocation().getYaw());
                event.getPlayer().teleport(l);
                String[] split = sound.split(";");
                SendMessage.sendAction(event.getPlayer(), "elevator-down");
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.valueOf(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            }
        }
    }
}
