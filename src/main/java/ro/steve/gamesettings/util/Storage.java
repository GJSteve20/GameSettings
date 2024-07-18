package ro.steve.gamesettings.util;

import ro.steve.gamesettings.gamesettings.*;

import java.util.ArrayList;
import java.util.List;

public class Storage {

    private final List<GameSettings> gameSettingsList;

    public Storage() {
        gameSettingsList = new ArrayList<>();
        gameSettingsList.add(new Elevator());
        gameSettingsList.add(new ClearLag());
        gameSettingsList.add(new Gens());
        gameSettingsList.add(new InventoryEvent());
        gameSettingsList.add(new MobStacker());
        gameSettingsList.add(new AntiSwear());
        gameSettingsList.add(new AntiAd());
    }

    public List<GameSettings> getGameSettingsList() {
        return gameSettingsList;
    }
}
