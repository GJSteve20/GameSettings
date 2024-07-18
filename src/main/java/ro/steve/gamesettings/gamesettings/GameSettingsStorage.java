package ro.steve.gamesettings.gamesettings;

import org.bukkit.Location;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameSettingsStorage {

    private final ConcurrentMap<Location, String> eventBlocks;

    public GameSettingsStorage() {
        eventBlocks = new ConcurrentHashMap<>();
    }

    public void addEventBlock(Location loc, String tier) {
        eventBlocks.put(loc, tier);
    }

    public ConcurrentMap<Location, String> getEventBlocks() {
        return eventBlocks;
    }

    public void removeEventBlock(Location loc) {
        eventBlocks.remove(loc);
    }
}
