package org.macausmp.sportsday;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Config files for storing players' plugin data.
 */
public final class ConfigManager {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private File contestantsFile;
    private FileConfiguration contestantsConfig;
    private File competitionFile;
    private FileConfiguration competitionConfig;

    void setup() {
        if (!PLUGIN.getDataFolder().exists() && PLUGIN.getDataFolder().mkdir())
            PLUGIN.getLogger().log(Level.INFO, "Data folder created");
        contestantsFile = new File(PLUGIN.getDataFolder(), "contestants.yml");
        contestantsConfig = loadFile(contestantsFile, "contestants.yml");
        competitionFile = new File(PLUGIN.getDataFolder(), "competition.yml");
        competitionConfig = loadFile(competitionFile, "competition.yml");
    }

    private @NotNull FileConfiguration loadFile(@NotNull File file, String name) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    PLUGIN.getLogger().log(Level.INFO, name + " file has been created");
                }
            } catch (IOException e) {
                PLUGIN.getLogger().log(Level.SEVERE, "Could not create the " + name + " file");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Get the config that store contestants' data.
     * @return contestants data config
     */
    public FileConfiguration getContestantsConfig() {
        return contestantsConfig;
    }

    public void saveContestsConfig() {
        try {
            contestantsConfig.save(contestantsFile);
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not save the contestants.yml file", e);
        }
    }

    /**
     * Get the config that store competition data.
     * @return competition data config
     */
    public FileConfiguration getCompetitionConfig() {
        return competitionConfig;
    }

    public void saveCompetitionConfig() {
        try {
            competitionConfig.save(competitionFile);
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not save the competition.yml file", e);
        }
    }
}
